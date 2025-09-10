package com.codeit.findex.common.openapi.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.codeit.findex.common.openapi.dto.ApiResponseDto;
import com.codeit.findex.common.openapi.entity.ApiIndexData;
import com.codeit.findex.common.openapi.entity.ApiIndexInfo;
import com.codeit.findex.common.openapi.repository.ApiIndexDataRepository;
import com.codeit.findex.common.openapi.repository.ApiIndexInfoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApiIndexDataService {

	private final ApiIndexDataRepository indexDataRepository;
	private final ApiIndexInfoRepository indexInfoRepository;
	private final RestTemplate restTemplate = new RestTemplate();

	@Value("${api.service-key}")
	private String serviceKey;

	@Value("${api.stock-url}")
	private String apiUrl;

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

	@Transactional
	public void fetchAndSaveIndexData() {
		try {
			int pageNo = 1;
			int numOfRows = 100000;
			boolean hasMoreData = true;
			int savedCount = 0;

			while (hasMoreData) {
				String url = buildApiUrl(pageNo, numOfRows);

				// API 호출
				ApiResponseDto response = restTemplate.getForObject(url, ApiResponseDto.class);

				// 응답 데이터 검증
				if (response != null && response.getResponse() != null
					&& response.getResponse().getBody() != null
					&& response.getResponse().getBody().getItems() != null) {

					List<ApiResponseDto.Item> items = response.getResponse().getBody().getItems().getItem();

					if (items != null && !items.isEmpty()) {
						int inserted = saveIndexDataBatch(items);
						savedCount += inserted;

						int totalCount = response.getResponse().getBody().getTotalCount();
						if (pageNo * numOfRows >= totalCount) {
							hasMoreData = false;
						} else {
							pageNo++;
						}
					} else {
						hasMoreData = false;
					}
				} else {
					hasMoreData = false;
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to fetch index data", e);
		}
	}

	private int saveIndexDataBatch(List<ApiResponseDto.Item> items) {
		if (items == null || items.isEmpty())
			return 0;

		// indexInfo 캐시
		Map<String, ApiIndexInfo> infoCache = new HashMap<>();

		// 리스트로 배치 저장
		List<ApiIndexData> batch = new ArrayList<>();

		for (ApiResponseDto.Item item : items) {
			LocalDate baseDate = parseDate(item.getBasDt());
			if (baseDate == null)
				continue;

			String key = item.getIdxNm() + "_" + item.getIdxCsf();

			ApiIndexInfo indexInfo = infoCache.computeIfAbsent(key, k -> {
				List<ApiIndexInfo> found = indexInfoRepository.findByIndexNameAndIndexClassification(
					item.getIdxNm(), item.getIdxCsf());

				if (found.isEmpty()) {
					ApiIndexInfo newInfo = ApiIndexInfo.builder()
						.indexName(item.getIdxNm())
						.indexClassification(item.getIdxCsf())
						.employedItemsCount(parseInteger(item.getEpyItmsCnt()))
						.basePointInTime(parseDate(item.getBasPntm()))
						.baseIndex(parseBigDecimal(item.getBasIdx()))
						.build();
					return indexInfoRepository.save(newInfo);
				} else {
					return found.get(0);
				}
			});

			batch.add(ApiIndexData.builder()
				.indexInfo(indexInfo)
				.baseDate(baseDate)
				.closingPrice(parseBigDecimal(item.getClpr()))
				.priceChange(parseBigDecimal(item.getVs()))
				.fluctuationRate(parseBigDecimal(item.getFltRt()))
				.marketPrice(parseBigDecimal(item.getMkp()))
				.highPrice(parseBigDecimal(item.getHipr()))
				.lowPrice(parseBigDecimal(item.getLopr()))
				.tradingVolume(parseLong(item.getTrqu()))
				.transactionPrice(parseBigDecimal(item.getTrPrc()))
				.marketCap(parseBigDecimal(item.getLstgMrktTotAmt()))
				.build());
		}

		// indexInfo 별로 baseDate 중복 체크
		Map<ApiIndexInfo, List<LocalDate>> datesByInfo = batch.stream()
			.collect(Collectors.groupingBy(ApiIndexData::getIndexInfo,
				Collectors.mapping(ApiIndexData::getBaseDate, Collectors.toList())));

		Set<String> existingKeys = new HashSet<>();
		for (Map.Entry<ApiIndexInfo, List<LocalDate>> entry : datesByInfo.entrySet()) {
			ApiIndexInfo info = entry.getKey();
			List<LocalDate> dates = entry.getValue();
			List<LocalDate> existingDates =
				indexDataRepository.findExistingDates(info, dates);
			existingDates.forEach(d -> existingKeys.add(info.getId() + "_" + d));
		}

		// 신규 데이터만 필터링
		List<ApiIndexData> toInsert = batch.stream()
			.filter(d -> !existingKeys.contains(d.getIndexInfo().getId() + "_" + d.getBaseDate()))
			.toList();

		if (!toInsert.isEmpty()) {
			indexDataRepository.saveAll(toInsert);
			return toInsert.size();
		}
		return 0;
	}

	private String buildApiUrl(int pageNo, int numOfRows) {
		return UriComponentsBuilder.fromHttpUrl(apiUrl)
			.queryParam("serviceKey", serviceKey)
			.queryParam("resultType", "json")
			.queryParam("pageNo", pageNo)
			.queryParam("numOfRows", numOfRows)
			.build(false)
			.toUriString();
	}

	private Integer parseInteger(String value) {
		if (value == null || value.trim().isEmpty())
			return null;
		try {
			return Integer.parseInt(value.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private Long parseLong(String value) {
		if (value == null || value.trim().isEmpty())
			return null;
		try {
			return Long.parseLong(value.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private BigDecimal parseBigDecimal(String value) {
		if (value == null || value.trim().isEmpty())
			return null;
		try {
			return new BigDecimal(value.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private LocalDate parseDate(String value) {
		if (value == null || value.trim().isEmpty())
			return null;
		try {
			return LocalDate.parse(value.trim(), DATE_FORMATTER);
		} catch (Exception e) {
			return null;
		}
	}
}