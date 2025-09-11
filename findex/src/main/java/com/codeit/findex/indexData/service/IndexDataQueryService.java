package com.codeit.findex.indexData.service;

import java.time.LocalDate;
import java.util.Optional;

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

		Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection)
			? Sort.Direction.DESC
			: Sort.Direction.ASC;

		String entitySortField = mapSortField(sortField);

		Sort sort = Sort.by(direction, entitySortField).and(Sort.by(Sort.Direction.ASC, "id"));
		Pageable pageable = PageRequest.of(0, size, sort);

		Page<IndexData> result;
		if (lastId != null) {
			Optional<IndexData> lastItem = indexDataRepository.findById(lastId);
			if (lastItem.isPresent()) {
				LocalDate lastBaseDate = lastItem.get().getBaseDate();

				if (direction == Sort.Direction.DESC) {
					result = indexDataRepository.findIndexDataWithFiltersAfterIdDesc(
						indexInfoId, startDate, endDate, lastBaseDate, lastId, pageable);
				} else {
					result = indexDataRepository.findIndexDataWithFiltersAfterIdAsc(
						indexInfoId, startDate, endDate, lastBaseDate, lastId, pageable);
				}
			} else {
				result = indexDataRepository.findIndexDataWithFilters(
					indexInfoId, startDate, endDate, pageable);
			}
		} else {
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

	public long getTotalCount(Long indexInfoId, LocalDate startDate, LocalDate endDate) {
		return indexDataRepository.countIndexDataWithFilters(indexInfoId, startDate, endDate);
	}
}