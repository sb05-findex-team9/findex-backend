package com.codeit.findex.indexData.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codeit.findex.indexData.domain.IndexData;
import com.codeit.findex.indexData.repository.IndexDataRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IndexDataQueryService {

	private final IndexDataRepository indexDataRepository;

	public Slice<IndexData> getIndexDataList(Long indexInfoId, LocalDate startDate, LocalDate endDate,
		Long lastId, String sortField, String sortDirection, Integer size) {

		Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection)
			? Sort.Direction.DESC
			: Sort.Direction.ASC;

		String entitySortField = mapSortField(sortField);

		// size + 1 조회해서 hasNext 판단
		Pageable pageable = PageRequest.of(0, size + 1,
			Sort.by(direction, entitySortField).and(Sort.by(Sort.Direction.ASC, "id")));

		Slice<IndexData> resultSlice;

		if (lastId != null) {
			Optional<IndexData> lastItem = indexDataRepository.findById(lastId);
			if (lastItem.isPresent()) {
				LocalDate lastBaseDate = lastItem.get().getBaseDate();

				if (direction == Sort.Direction.DESC) {
					resultSlice = indexDataRepository.findIndexDataWithFiltersAfterIdDescSlice(
						indexInfoId, startDate, endDate, lastBaseDate, lastId, pageable);
				} else {
					resultSlice = indexDataRepository.findIndexDataWithFiltersAfterIdAscSlice(
						indexInfoId, startDate, endDate, lastBaseDate, lastId, pageable);
				}
			} else {
				resultSlice = indexDataRepository.findIndexDataWithFiltersSlice(
					indexInfoId, startDate, endDate, pageable);
			}
		} else {
			resultSlice = indexDataRepository.findIndexDataWithFiltersSlice(
				indexInfoId, startDate, endDate, pageable);
		}

		// 실제 데이터와 hasNext 값을 정확히 계산
		List<IndexData> content = resultSlice.getContent();
		boolean hasNext = content.size() > size;

		// 실제 반환할 데이터는 원래 size만큼만
		List<IndexData> actualContent = hasNext ? content.subList(0, size) : content;

		log.debug("🔍 무한스크롤 - 요청size: {}, 조회된개수: {}, 실제반환: {}, hasNext: {}, lastId: {}",
			size, content.size(), actualContent.size(), hasNext, lastId);

		// 정확한 Slice 객체 생성
		return new SliceImpl<>(actualContent, pageable, hasNext);
	}

	private String mapSortField(String apiSortField) {
		return switch (apiSortField) {
			case "baseDate", "date" -> "baseDate";           // 날짜
			case "closingPrice", "price" -> "closingPrice";  // 종가
			default -> "baseDate";                           // 기본값: 날짜
		};
	}

	// lastId 파라미터 제거 - 전체 조건에 맞는 총 개수만 반환
	public long getTotalCount(Long indexInfoId, LocalDate startDate, LocalDate endDate) {
		return indexDataRepository.countIndexDataWithFilters(indexInfoId, startDate, endDate);
	}
}