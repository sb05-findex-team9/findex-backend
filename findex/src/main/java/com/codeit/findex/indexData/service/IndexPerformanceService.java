package com.codeit.findex.indexData.service;

import com.codeit.findex.indexData.domain.IndexData;
import com.codeit.findex.indexData.domain.PerformancePeriodType;
import com.codeit.findex.indexData.dto.IndexPerformanceRankResponse;
import com.codeit.findex.indexData.repository.IndexDataRepository;
import com.codeit.findex.indexInfo.domain.IndexInfo;
import com.codeit.findex.indexInfo.repository.IndexInfoRepository;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IndexPerformanceService {

	private final IndexDataRepository indexDataRepository;
	private final IndexInfoRepository indexInfoRepository;

	public List<IndexPerformanceRankResponse> getPerformanceRanking(
		Long indexInfoId,
		PerformancePeriodType periodType,
		Integer limit) {

		// 기본값 설정
		if (periodType == null) {
			periodType = PerformancePeriodType.DAILY;
		}
		if (limit == null || limit <= 0) {
			limit = 10;
		}

		// 최신 거래일 조회
		LocalDate latestDate = indexDataRepository.findMaxBaseDate()
			.orElseThrow(() -> new IllegalStateException("거래 데이터가 없습니다."));

		LocalDate compareDate = calculateCompareDate(latestDate, periodType);

		compareDate = findNearestTradingDate(compareDate, latestDate);

		List<IndexPerformanceRankResponse> rankings;

		if (indexInfoId != null) {
			// 특정 지수의 랭킹 조회
			rankings = getSpecificIndexRanking(indexInfoId, latestDate, compareDate, periodType);
		} else {
			rankings = getAllIndexRankingOptimized(latestDate, compareDate, limit);
		}

		return rankings;
	}

	private LocalDate calculateCompareDate(LocalDate baseDate, PerformancePeriodType periodType) {
		return switch (periodType) {
			case DAILY -> getPreviousTradingDay(baseDate);
			case WEEKLY -> getPreviousTradingDay(baseDate.minusWeeks(1));
			case MONTHLY -> getPreviousTradingDay(baseDate.minusMonths(1));
		};
	}

	private LocalDate getPreviousTradingDay(LocalDate date) {
		LocalDate previousDay = date;

		if (previousDay.getDayOfWeek() == DayOfWeek.SATURDAY) {
			previousDay = previousDay.minusDays(1);
		} else if (previousDay.getDayOfWeek() == DayOfWeek.SUNDAY) {
			previousDay = previousDay.minusDays(2);
		}

		return previousDay;
	}

	private LocalDate findNearestTradingDate(LocalDate targetDate, LocalDate maxDate) {
		LocalDate searchDate = targetDate;

		for (int i = 0; i < 10; i++) {
			if (searchDate.isAfter(maxDate)) {
				searchDate = searchDate.minusDays(1);
				continue;
			}

			List<IndexData> dataList = indexDataRepository.findAllByBaseDateWithIndexInfo(searchDate);
			if (!dataList.isEmpty()) {
				return searchDate;
			}

			searchDate = searchDate.minusDays(1);
		}

		return targetDate;
	}

	private List<IndexPerformanceRankResponse> getAllIndexRankingOptimized(
		LocalDate latestDate,
		LocalDate compareDate,
		Integer limit) {

		Map<Long, IndexData> currentDataMap = indexDataRepository
			.findAllByBaseDateWithIndexInfo(latestDate)
			.stream()
			.collect(Collectors.toMap(data -> data.getIndexInfo().getId(), data -> data));

		Map<Long, IndexData> compareDataMap = indexDataRepository
			.findAllByBaseDateWithIndexInfo(compareDate)
			.stream()
			.collect(Collectors.toMap(data -> data.getIndexInfo().getId(), data -> data));

		List<IndexPerformanceData> performanceDataList = new ArrayList<>();

		for (Map.Entry<Long, IndexData> entry : currentDataMap.entrySet()) {
			Long indexInfoId = entry.getKey();
			IndexData currentData = entry.getValue();
			IndexData compareData = compareDataMap.get(indexInfoId);

			if (compareData != null && currentData.getClosingPrice() != null && compareData.getClosingPrice() != null) {
				IndexPerformanceData perfData = calculatePerformance(
					currentData.getIndexInfo(), currentData, compareData);
				performanceDataList.add(perfData);
			}
		}

		// 등락률 기준 내림차순 정렬
		performanceDataList.sort((a, b) ->
			Float.compare(b.getFluctuationRate(), a.getFluctuationRate()));

		// 상위 N개만 선택하고 랭킹 부여
		return performanceDataList.stream()
			.limit(limit)
			.map(perfData -> {
				int rank = performanceDataList.indexOf(perfData) + 1;
				return createRankResponse(perfData, rank);
			})
			.collect(Collectors.toList());
	}

	private List<IndexPerformanceRankResponse> getSpecificIndexRanking(
		Long indexInfoId,
		LocalDate latestDate,
		LocalDate compareDate,
		PerformancePeriodType periodType) {

		IndexInfo indexInfo = indexInfoRepository.findById(indexInfoId)
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지수 정보입니다."));

		// 해당 지수의 최신 데이터 조회
		Optional<IndexData> currentDataOpt = indexDataRepository
			.findByIndexInfoIdAndBaseDate(indexInfoId, latestDate);

		if (currentDataOpt.isEmpty()) {
			// 최신 거래일에 데이터가 없으면 가장 최근 데이터 조회
			Optional<LocalDate> maxDate = indexDataRepository
				.findMaxBaseDateByIndexInfoId(indexInfoId);
			if (maxDate.isEmpty()) {
				throw new IllegalArgumentException("해당 지수의 거래 데이터가 없습니다.");
			}
			latestDate = maxDate.get();
			compareDate = calculateCompareDate(latestDate, periodType);
			compareDate = findNearestTradingDate(compareDate, latestDate);
			currentDataOpt = indexDataRepository.findByIndexInfoIdAndBaseDate(indexInfoId, latestDate);
		}

		Optional<IndexData> compareDataOpt = indexDataRepository
			.findByIndexInfoIdAndBaseDate(indexInfoId, compareDate);

		if (currentDataOpt.isEmpty() || compareDataOpt.isEmpty()) {
			throw new IllegalArgumentException("성과 계산을 위한 데이터가 부족합니다.");
		}

		IndexPerformanceData perfData = calculatePerformance(
			indexInfo, currentDataOpt.get(), compareDataOpt.get());

		int rank = calculateRankAmongAllOptimized(perfData.getFluctuationRate(), latestDate, compareDate);

		return List.of(createRankResponse(perfData, rank));
	}

	private int calculateRankAmongAllOptimized(float targetFluctuationRate,
		LocalDate latestDate,
		LocalDate compareDate) {

		Map<Long, IndexData> currentDataMap = indexDataRepository
			.findAllByBaseDateWithIndexInfo(latestDate)
			.stream()
			.collect(Collectors.toMap(data -> data.getIndexInfo().getId(), data -> data));

		Map<Long, IndexData> compareDataMap = indexDataRepository
			.findAllByBaseDateWithIndexInfo(compareDate)
			.stream()
			.collect(Collectors.toMap(data -> data.getIndexInfo().getId(), data -> data));

		int rank = 1;

		for (Map.Entry<Long, IndexData> entry : currentDataMap.entrySet()) {
			Long indexInfoId = entry.getKey();
			IndexData currentData = entry.getValue();
			IndexData compareData = compareDataMap.get(indexInfoId);

			if (compareData != null && currentData.getClosingPrice() != null && compareData.getClosingPrice() != null) {
				float fluctuationRate = calculateFluctuationRate(
					currentData.getClosingPrice(),
					compareData.getClosingPrice());

				if (fluctuationRate > targetFluctuationRate) {
					rank++;
				}
			}
		}

		return rank;
	}

	private IndexPerformanceData calculatePerformance(
		IndexInfo indexInfo,
		IndexData currentData,
		IndexData compareData) {

		BigDecimal currentPrice = currentData.getClosingPrice();
		BigDecimal beforePrice = compareData.getClosingPrice();

		if (currentPrice == null || beforePrice == null || beforePrice.compareTo(BigDecimal.ZERO) == 0) {
			return IndexPerformanceData.builder()
				.indexInfo(indexInfo)
				.currentPrice(currentPrice != null ? currentPrice.floatValue() : 0f)
				.beforePrice(beforePrice != null ? beforePrice.floatValue() : 0f)
				.versus(0f)
				.fluctuationRate(0f)
				.build();
		}

		// 전일 대비 = 현재가 - 이전가
		BigDecimal versus = currentPrice.subtract(beforePrice);

		// 등락률 = (현재가 - 이전가) / 이전가 * 100
		float fluctuationRate = calculateFluctuationRate(currentPrice, beforePrice);

		return IndexPerformanceData.builder()
			.indexInfo(indexInfo)
			.currentPrice(currentPrice.floatValue())
			.beforePrice(beforePrice.floatValue())
			.versus(versus.floatValue())
			.fluctuationRate(fluctuationRate)
			.build();
	}

	private float calculateFluctuationRate(BigDecimal currentPrice, BigDecimal beforePrice) {
		if (beforePrice.compareTo(BigDecimal.ZERO) == 0) {
			return 0f;
		}

		return currentPrice.subtract(beforePrice)
			.divide(beforePrice, 4, RoundingMode.HALF_UP)
			.multiply(BigDecimal.valueOf(100))
			.floatValue();
	}

	private IndexPerformanceRankResponse createRankResponse(
		IndexPerformanceData perfData,
		int rank) {

		return IndexPerformanceRankResponse.builder()
			.performance(IndexPerformanceRankResponse.Performance.builder()
				.indexInfoId(perfData.getIndexInfo().getId())
				.indexClassification(perfData.getIndexInfo().getIndexClassification())
				.indexName(perfData.getIndexInfo().getIndexName())
				.versus(perfData.getVersus())
				.fluctuationRate(perfData.getFluctuationRate())
				.currentPrice(perfData.getCurrentPrice())
				.beforePrice(perfData.getBeforePrice())
				.build())
			.rank(rank)
			.build();
	}

	@Getter
	@Builder
	private static class IndexPerformanceData {
		private IndexInfo indexInfo;
		private Float currentPrice;
		private Float beforePrice;
		private Float versus;
		private Float fluctuationRate;
	}
}