package com.codeit.findex.indexData.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codeit.findex.indexData.domain.PerformancePeriodType;
import com.codeit.findex.indexData.dto.IndexPerformanceRankResponse;
import com.codeit.findex.indexData.service.IndexPerformanceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/index-data")
@RequiredArgsConstructor
public class IndexPerformanceController {

	private final IndexPerformanceService indexPerformanceService;

	// 지수 성과 랭킹 조회
	@GetMapping("/performance/rank")
	public ResponseEntity<List<IndexPerformanceRankResponse>> getPerformanceRanking(
		@RequestParam(value = "indexInfoId", required = false) Long indexInfoId,
		@RequestParam(value = "periodType", required = false) String periodTypeStr,
		@RequestParam(value = "limit", required = false) Integer limit) {

		try {
			PerformancePeriodType periodType = null;
			if (periodTypeStr != null && !periodTypeStr.isEmpty()) {
				try {
					periodType = PerformancePeriodType.valueOf(periodTypeStr.toUpperCase());
				} catch (IllegalArgumentException e) {
					throw new IllegalArgumentException("유효하지 않은 성과 기간 유형입니다: " + periodTypeStr);
				}
			}

			log.info("성과 랭킹 조회 요청: indexInfoId={}, periodType={}, limit={}", indexInfoId, periodType, limit);

			List<IndexPerformanceRankResponse> rankings =
				indexPerformanceService.getPerformanceRanking(indexInfoId, periodType, limit);

			return ResponseEntity.ok(rankings);
		} catch (IllegalArgumentException e) {
			log.error("성과 랭킹 조회 실패: {}", e.getMessage());
			throw e;
		} catch (Exception e) {
			log.error("성과 랭킹 조회 중 예상치 못한 오류 발생", e);
			throw new RuntimeException("성과 랭킹 조회 중 오류가 발생했습니다.");
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