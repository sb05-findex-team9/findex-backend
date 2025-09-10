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
		IndexInfo indexInfo = indexInfoRepository.findById(indexInfoId)
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지수 정보 ID입니다: " + indexInfoId));

		LocalDate startDate = calculateStartDate(periodType);

		List<IndexData> indexDataList = getIndexDataOptimized(indexInfoId, startDate);

		if (indexDataList.isEmpty()) {
			log.warn("지수 데이터가 없습니다. indexInfoId: {}, periodType: {}", indexInfoId, periodType);
			return createEmptyResponse(indexInfo, periodType);
		}

		List<IndexChartResponse.DataPoint> dataPoints = createDataPointsOptimized(indexDataList, startDate);

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

		List<IndexData> validData = dataList.stream()
			.filter(data -> data.getClosingPrice() != null)
			.toList();

		if (validData.size() < period) {
			return maDataPoints;
		}

		BigDecimal sum = BigDecimal.ZERO;

		for (int i = 0; i < period; i++) {
			sum = sum.add(validData.get(i).getClosingPrice());
		}

		LocalDate firstDate = validData.get(period - 1).getBaseDate();
		if (startDate == null || !firstDate.isBefore(startDate)) {
			float average = sum.divide(BigDecimal.valueOf(period), 2, RoundingMode.HALF_UP).floatValue();
			maDataPoints.add(IndexChartResponse.DataPoint.builder()
				.date(firstDate.format(DATE_FORMATTER))
				.value(average)
				.build());
		}

		for (int i = period; i < validData.size(); i++) {
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