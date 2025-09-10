package com.codeit.findex.indexData.service;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codeit.findex.indexData.domain.IndexData;
import com.codeit.findex.indexData.repository.IndexDataRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IndexDataQueryService {
	private final IndexDataRepository indexDataRepository;

	public Page<IndexData> getIndexDataList(Long indexInfoId, LocalDate startDate, LocalDate endDate,
		Long lastId, String sortField, String sortDirection, Integer size) {

		// 정렬 방향 설정
		Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection)
			? Sort.Direction.DESC
			: Sort.Direction.ASC;

		String entitySortField = mapSortField(sortField);

		// 보조 정렬로 ID 추가 (중복 값이 있을 때 일관성 보장)
		Sort sort = Sort.by(direction, entitySortField).and(Sort.by(Sort.Direction.ASC, "id"));
		Pageable pageable = PageRequest.of(0, size, sort);

		Page<IndexData> result;
		if (lastId != null) {
			// 커서 기반 조회 - 정렬 방향에 따라 다른 조건 사용
			if (direction == Sort.Direction.DESC) {
				result = indexDataRepository.findIndexDataWithFiltersAfterIdDesc(
					indexInfoId, startDate, endDate, lastId, pageable);
			} else {
				result = indexDataRepository.findIndexDataWithFiltersAfterIdAsc(
					indexInfoId, startDate, endDate, lastId, pageable);
			}
		} else {
			// 첫 페이지 조회
			result = indexDataRepository.findIndexDataWithFilters(
				indexInfoId, startDate, endDate, pageable);
		}

		return result;
	}

	private String mapSortField(String apiSortField) {
		return switch (apiSortField) {
			case "baseDate", "date" -> "baseDate";           // 날짜
			case "closingPrice", "price" -> "closingPrice";  // 종가
			default -> "baseDate";                           // 기본값: 날짜
		};
	}
}