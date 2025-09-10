package com.codeit.findex.indexData.service;

import static java.time.LocalDate.*;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import com.codeit.findex.indexData.domain.IndexData;
import com.codeit.findex.indexData.repository.IndexDataRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequestMapping("/api/index-data")
@RequiredArgsConstructor
public class IndexDataService {
	private final IndexDataRepository indexDataRepository;

	// 지수 데이터 목록 조회
	public Page<IndexData> getIndexDataList(Integer indexInfoId, String startDate, String endDate,
		Integer idAfter, String sortField, String sortDirection, Integer size) {

		LocalDate startLocalDate = parseDate(startDate);
		LocalDate endLocalDate = parseDate(endDate);

		// 정렬 방향 설정
		Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection)
			? Sort.Direction.DESC
			: Sort.Direction.ASC;

		String entitySortField = mapSortField(sortField);

		Sort sort = Sort.by(direction, entitySortField);
		Pageable pageable = PageRequest.of(0, size, sort);

		return indexDataRepository.findIndexDataWithFilters(
			indexInfoId, startLocalDate, endLocalDate, idAfter, pageable);
	}

	private LocalDate parseDate(String dateString) {
		if (dateString == null || dateString.trim().isEmpty()) {
			return null;
		}
		try {
			return LocalDate.parse(dateString);
		} catch (Exception e) {
			return null;
		}
	}

	private String mapSortField(String apiSortField) {
		return switch (apiSortField) {
			case "indexClassification" -> "indexInfo.indexClassification"; // Join 필드
			case "indexName" -> "indexInfo.indexName"; // Join 필드
			case "employedItemsCount" -> "indexInfo.employedItemsCount"; // Join 필드
			case "baseDate" -> "baseDate";
			default -> "id"; // 기본값
		};
	}
}
