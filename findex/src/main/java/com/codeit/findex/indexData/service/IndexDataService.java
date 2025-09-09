package com.codeit.findex.indexData.service;

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
	public Page<IndexData> getIndexDataList(Long indexInfoId, LocalDate startDate, LocalDate endDate,
		Long idAfter, String sortField, String sortDirection, int size) {

		// 정렬 방향 설정
		Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection)
			? Sort.Direction.DESC
			: Sort.Direction.ASC;

		// 정렬 필드 매핑 (API 스펙의 sortField를 entity 필드로 매핑)
		String entitySortField = mapSortField(sortField);

		Sort sort = Sort.by(direction, entitySortField);
		Pageable pageable = PageRequest.of(0, size, sort);

		return indexDataRepository.findIndexDataWithFilters(
			indexInfoId, startDate, endDate, idAfter, pageable);
	}

	private String mapSortField(String apiSortField) {
		// API 스펙의 정렬 필드를 엔티티 필드로 매핑
		switch (apiSortField) {
			case "indexClassification":
				return "indexInfo.indexClassification"; // Join 필드
			case "indexName":
				return "indexInfo.indexName"; // Join 필드
			case "employedItemsCount":
				return "indexInfo.employedItemsCount"; // Join 필드
			case "baseDate":
				return "baseDate";
			default:
				return "id"; // 기본값
		}
	}
}
