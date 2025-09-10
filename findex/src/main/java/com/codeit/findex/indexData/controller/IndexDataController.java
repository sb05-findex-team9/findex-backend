package com.codeit.findex.indexData.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codeit.findex.indexData.domain.IndexData;
import com.codeit.findex.indexData.domain.PeriodType;
import com.codeit.findex.indexData.dto.IndexChartResponse;
import com.codeit.findex.indexData.dto.IndexDataRequestDto;
import com.codeit.findex.indexData.dto.IndexDataResponseDto;
import com.codeit.findex.indexData.dto.PagedResponseDto;
import com.codeit.findex.indexData.service.IndexDataService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/index-data")
@RequiredArgsConstructor
public class IndexDataController {
	private final IndexDataService indexDataService;

	// 지수 데이터 목록 조회
	@GetMapping
	public ResponseEntity<PagedResponseDto<IndexDataResponseDto>> getIndexDataList(
		@RequestParam(required = false) Long indexInfoId,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
		@RequestParam(required = false) String cursor,
		@RequestParam(required = false) String idAfter,
		@RequestParam(required = false, defaultValue = "baseDate") String sortField,
		@RequestParam(required = false, defaultValue = "asc") String sortDirection,
		@RequestParam(required = false, defaultValue = "10") Integer size) {

		Long lastId = null;
		if (cursor != null && !cursor.trim().isEmpty()) {
			try {
				lastId = Long.parseLong(cursor);
			} catch (NumberFormatException ignored) {
			}
		} else if (idAfter != null && !idAfter.trim().isEmpty()) {
			try {
				lastId = Long.parseLong(idAfter);
			} catch (NumberFormatException ignored) {
			}
		}

		Page<IndexData> indexDataPage = indexDataService.getIndexDataList(
			indexInfoId, startDate, endDate, lastId, sortField, sortDirection, size);

		List<IndexDataResponseDto> content = indexDataPage.getContent().stream()
			.map(IndexDataResponseDto::from)
			.collect(Collectors.toList());

		String nextCursor = null;
		String nextIdAfter = null;
		if (indexDataPage.hasNext() && !content.isEmpty()) {
			Long lastIdInPage = content.get(content.size() - 1).getId();
			nextCursor = lastIdInPage.toString();
			nextIdAfter = lastIdInPage.toString();
		}

		PagedResponseDto<IndexDataResponseDto> response = PagedResponseDto.<IndexDataResponseDto>builder()
			.content(content)
			.nextCursor(nextCursor)
			.nextIdAfter(nextIdAfter)
			.size(content.size())
			.totalElements((int)indexDataPage.getTotalElements())
			.hasNext(indexDataPage.hasNext())
			.build();

		return ResponseEntity.ok(response);
	}

	// 지수 데이터 등록
	@PostMapping
	public ResponseEntity<IndexDataResponseDto> createIndexData(@RequestBody IndexDataRequestDto requestDto) {
		IndexData createdIndexData = indexDataService.createIndexData(requestDto);
		IndexDataResponseDto response = IndexDataResponseDto.from(createdIndexData);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	// 지수 차트 조회
	@GetMapping("/{id}")
	public ResponseEntity<IndexChartResponse> getIndexChartData(
		@PathVariable("id") Long id,
		@RequestParam(value = "periodType", required = false) String periodTypeStr) {

		PeriodType periodType = null;
		if (periodTypeStr != null && !periodTypeStr.isEmpty()) {
			try {
				periodType = PeriodType.valueOf(periodTypeStr.toUpperCase());
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("유효하지 않은 기간 유형입니다: " + periodTypeStr);
			}
		}

		IndexChartResponse response = indexDataService.getIndexChartData(id, periodType);
		return ResponseEntity.ok(response);
	}
}
