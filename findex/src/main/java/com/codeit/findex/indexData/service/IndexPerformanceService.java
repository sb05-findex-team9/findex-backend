package com.codeit.findex.indexData.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codeit.findex.indexData.domain.IndexData;
import com.codeit.findex.indexData.domain.PerformancePeriodType;
import com.codeit.findex.indexData.dto.IndexPerformanceDto;
import com.codeit.findex.indexData.dto.IndexPerformanceRankResponse;
import com.codeit.findex.indexData.repository.IndexDataRepository;
import com.codeit.findex.indexInfo.domain.IndexInfo;
import com.codeit.findex.indexInfo.repository.IndexInfoRepository;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

		if (periodType == null)
			periodType = PerformancePeriodType.DAILY;
		if (limit == null || limit <= 0)
			limit = 10;

		log.info("성과 랭킹 조회 시작 - indexInfoId: {}, periodType: {}, limit: {}", indexInfoId, periodType, limit);

		LocalDate latestDate = findLatestDateWithSufficientData(limit);
		LocalDate compareDate = calculateCompareDate(latestDate, periodType);

		log.info("날짜 설정 완료 - 최신날짜: {}, 비교날짜: {}", latestDate, compareDate);

		if (indexInfoId != null) {
			return getSpecificIndexRanking(indexInfoId, latestDate, compareDate, periodType);
		} else {
			return getAllIndexRankingBatch(latestDate, compareDate, periodType, limit);
		}
	}

	private LocalDate findLatestDateWithSufficientData(Integer requiredCount) {
		LocalDate maxDate = indexDataRepository.findMaxBaseDate()
			.orElseThrow(() -> new IllegalStateException("거래 데이터가 없습니다."));

		for (int i = 0; i < 10; i++) {
			LocalDate checkDate = maxDate.minusDays(i);
			// 엔티티 로드 없이 개수만 조회
			long dataCount = indexDataRepository.countByBaseDateAndClosingPriceIsNotNull(checkDate);
			log.debug("날짜 {} 데이터 개수: {}", checkDate, dataCount);
			if (dataCount >= Math.min(requiredCount, 10)) {
				log.info("충분한 데이터가 있는 날짜 선택: {} ({}개)", checkDate, dataCount);
				return checkDate;
			}
		}

		log.warn("충분한 데이터가 있는 날짜를 찾지 못함. 최신 날짜 사용: {}", maxDate);
		return maxDate;
	}

	private List<IndexPerformanceRankResponse> getAllIndexRankingBatch(
		LocalDate latestDate,
		LocalDate compareDate,
		PerformancePeriodType periodType,
		Integer limit) {

		log.info("전체 랭킹 조회 시작 - limit: {}", limit);

		// 1. Weekly/Monthly의 경우 평균 계산을 위한 모든 필요 데이터를 한 번에 조회
		List<IndexPerformanceData> performanceDataList;

		if (periodType == PerformancePeriodType.DAILY) {
			performanceDataList = getDailyPerformanceData(latestDate, compareDate);
		} else {
			performanceDataList = getPeriodPerformanceData(latestDate, compareDate, periodType);
		}

		// 2. 등락률 기준으로 정렬하여 상위 limit개 선택
		List<IndexPerformanceData> topPerformers = performanceDataList.stream()
			.sorted((a, b) -> Float.compare(b.getFluctuationRate(), a.getFluctuationRate()))
			.limit(limit)
			.collect(Collectors.toList());

		// 3. 응답 객체 생성
		List<IndexPerformanceRankResponse> result = new ArrayList<>();
		for (int i = 0; i < topPerformers.size(); i++) {
			result.add(createRankResponse(topPerformers.get(i), i + 1));
		}

		log.info("최종 반환 결과 개수: {}", result.size());
		return result;
	}

	private List<IndexPerformanceData> getDailyPerformanceData(LocalDate latestDate, LocalDate compareDate) {
		// 두 날짜의 모든 데이터를 한 번에 조회 (이미 JOIN FETCH로 최적화됨)
		List<LocalDate> dates = Arrays.asList(latestDate, compareDate);
		List<IndexData> allData = indexDataRepository.findAllByBaseDateInWithIndexInfo(dates);

		// 날짜별, 지수별로 그룹화
		Map<LocalDate, Map<Long, IndexData>> dataByDate = allData.stream()
			.collect(Collectors.groupingBy(
				IndexData::getBaseDate,
				Collectors.toMap(
					data -> data.getIndexInfo().getId(),
					data -> data,
					(existing, replacement) -> existing
				)
			));

		Map<Long, IndexData> currentDataMap = dataByDate.getOrDefault(latestDate, Collections.emptyMap());
		Map<Long, IndexData> compareDataMap = dataByDate.getOrDefault(compareDate, Collections.emptyMap());

		return currentDataMap.entrySet().stream()
			.map(entry -> {
				Long id = entry.getKey();
				IndexData currentData = entry.getValue();
				IndexData compareData = compareDataMap.get(id);

				if (currentData.getClosingPrice() == null || compareData == null
					|| compareData.getClosingPrice() == null) {
					return null;
				}

				float currentPrice = currentData.getClosingPrice().floatValue();
				float comparePrice = compareData.getClosingPrice().floatValue();

				if (currentPrice == 0f || comparePrice == 0f) {
					return null;
				}

				float versus = currentPrice - comparePrice;
				float fluctuationRate = calculateFluctuationRate(currentPrice, comparePrice);

				return IndexPerformanceData.builder()
					.indexInfo(currentData.getIndexInfo()) // 이미 JOIN FETCH된 데이터
					.currentPrice(currentPrice)
					.beforePrice(comparePrice)
					.versus(versus)
					.fluctuationRate(fluctuationRate)
					.build();
			})
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}

	private List<IndexPerformanceData> getPeriodPerformanceData(LocalDate latestDate, LocalDate compareDate,
		PerformancePeriodType periodType) {
		// 1. 최신 날짜의 모든 지수 데이터 조회 (현재가)
		List<IndexData> currentDataList = indexDataRepository.findAllByBaseDateWithIndexInfo(latestDate);

		// 2. 모든 지수 ID 추출
		Set<Long> indexInfoIds = currentDataList.stream()
			.map(data -> data.getIndexInfo().getId())
			.collect(Collectors.toSet());

		// 3. 배치로 모든 지수의 평균값 조회 - 한 번의 쿼리로 처리
		Map<Long, Double> averagePriceMap = indexDataRepository.findAverageClosingPricesByIndexInfosBetween(
			new ArrayList<>(indexInfoIds), compareDate, latestDate
		);

		// 4. 성과 데이터 계산
		return currentDataList.stream()
			.map(currentData -> {
				Long indexInfoId = currentData.getIndexInfo().getId();

				if (currentData.getClosingPrice() == null) {
					return null;
				}

				float currentPrice = currentData.getClosingPrice().floatValue();
				Double averagePrice = averagePriceMap.get(indexInfoId);

				if (averagePrice == null || currentPrice == 0f || averagePrice == 0.0) {
					return null;
				}

				float comparePrice = averagePrice.floatValue();
				float versus = currentPrice - comparePrice;
				float fluctuationRate = calculateFluctuationRate(currentPrice, comparePrice);

				return IndexPerformanceData.builder()
					.indexInfo(currentData.getIndexInfo()) // 이미 JOIN FETCH된 데이터
					.currentPrice(currentPrice)
					.beforePrice(comparePrice)
					.versus(versus)
					.fluctuationRate(fluctuationRate)
					.build();
			})
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}

	private List<IndexPerformanceRankResponse> getSpecificIndexRanking(
		Long indexInfoId,
		LocalDate latestDate,
		LocalDate compareDate,
		PerformancePeriodType periodType) {

		log.info("특정 지수 랭킹 조회 시작 - indexInfoId: {}", indexInfoId);

		// IndexInfo와 IndexData를 함께 조회하여 N+1 문제 방지
		IndexData currentIndexData = indexDataRepository
			.findByIndexInfoIdAndBaseDateWithIndexInfo(indexInfoId, latestDate)
			.orElseThrow(() -> new IllegalArgumentException("해당 날짜의 지수 데이터가 없습니다."));

		IndexInfo indexInfo = currentIndexData.getIndexInfo(); // 이미 JOIN FETCH된 데이터

		float currentPrice = currentIndexData.getClosingPrice() != null ?
			currentIndexData.getClosingPrice().floatValue() : 0f;

		float comparePrice;
		if (periodType == PerformancePeriodType.DAILY) {
			comparePrice = indexDataRepository
				.findByIndexInfoIdAndBaseDate(indexInfoId, compareDate)
				.map(d -> d.getClosingPrice() != null ? d.getClosingPrice().floatValue() : 0f)
				.orElse(0f);
		} else {
			comparePrice = indexDataRepository
				.findAverageClosingPriceByIndexInfoBetween(indexInfoId, compareDate, latestDate)
				.orElse(0.0)
				.floatValue();
		}

		IndexPerformanceData perfData = IndexPerformanceData.builder()
			.indexInfo(indexInfo)
			.currentPrice(currentPrice)
			.beforePrice(comparePrice)
			.versus(currentPrice - comparePrice)
			.fluctuationRate(calculateFluctuationRate(currentPrice, comparePrice))
			.build();

		int rank = calculateRankOptimized(perfData.getFluctuationRate(), latestDate, compareDate, periodType);

		log.info("특정 지수 랭킹 계산 완료 - 지수명: {}, 등락률: {}%, 순위: {}",
			indexInfo.getIndexName(), perfData.getFluctuationRate(), rank);

		return List.of(createRankResponse(perfData, rank));
	}

	private int calculateRankOptimized(float targetFluctuationRate, LocalDate latestDate, LocalDate compareDate,
		PerformancePeriodType periodType) {
		if (periodType == PerformancePeriodType.DAILY) {
			return calculateDailyRank(targetFluctuationRate, latestDate, compareDate);
		} else {
			return calculatePeriodRank(targetFluctuationRate, latestDate, compareDate);
		}
	}

	private int calculateDailyRank(float targetFluctuationRate, LocalDate latestDate, LocalDate compareDate) {
		// 두 날짜의 모든 데이터를 한 번에 조회
		List<LocalDate> dates = Arrays.asList(latestDate, compareDate);
		List<IndexData> allData = indexDataRepository.findAllByBaseDateInWithIndexInfo(dates);

		Map<LocalDate, Map<Long, IndexData>> dataByDate = allData.stream()
			.collect(Collectors.groupingBy(
				IndexData::getBaseDate,
				Collectors.toMap(
					data -> data.getIndexInfo().getId(),
					data -> data,
					(existing, replacement) -> existing
				)
			));

		Map<Long, IndexData> currentDataMap = dataByDate.getOrDefault(latestDate, Collections.emptyMap());
		Map<Long, IndexData> compareDataMap = dataByDate.getOrDefault(compareDate, Collections.emptyMap());

		int rank = 1;
		for (Map.Entry<Long, IndexData> entry : currentDataMap.entrySet()) {
			IndexData currentData = entry.getValue();
			IndexData compareData = compareDataMap.get(entry.getKey());

			if (currentData.getClosingPrice() != null && compareData != null && compareData.getClosingPrice() != null) {
				float fluctuationRate = calculateFluctuationRate(
					currentData.getClosingPrice().floatValue(),
					compareData.getClosingPrice().floatValue()
				);
				if (fluctuationRate > targetFluctuationRate) {
					rank++;
				}
			}
		}

		return rank;
	}

	private int calculatePeriodRank(float targetFluctuationRate, LocalDate latestDate, LocalDate compareDate) {
		// 현재 날짜의 모든 데이터 조회 - DTO 프로젝션으로 N+1 완전 해결
		List<IndexPerformanceDto> currentDataList = indexDataRepository.findAllByBaseDateWithIndexInfoDto(latestDate);

		Set<Long> indexInfoIds = currentDataList.stream()
			.map(IndexPerformanceDto::getIndexInfoId)
			.collect(Collectors.toSet());

		// 배치로 모든 지수의 평균값 조회
		Map<Long, Double> averagePriceMap = indexDataRepository.findAverageClosingPricesByIndexInfosBetween(
			new ArrayList<>(indexInfoIds), compareDate, latestDate
		);

		int rank = 1;
		for (IndexPerformanceDto currentData : currentDataList) {
			if (currentData.getClosingPrice() == null)
				continue;

			Long indexInfoId = currentData.getIndexInfoId();
			Double averagePrice = averagePriceMap.get(indexInfoId);

			if (averagePrice != null && averagePrice > 0) {
				float fluctuationRate = calculateFluctuationRate(
					currentData.getClosingPriceAsFloat(),
					averagePrice.floatValue()
				);
				if (fluctuationRate > targetFluctuationRate) {
					rank++;
				}
			}
		}

		return rank;
	}

	private LocalDate calculateCompareDate(LocalDate baseDate, PerformancePeriodType periodType) {
		LocalDate compareDate = switch (periodType) {
			case DAILY -> baseDate.minusDays(1);
			case WEEKLY -> baseDate.minusWeeks(1);
			case MONTHLY -> baseDate.minusMonths(1);
		};

		if (compareDate.getDayOfWeek() == DayOfWeek.SATURDAY)
			compareDate = compareDate.minusDays(1);
		else if (compareDate.getDayOfWeek() == DayOfWeek.SUNDAY)
			compareDate = compareDate.minusDays(2);

		return compareDate;
	}

	private float calculateFluctuationRate(float currentPrice, float comparePrice) {
		if (comparePrice == 0f)
			return 0f;
		return ((currentPrice - comparePrice) / comparePrice) * 100f;
	}

	private IndexPerformanceRankResponse createRankResponse(IndexPerformanceData perfData, int rank) {
		IndexInfo indexInfo = perfData.getIndexInfo();

		return IndexPerformanceRankResponse.builder()
			.performance(IndexPerformanceRankResponse.Performance.builder()
				.indexInfoId(indexInfo.getId())
				.indexClassification(indexInfo.getIndexClassification())
				.indexName(indexInfo.getIndexName())
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
		private float currentPrice;
		private float beforePrice;
		private float versus;
		private float fluctuationRate;
	}
}