package com.codeit.findex.indexData.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codeit.findex.indexData.domain.PeriodType;
import com.codeit.findex.indexData.dto.IndexChartResponse;
import com.codeit.findex.indexData.service.IndexChartService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/index-data")
@RequiredArgsConstructor
public class IndexChartController {
	private final IndexChartService indexChartService;

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

		IndexChartResponse response = indexChartService.getIndexChartData(id, periodType);
		return ResponseEntity.ok(response);
	}
}