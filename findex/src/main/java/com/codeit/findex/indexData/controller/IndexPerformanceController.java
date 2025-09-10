package com.codeit.findex.indexData.controller;

import com.codeit.findex.indexData.domain.PerformancePeriodType;
import com.codeit.findex.indexData.dto.IndexPerformanceRankResponse;
import com.codeit.findex.indexData.service.IndexPerformanceService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/index-data")
@RequiredArgsConstructor
public class IndexPerformanceController {

	private final IndexPerformanceService indexPerformanceService;

	/**
	 * 지수 성과 랭킹 조회
	 *
	 * @param indexInfoId 지수 정보 ID (선택)
	 * @param periodTypeStr 성과 기간 유형 (DAILY, WEEKLY, MONTHLY)
	 * @param limit 최대 랭킹 수
	 * @return 성과 랭킹 목록
	 */
	@GetMapping("/performance/rank")
	public ResponseEntity<List<IndexPerformanceRankResponse>> getPerformanceRanking(
		@RequestParam(value = "indexInfoId", required = false) Long indexInfoId,
		@RequestParam(value = "periodType", required = false) String periodTypeStr,
		@RequestParam(value = "limit", required = false) Integer limit) {

		// 성과 기간 유형 파싱
		PerformancePeriodType periodType = null;
		if (periodTypeStr != null && !periodTypeStr.isEmpty()) {
			try {
				periodType = PerformancePeriodType.valueOf(periodTypeStr.toUpperCase());
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("유효하지 않은 성과 기간 유형입니다: " + periodTypeStr);
			}
		}

		// 서비스 호출
		List<IndexPerformanceRankResponse> rankings =
			indexPerformanceService.getPerformanceRanking(indexInfoId, periodType, limit);

		return ResponseEntity.ok(rankings);
	}
}