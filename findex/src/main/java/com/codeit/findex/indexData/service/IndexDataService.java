package com.codeit.findex.indexData.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.codeit.findex.indexData.domain.IndexData;
import com.codeit.findex.indexData.domain.PeriodType;
import com.codeit.findex.indexData.dto.IndexChartResponse;
import com.codeit.findex.indexData.dto.IndexDataRequestDto;
import com.codeit.findex.indexData.repository.IndexDataRepository;
import com.codeit.findex.indexInfo.domain.IndexInfo;
import com.codeit.findex.indexInfo.repository.IndexInfoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class IndexDataService {
	private final IndexDataRepository indexDataRepository;
	private final IndexInfoRepository indexInfoRepository;
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	public Page<IndexData> getIndexDataList(Long indexInfoId, LocalDate startDate, LocalDate endDate,
		Long lastId, String sortField, String sortDirection, Integer size) {

		// 정렬 방향 설정
		Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection)
			? Sort.Direction.DESC
			: Sort.Direction.ASC;

		String entitySortField = mapSortField(sortField);

		log.info("정렬 설정 - 필드: {}, 방향: {}, 매핑된 필드: {}, lastId: {}",
			sortField, sortDirection, entitySortField, lastId);

		// 보조 정렬로 ID 추가 (중복 값이 있을 때 일관성 보장)
		Sort sort = Sort.by(direction, entitySortField).and(Sort.by(Sort.Direction.ASC, "id"));
		Pageable pageable = PageRequest.of(0, size, sort);

		Page<IndexData> result;
		if (lastId != null) {
			// 커서 기반 조회 - 정렬 방향에 따라 다른 조건 사용
			if (direction == Sort.Direction.DESC) {
				log.info("내림차순 커서 조회 - id < {}", lastId);
				result = indexDataRepository.findIndexDataWithFiltersAfterIdDesc(
					indexInfoId, startDate, endDate, lastId, pageable);
			} else {
				log.info("오름차순 커서 조회 - id > {}", lastId);
				result = indexDataRepository.findIndexDataWithFiltersAfterIdAsc(
					indexInfoId, startDate, endDate, lastId, pageable);
			}
			log.info("커서 기반 조회 완료 - 결과 수: {}", result.getContent().size());
		} else {
			// 첫 페이지 조회
			result = indexDataRepository.findIndexDataWithFilters(
				indexInfoId, startDate, endDate, pageable);
			log.info("첫 페이지 조회 완료 - 결과 수: {}", result.getContent().size());
		}

		return result;
	}

	public IndexData createIndexData(IndexDataRequestDto requestDto) {
		IndexInfo indexInfo = null;
		if (requestDto.getIndexInfoId() != null) {
			indexInfo = indexInfoRepository.findById(requestDto.getIndexInfoId())
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지수 정보 ID입니다: " + requestDto.getIndexInfoId()));
		}
		IndexData indexData = requestDto.toEntity(indexInfo);

		return indexDataRepository.save(indexData);
	}

	private String mapSortField(String apiSortField) {
		return switch (apiSortField) {
			case "baseDate", "date" -> "baseDate";           // 날짜
			case "closingPrice", "price" -> "closingPrice";  // 종가
			default -> "baseDate";                           // 기본값: 날짜
		};
	}

	public IndexChartResponse getIndexChartData(Long indexInfoId, PeriodType periodType) {
		// 기간 계산
		LocalDate startDate = calculateStartDate(periodType);

		// 데이터 조회
		List<IndexData> indexDataList = startDate != null
			? indexDataRepository.findByIndexInfoIdAndBaseDateGreaterThanEqualOrderByBaseDateAsc(indexInfoId, startDate)
			: indexDataRepository.findByIndexInfoIdOrderByBaseDateAsc(indexInfoId);

		if (indexDataList.isEmpty()) {
			throw new IllegalArgumentException("해당 지수 정보에 대한 데이터가 없습니다.");
		}

		// 지수 정보 조회
		var indexInfo = indexDataList.get(0).getIndexInfo();

		// 기간별 데이터 필터링
		List<IndexData> filteredData = filterDataByPeriod(indexDataList, periodType);

		// 차트 데이터 생성
		List<IndexChartResponse.DataPoint> dataPoints = createDataPoints(filteredData);

		// 이동평균선 계산
		List<IndexChartResponse.DataPoint> ma5DataPoints = calculateMovingAverage(indexDataList, 5);
		List<IndexChartResponse.DataPoint> ma20DataPoints = calculateMovingAverage(indexDataList, 20);

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
			.filter(data -> !data.getBaseDate().isBefore(startDate))
			.collect(Collectors.toList());
	}

	private List<IndexChartResponse.DataPoint> createDataPoints(List<IndexData> dataList) {
		return dataList.stream()
			.map(data -> IndexChartResponse.DataPoint.builder()
				.date(data.getBaseDate().format(DATE_FORMATTER))
				.value(data.getClosingPrice() != null ? data.getClosingPrice().floatValue() : 0f)
				.build())
			.collect(Collectors.toList());
	}

	private List<IndexChartResponse.DataPoint> calculateMovingAverage(List<IndexData> dataList, int period) {
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
				float average = sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP).floatValue();
				maDataPoints.add(IndexChartResponse.DataPoint.builder()
					.date(dataList.get(i).getBaseDate().format(DATE_FORMATTER))
					.value(average)
					.build());
			}
		}

		return maDataPoints;
	}
}