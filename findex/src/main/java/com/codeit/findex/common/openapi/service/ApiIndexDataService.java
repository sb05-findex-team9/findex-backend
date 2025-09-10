package com.codeit.findex.common.openapi.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.codeit.findex.common.openapi.dto.ApiResponseDto;
import com.codeit.findex.indexData.domain.IndexData;
import com.codeit.findex.indexData.repository.IndexDataRepository;
import com.codeit.findex.indexInfo.domain.IndexInfo;
import com.codeit.findex.indexInfo.repository.IndexInfoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApiIndexDataService {

	private final IndexDataRepository indexDataRepository;
	private final IndexInfoRepository indexInfoRepository;
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
			int numOfRows = 10000;
			boolean hasMoreData = true;
			int savedCount = 0;

			LocalDate oneYearAgo = LocalDate.now().minusYears(1);

			while (hasMoreData) {
				String url = buildApiUrl(pageNo, numOfRows);

				ApiResponseDto response = restTemplate.getForObject(url, ApiResponseDto.class);

				if (response != null && response.getResponse() != null
					&& response.getResponse().getBody() != null
					&& response.getResponse().getBody().getItems() != null) {

					List<ApiResponseDto.Item> items = response.getResponse().getBody().getItems().getItem();

					if (items != null && !items.isEmpty()) {
						boolean containsOldData = items.stream()
							.map(i -> parseDate(i.getBasDt()))
							.filter(Objects::nonNull)
							.anyMatch(d -> d.isBefore(oneYearAgo));

						List<ApiResponseDto.Item> filteredItems = items.stream()
							.filter(i -> {
								LocalDate d = parseDate(i.getBasDt());
								return d != null && !d.isBefore(oneYearAgo);
							})
							.toList();

						if (!filteredItems.isEmpty()) {
							int inserted = saveIndexDataBatch(filteredItems);
							savedCount += inserted;
						}

						if (containsOldData) {
							hasMoreData = false;
						} else {
							int totalCount = response.getResponse().getBody().getTotalCount();
							if (pageNo * numOfRows >= totalCount) {
								hasMoreData = false;
							} else {
								pageNo++;
							}
						}
					} else {
						hasMoreData = false;
					}
				} else {
					hasMoreData = false;
				}
			}

			System.out.println("Saved count = " + savedCount);
		} catch (Exception e) {
			throw new RuntimeException("Failed to fetch index data", e);
		}
	}

	private int saveIndexDataBatch(List<ApiResponseDto.Item> items) {
		if (items == null || items.isEmpty())
			return 0;

		Map<String, IndexInfo> infoCache = new HashMap<>();
		List<IndexData> batch = new ArrayList<>();

		for (ApiResponseDto.Item item : items) {
			LocalDate baseDate = parseDate(item.getBasDt());
			if (baseDate == null) continue;

			String key = item.getIdxNm() + "_" + item.getIdxCsf();

			IndexInfo indexInfo = infoCache.computeIfAbsent(key, k -> {
				List<IndexInfo> found = indexInfoRepository.findByIndexNameAndIndexClassification(
					item.getIdxNm(), item.getIdxCsf());

				if (found.isEmpty()) {
					IndexInfo newInfo = IndexInfo.builder()
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

			batch.add(IndexData.builder()
				.indexInfo(indexInfo)
				.baseDate(baseDate)
				.closingPrice(parseBigDecimal(item.getClpr()))
				.versus(parseBigDecimal(item.getVs()))
				.fluctuationRate(parseBigDecimal(item.getFltRt()))
				.marketPrice(parseBigDecimal(item.getMkp()))
				.highPrice(parseBigDecimal(item.getHipr()))
				.lowPrice(parseBigDecimal(item.getLopr()))
				.tradingQuantity(parseLong(item.getTrqu()))
				.tradingPrice(parseBigDecimal(item.getTrPrc()))
				.marketTotalAmount(parseBigDecimal(item.getLstgMrktTotAmt()))
				.build());
		}

		Map<IndexInfo, List<LocalDate>> datesByInfo = batch.stream()
			.collect(Collectors.groupingBy(IndexData::getIndexInfo,
				Collectors.mapping(IndexData::getBaseDate, Collectors.toList())));

		Set<String> existingKeys = new HashSet<>();
		for (Map.Entry<IndexInfo, List<LocalDate>> entry : datesByInfo.entrySet()) {
			IndexInfo info = entry.getKey();
			List<LocalDate> dates = entry.getValue();
			List<LocalDate> existingDates =
				indexDataRepository.findExistingDates(info, dates);
			existingDates.forEach(d -> existingKeys.add(info.getId() + "_" + d));
		}

		List<IndexData> toInsert = batch.stream()
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
		if (value == null || value.trim().isEmpty()) return null;
		try {
			return Integer.parseInt(value.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private Long parseLong(String value) {
		if (value == null || value.trim().isEmpty()) return null;
		try {
			return Long.parseLong(value.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private BigDecimal parseBigDecimal(String value) {
		if (value == null || value.trim().isEmpty()) return null;
		try {
			return new BigDecimal(value.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private LocalDate parseDate(String value) {
		if (value == null || value.trim().isEmpty()) return null;
		try {
			return LocalDate.parse(value.trim(), DATE_FORMATTER);
		} catch (Exception e) {
			return null;
		}
	}
}