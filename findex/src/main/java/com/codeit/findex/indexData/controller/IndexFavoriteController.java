package com.codeit.findex.indexData.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codeit.findex.indexData.domain.PerformancePeriodType;
import com.codeit.findex.indexData.dto.IndexFavoritePerformance;
import com.codeit.findex.indexData.service.IndexFavoriteService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/index-data")
@RequiredArgsConstructor
public class IndexFavoriteController {

	private final IndexFavoriteService indexFavoriteService;

	@GetMapping("/performance/favorite")
	public ResponseEntity<List<IndexFavoritePerformance>> getFavoriteIndexPerformance(
		@RequestParam(value = "periodType") String periodTypeStr) {

		PerformancePeriodType periodType;
		try {
			periodType = PerformancePeriodType.valueOf(periodTypeStr.toUpperCase());
		} catch (IllegalArgumentException e) {
			periodType = PerformancePeriodType.DAILY;
		}

		List<IndexFavoritePerformance> performances = indexFavoriteService.getFavoriteIndexPerformance(periodType);
		return ResponseEntity.ok(performances);
	}
}
