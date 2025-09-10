package com.codeit.findex.indexData.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codeit.findex.indexData.domain.IndexData;
import com.codeit.findex.indexData.domain.PerformancePeriodType;
import com.codeit.findex.indexData.dto.IndexFavoritePerformance;
import com.codeit.findex.indexData.repository.IndexDataRepository;
import com.codeit.findex.indexInfo.domain.IndexInfo;
import com.codeit.findex.indexInfo.repository.IndexInfoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IndexFavoriteService {

	private final IndexInfoRepository indexInfoRepository;
	private final IndexDataRepository indexDataRepository;

	public List<IndexFavoritePerformance> getFavoriteIndexPerformance(PerformancePeriodType periodType) {
		List<IndexInfo> favoriteIndexes = indexInfoRepository.findByFavoriteTrue();

		LocalDate currentDate = LocalDate.now();
		LocalDate compareDate = getCompareDate(currentDate, periodType);

		return favoriteIndexes.stream()
			.map(indexInfo -> calculatePerformance(indexInfo, currentDate, compareDate))
			.collect(Collectors.toList());
	}

	private IndexFavoritePerformance calculatePerformance(IndexInfo indexInfo, LocalDate currentDate, LocalDate compareDate) {
		IndexData currentData = indexDataRepository
			.findTopByIndexInfoOrderByBaseDateDesc(indexInfo)
			.orElse(null);

		IndexData compareData = indexDataRepository
			.findTopByIndexInfoAndBaseDateLessThanEqualOrderByBaseDateDesc(indexInfo, compareDate)
			.orElse(null);

		if (currentData == null || compareData == null) {
			return IndexFavoritePerformance.of(indexInfo, BigDecimal.ZERO, BigDecimal.ZERO,
				BigDecimal.ZERO, BigDecimal.ZERO);
		}

		BigDecimal currentPrice = currentData.getClosingPrice();
		BigDecimal beforePrice = compareData.getClosingPrice();

		if (currentPrice == null || beforePrice == null || beforePrice.compareTo(BigDecimal.ZERO) == 0) {
			return IndexFavoritePerformance.of(indexInfo, BigDecimal.ZERO, BigDecimal.ZERO,
				currentPrice != null ? currentPrice : BigDecimal.ZERO,
				beforePrice != null ? beforePrice : BigDecimal.ZERO);
		}

		BigDecimal versus = currentPrice.subtract(beforePrice);

		BigDecimal fluctuationRate = versus.divide(beforePrice, 4, RoundingMode.HALF_UP)
			.multiply(BigDecimal.valueOf(100));

		return IndexFavoritePerformance.of(indexInfo, versus, fluctuationRate, currentPrice, beforePrice);
	}

	private LocalDate getCompareDate(LocalDate currentDate, PerformancePeriodType periodType) {
		return switch (periodType) {
			case DAILY -> currentDate.minusDays(1);
			case WEEKLY -> currentDate.minusWeeks(1);
			case MONTHLY -> currentDate.minusMonths(1);
		};
	}
}