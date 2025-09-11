package com.codeit.findex.openApi.service;

import com.codeit.findex.openApi.domain.AutoSyncConfig;
import com.codeit.findex.openApi.dto.AutoSyncConfigDto;
import com.codeit.findex.openApi.dto.request.AutoSyncConfigUpdateRequest;
import com.codeit.findex.openApi.dto.response.CursorPageResponseAutoSyncConfigDto;
import com.codeit.findex.openApi.mapper.AutoSyncConfigMapper;
import com.codeit.findex.openApi.repository.AutoSyncConfigRepository;
import com.codeit.findex.openApi.spec.AutoSyncConfigSpecs;
import com.codeit.findex.openApi.spec.CursorUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class AutoSyncConfigService {

	private final AutoSyncConfigRepository repository;

	public CursorPageResponseAutoSyncConfigDto getList(
		Long indexInfoId,
		Boolean enabled,
		Long idAfter,
		String cursor,
		String sortField,
		Sort.Direction direction,
		Integer size
	) {
		// 0) size 클램핑
		final int pageSize = (size == null) ? 10 : Math.max(1, Math.min(100, size));

		// 1) 정렬 화이트리스트
		Map<String, String> sortMap = new HashMap<>();
		sortMap.put("indexInfo.indexName", "indexInfo.indexName");
		sortMap.put("indexName", "indexInfo.indexName");
		sortMap.put("enabled", "enabled");
		sortMap.put("id", "id");

		// (옵션) 필터( enabled/indexInfoId )가 있으면 안정성을 위해 id 정렬 강제
		boolean forceIdSort = (enabled != null) || (indexInfoId != null);
		String requestedSortProp = sortMap.getOrDefault(
			(sortField == null || sortField.isBlank()) ? "indexInfo.indexName" : sortField,
			"indexInfo.indexName"
		);
		final String sortProp = forceIdSort ? "id" : requestedSortProp;

		final Sort.Direction dir = (direction == null) ? Sort.Direction.ASC : direction;
		Sort sort = Sort.by(new Sort.Order(dir, sortProp)).and(Sort.by(Sort.Order.asc("id")));

		// 2) 기본 필터
		Specification<AutoSyncConfig> spec = (root, q, cb) -> cb.conjunction();
		spec = spec.and(AutoSyncConfigSpecs.indexInfoIdEq(indexInfoId))
			.and(AutoSyncConfigSpecs.enabledEq(enabled));

		// 3) 커서 decode (엄격검사 + 필터 불일치 시 커서 무시)
		CursorUtil.Decoded decoded = null;
		if (cursor != null && !cursor.isBlank()) {
			CursorUtil.Decoded d = CursorUtil.decode(cursor);
			if (d == null || !d.valid) {
				throw new IllegalArgumentException("Invalid cursor");
			}
			// 정렬 불일치 → 커서 무시(첫 페이지)
			if (!Objects.equals(d.sortField, sortProp) || d.direction != dir) {
				d = null;
			}
			// 필터 불일치 → 커서 무시(첫 페이지)  (하위호환: 커서에 ff 없으면 무시)
			if (d != null) {
				if ((d.enabledFilter != null && !Objects.equals(d.enabledFilter, enabled)) ||
					(d.indexInfoIdFilter != null && !Objects.equals(d.indexInfoIdFilter, indexInfoId))) {
					d = null;
				}
			}
			decoded = d;
		}

		// 4) 초기 커서(idAfter 제공) — id 정렬일 때만
		if (decoded == null && "id".equals(sortProp) && idAfter != null) {
			decoded = CursorUtil.ofIdOnly("id", dir, idAfter);
		}

		// 5) 커서 스펙 적용
		if (decoded != null && decoded.valid) {
			String keySort = (decoded.sortField == null) ? sortProp : decoded.sortField;
			if ("indexInfo.indexName".equals(keySort)) {
				spec = spec.and(AutoSyncConfigSpecs.afterIndexName(decoded.sortValue, decoded.lastId, dir));
			} else if ("enabled".equals(keySort)) {
				Boolean lastEnabled = (decoded.sortValue == null) ? null : Boolean.valueOf(decoded.sortValue);
				spec = spec.and(AutoSyncConfigSpecs.afterEnabled(lastEnabled, decoded.lastId, dir));
			} else { // id
				spec = spec.and(AutoSyncConfigSpecs.idAfter(decoded.lastId, dir));
			}
		}

		// 6) 조회
		Pageable pageable = PageRequest.of(0, pageSize + 1, sort);
		Page<AutoSyncConfig> page = repository.findAll(spec, pageable);
		List<AutoSyncConfig> rows = page.getContent();

		boolean hasNext = rows.size() > pageSize;
		if (hasNext) rows = rows.subList(0, pageSize);

		List<AutoSyncConfigDto> content = rows.stream()
			.map(AutoSyncConfigMapper::toDto)
			.collect(Collectors.toList());

		// 7) nextCursor (필터 핑거프린트 포함)
		String nextCursor = null;
		Long nextIdAfter = null;
		if (!rows.isEmpty()) {
			AutoSyncConfig last = rows.get(rows.size() - 1);
			nextIdAfter = last.getId();

			String sv = null;
			if ("indexInfo.indexName".equals(sortProp)) {
				sv = (last.getIndexInfo() != null) ? last.getIndexInfo().getIndexName() : null;
			} else if ("enabled".equals(sortProp)) {
				sv = String.valueOf(Boolean.TRUE.equals(last.getEnabled()));
			}

			nextCursor = CursorUtil.encode(
				sortProp, dir, sv, last.getId(),
				enabled, indexInfoId
			);
		}

		return CursorPageResponseAutoSyncConfigDto.builder()
			.content(content)
			.nextCursor(nextCursor)
			.nextIdAfter(nextIdAfter)
			.size(pageSize)
			.totalElements(page.getTotalElements())
			.hasNext(hasNext)
			.build();
	}

	@Transactional
	public AutoSyncConfigDto update(Long id, AutoSyncConfigUpdateRequest request) {
		if (request == null || request.getEnabled() == null) {
			throw new IllegalArgumentException("enabled는 필수입니다.");
		}
		AutoSyncConfig entity = repository.findById(id)
			.orElseThrow(() -> new NoSuchElementException("자동 연동 설정을 찾을 수 없습니다. id=" + id));
		entity.setEnabled(request.getEnabled());
		return AutoSyncConfigMapper.toDto(entity);
	}
}

