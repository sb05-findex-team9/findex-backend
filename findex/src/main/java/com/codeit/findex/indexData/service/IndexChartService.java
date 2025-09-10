package com.codeit.findex.indexData.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

	public IndexChartResponse getIndexChartData(Long indexInfoId, PeriodType periodType) {
		// IndexInfo 존재 여부 확인
		IndexInfo indexInfo = indexInfoRepository.findById(indexInfoId)
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지수 정보 ID입니다: " + indexInfoId));

		// 기간 계산
		LocalDate startDate = calculateStartDate(periodType);

		// 데이터 조회
		List<IndexData> indexDataList = startDate != null
			? indexDataRepository.findByIndexInfoIdAndBaseDateGreaterThanEqualOrderByBaseDateAsc(indexInfoId, startDate)
			: indexDataRepository.findByIndexInfoIdOrderByBaseDateAsc(indexInfoId);

		if (indexDataList.isEmpty()) {
			log.warn("지수 데이터가 없습니다. indexInfoId: {}, periodType: {}", indexInfoId, periodType);
			return IndexChartResponse.builder()
				.indexInfoId(indexInfoId)
				.indexClassification(indexInfo.getIndexClassification())
				.indexName(indexInfo.getIndexName())
				.periodType(periodType != null ? periodType.name() : "ALL")
				.dataPoints(new ArrayList<>())
				.ma5DataPoints(new ArrayList<>())
				.ma20DataPoints(new ArrayList<>())
				.build();
		}

		List<IndexData> filteredData = filterDataByPeriod(indexDataList, periodType);

		// 차트 데이터 생성
		List<IndexChartResponse.DataPoint> dataPoints = createDataPoints(filteredData);

		List<IndexChartResponse.DataPoint> ma5DataPoints = calculateMovingAverage(indexDataList, 5, startDate);
		List<IndexChartResponse.DataPoint> ma20DataPoints = calculateMovingAverage(indexDataList, 20, startDate);

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

	private List<IndexData> filterDataByPeriod(List<IndexData> dataList, PeriodType periodType) {
		if (periodType == null)
			return dataList;

		LocalDate startDate = calculateStartDate(periodType);
		return dataList.stream()
			.filter(data -> data.getBaseDate() != null && !data.getBaseDate().isBefore(startDate))
			.collect(Collectors.toList());
	}

	private List<IndexChartResponse.DataPoint> createDataPoints(List<IndexData> dataList) {
		return dataList.stream()
			.filter(data -> data.getClosingPrice() != null) // null 체크 추가
			.map(data -> IndexChartResponse.DataPoint.builder()
				.date(data.getBaseDate().format(DATE_FORMATTER))
				.value(data.getClosingPrice().floatValue())
				.build())
			.collect(Collectors.toList());
	}

	private List<IndexChartResponse.DataPoint> calculateMovingAverage(List<IndexData> dataList, int period,
		LocalDate startDate) {
		List<IndexChartResponse.DataPoint> maDataPoints = new ArrayList<>();

		if (dataList.size() < period) {
			return maDataPoints;
		}

		for (int i = period - 1; i < dataList.size(); i++) {
			BigDecimal sum = BigDecimal.ZERO;
			int count = 0;

			for (int j = i - period + 1; j <= i; j++) {
				BigDecimal price = dataList.get(j).getClosingPrice();
				if (price != null) {
					sum = sum.add(price);
					count++;
				}
			}

			if (count > 0) {
				// startDate가 있는 경우 해당 기간 이후 데이터만 포함
				LocalDate currentDate = dataList.get(i).getBaseDate();
				if (startDate == null || !currentDate.isBefore(startDate)) {
					float average = sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP).floatValue();
					maDataPoints.add(IndexChartResponse.DataPoint.builder()
						.date(currentDate.format(DATE_FORMATTER))
						.value(average)
						.build());
				}
			}
		}

		return maDataPoints;
	}
}