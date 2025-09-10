package com.codeit.findex.indexData.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codeit.findex.indexData.domain.PeriodType;
import com.codeit.findex.indexData.dto.IndexChartResponse;
import com.codeit.findex.indexData.service.IndexChartService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

		try {
			PeriodType periodType = null;
			if (periodTypeStr != null && !periodTypeStr.isEmpty()) {
				try {
					periodType = PeriodType.valueOf(periodTypeStr.toUpperCase());
				} catch (IllegalArgumentException e) {
					throw new IllegalArgumentException("유효하지 않은 기간 유형입니다: " + periodTypeStr);
				}
			}

			log.info("차트 데이터 조회 요청: indexInfoId={}, periodType={}", id, periodType);
			IndexChartResponse response = indexChartService.getIndexChartData(id, periodType);
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException e) {
			log.error("차트 데이터 조회 실패: {}", e.getMessage());
			throw e;
		} catch (Exception e) {
			log.error("차트 데이터 조회 중 예상치 못한 오류 발생", e);
			throw new RuntimeException("차트 데이터 조회 중 오류가 발생했습니다.");
		}
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
		return ResponseEntity.badRequest().body(e.getMessage());
	}

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
		return ResponseEntity.internalServerError().body(e.getMessage());
	}
}