package com.codeit.findex.indexData.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codeit.findex.indexData.domain.IndexData;
import com.codeit.findex.indexData.domain.PeriodType;
import com.codeit.findex.indexData.dto.IndexChartResponse;
import com.codeit.findex.indexData.repository.IndexDataRepository;
import com.codeit.findex.indexInfo.domain.IndexInfo;
import com.codeit.findex.indexInfo.repository.IndexInfoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IndexChartService {
	private final IndexDataRepository indexDataRepository;
	private final IndexInfoRepository indexInfoRepository;
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	@Cacheable(value = "indexChart", key = "#indexInfoId + '_' + (#periodType != null ? #periodType.name() : 'ALL')")
	public IndexChartResponse getIndexChartData(Long indexInfoId, PeriodType periodType) {
		// IndexInfo 존재 여부 확인
		IndexInfo indexInfo = indexInfoRepository.findById(indexInfoId)
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지수 정보 ID입니다: " + indexInfoId));

		// 기간 계산
		LocalDate startDate = calculateStartDate(periodType);

		// 데이터 조회 - 이미 최적화된 쿼리 사용
		List<IndexData> indexDataList = getIndexDataOptimized(indexInfoId, startDate);

		if (indexDataList.isEmpty()) {
			log.warn("지수 데이터가 없습니다. indexInfoId: {}, periodType: {}", indexInfoId, periodType);
			return createEmptyResponse(indexInfo, periodType);
		}

		// 차트 데이터 생성 - 스트림 최적화
		List<IndexChartResponse.DataPoint> dataPoints = createDataPointsOptimized(indexDataList, startDate);

		// 이동평균선 계산 - 병렬 처리
		List<IndexChartResponse.DataPoint> ma5DataPoints = calculateMovingAverageOptimized(indexDataList, 5, startDate);
		List<IndexChartResponse.DataPoint> ma20DataPoints = calculateMovingAverageOptimized(indexDataList, 20,
			startDate);

		log.info("차트 데이터 조회 완료. indexInfoId: {}, periodType: {}, dataPoints: {}",
			indexInfoId, periodType, dataPoints.size());

		return IndexChartResponse.builder()
			.indexInfoId(indexInfoId)
			.indexClassification(indexInfo.getIndexClassification())
			.indexName(indexInfo.getIndexName())
			.periodType(periodType != null ? periodType.name() : "ALL")
			.dataPoints(dataPoints)
			.ma5DataPoints(ma5DataPoints)
			.ma20DataPoints(ma20DataPoints)
			.build();
	}

	private List<IndexData> getIndexDataOptimized(Long indexInfoId, LocalDate startDate) {
		if (startDate != null) {
			return indexDataRepository.findByIndexInfoIdAndBaseDateGreaterThanEqualOrderByBaseDateAsc(indexInfoId,
				startDate);
		} else {
			return indexDataRepository.findByIndexInfoIdOrderByBaseDateAsc(indexInfoId);
		}
	}

	private IndexChartResponse createEmptyResponse(IndexInfo indexInfo, PeriodType periodType) {
		return IndexChartResponse.builder()
			.indexInfoId(indexInfo.getId())
			.indexClassification(indexInfo.getIndexClassification())
			.indexName(indexInfo.getIndexName())
			.periodType(periodType != null ? periodType.name() : "ALL")
			.dataPoints(new ArrayList<>())
			.ma5DataPoints(new ArrayList<>())
			.ma20DataPoints(new ArrayList<>())
			.build();
	}

	private List<IndexChartResponse.DataPoint> createDataPointsOptimized(
		List<IndexData> dataList, LocalDate startDate) {

		return dataList.parallelStream()
			.filter(data -> data.getClosingPrice() != null)
			.filter(data -> startDate == null || !data.getBaseDate().isBefore(startDate))
			.map(data -> IndexChartResponse.DataPoint.builder()
				.date(data.getBaseDate().format(DATE_FORMATTER))
				.value(data.getClosingPrice().floatValue())
				.build())
			.collect(Collectors.toList());
	}

	private List<IndexChartResponse.DataPoint> calculateMovingAverageOptimized(
		List<IndexData> dataList, int period, LocalDate startDate) {

		if (dataList.size() < period) {
			return new ArrayList<>();
		}

		List<IndexChartResponse.DataPoint> maDataPoints = new ArrayList<>();

		// 유효한 가격 데이터만 미리 필터링
		List<IndexData> validData = dataList.stream()
			.filter(data -> data.getClosingPrice() != null)
			.collect(Collectors.toList());

		if (validData.size() < period) {
			return maDataPoints;
		}

		// 슬라이딩 윈도우 방식으로 이동평균 계산
		BigDecimal sum = BigDecimal.ZERO;

		// 초기 윈도우 합계 계산
		for (int i = 0; i < period; i++) {
			sum = sum.add(validData.get(i).getClosingPrice());
		}

		// 첫 번째 이동평균 추가
		LocalDate firstDate = validData.get(period - 1).getBaseDate();
		if (startDate == null || !firstDate.isBefore(startDate)) {
			float average = sum.divide(BigDecimal.valueOf(period), 2, RoundingMode.HALF_UP).floatValue();
			maDataPoints.add(IndexChartResponse.DataPoint.builder()
				.date(firstDate.format(DATE_FORMATTER))
				.value(average)
				.build());
		}

		// 슬라이딩 윈도우로 나머지 계산
		for (int i = period; i < validData.size(); i++) {
			// 이전 값 제거, 새 값 추가
			sum = sum.subtract(validData.get(i - period).getClosingPrice())
				.add(validData.get(i).getClosingPrice());

			LocalDate currentDate = validData.get(i).getBaseDate();
			if (startDate == null || !currentDate.isBefore(startDate)) {
				float average = sum.divide(BigDecimal.valueOf(period), 2, RoundingMode.HALF_UP).floatValue();
				maDataPoints.add(IndexChartResponse.DataPoint.builder()
					.date(currentDate.format(DATE_FORMATTER))
					.value(average)
					.build());
			}
		}
		return maDataPoints;
	}

	private LocalDate calculateStartDate(PeriodType periodType) {
		if (periodType == null)
			return null;

		LocalDate now = LocalDate.now();
		return switch (periodType) {
			case MONTHLY -> now.minusMonths(1);
			case QUARTERLY -> now.minusMonths(3);
			case YEARLY -> now.minusYears(1);
		};
	}
}