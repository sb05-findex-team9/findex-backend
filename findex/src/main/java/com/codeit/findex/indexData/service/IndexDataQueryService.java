package com.codeit.findex.indexData.service;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codeit.findex.indexData.domain.IndexData;
import com.codeit.findex.indexData.dto.IndexDataResponseDto;
import com.codeit.findex.indexData.repository.IndexDataRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IndexDataQueryService {

	private final IndexDataRepository indexDataRepository;

	public Page<IndexDataResponseDto> getIndexDataList(Long indexInfoId, LocalDate startDate, LocalDate endDate,
		int page, int size, String sortField, String sortDirection) {

		// 정렬 방향 설정
		Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection)
			? Sort.Direction.DESC
			: Sort.Direction.ASC;

		// 정렬 필드 매핑
		String entitySortField = mapSortField(sortField);

		// Pageable 생성 (id를 보조 정렬로 추가하여 일관된 정렬 보장)
		Pageable pageable = PageRequest.of(page, size,
			Sort.by(direction, entitySortField).and(Sort.by(Sort.Direction.ASC, "id")));

		log.debug("🔍 OFFSET 기반 쿼리 - page: {}, size: {}, sortField: {}, direction: {}",
			page, size, entitySortField, direction);

		// 페이지네이션된 데이터 조회
		Page<IndexData> indexDataPage = indexDataRepository.findIndexDataWithFilters(
			indexInfoId, startDate, endDate, pageable);

		// DTO 변환
		Page<IndexDataResponseDto> result = indexDataPage.map(IndexDataResponseDto::from);

		log.debug("✅ 조회 완료 - 조회된 개수: {}, 전체 개수: {}, 전체 페이지: {}, hasNext: {}",
			result.getContent().size(), result.getTotalElements(), result.getTotalPages(), result.hasNext());

		return result;
	}

	private String mapSortField(String apiSortField) {
		return switch (apiSortField) {
			case "baseDate", "date" -> "baseDate";           // 날짜
			case "closingPrice", "price" -> "closingPrice";  // 종가
			case "marketPrice" -> "marketPrice";             // 시가
			case "highPrice" -> "highPrice";                 // 고가
			case "lowPrice" -> "lowPrice";                   // 저가
			default -> "baseDate";                           // 기본값: 날짜
		};
	}

	public long getTotalCount(Long indexInfoId, LocalDate startDate, LocalDate endDate) {
		return indexDataRepository.countIndexDataWithFilters(indexInfoId, startDate, endDate);
	}
}