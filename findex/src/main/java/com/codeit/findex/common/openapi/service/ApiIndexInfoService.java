package com.codeit.findex.common.openapi.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.codeit.findex.common.openapi.dto.ApiResponseDto;
import com.codeit.findex.common.openapi.entity.ApiIndexInfo;
import com.codeit.findex.common.openapi.repository.ApiIndexInfoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApiIndexInfoService {

	private final ApiIndexInfoRepository indexInfoRepository;
	private final RestTemplate restTemplate = new RestTemplate();

	@Value("${api.service-key}")
	private String serviceKey;

	@Value("${api.stock-url}")
	private String apiUrl;

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

	@Transactional
	public void fetchAndSaveIndexInfo() {
		try {
			int pageNo = 1;
			int numOfRows = 240;
			String url = buildApiUrl(pageNo, numOfRows);

			ApiResponseDto response = restTemplate.getForObject(url, ApiResponseDto.class);

			if (response != null && response.getResponse() != null
				&& response.getResponse().getBody() != null
				&& response.getResponse().getBody().getItems() != null) {

				List<ApiResponseDto.Item> items = response.getResponse().getBody().getItems().getItem();

				if (items != null && !items.isEmpty()) {
					for (ApiResponseDto.Item item : items) {
						saveIndexInfo(item);
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to fetch index info data", e);
		}
	}

	private void saveIndexInfo(ApiResponseDto.Item item) {
		String indexName = item.getIdxNm();
		String indexClassification = item.getIdxCsf();

		Optional<ApiIndexInfo> existingInfo = indexInfoRepository
			.findByIndexNameAndIndexClassification(indexName, indexClassification).stream().findFirst();

		if (existingInfo.isPresent()) {
			return;
		}

		ApiIndexInfo indexInfo = ApiIndexInfo.builder()
			.indexName(indexName)
			.indexClassification(indexClassification)
			.employedItemsCount(parseInteger(item.getEpyItmsCnt()))
			.basePointInTime(parseDate(item.getBasPntm()))
			.baseIndex(parseBigDecimal(item.getBasIdx()))
			.build();

		indexInfoRepository.save(indexInfo);
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
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		try {
			return Integer.parseInt(value.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private BigDecimal parseBigDecimal(String value) {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		try {
			return new BigDecimal(value.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private LocalDate parseDate(String value) {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		try {
			return LocalDate.parse(value.trim(), DATE_FORMATTER);
		} catch (Exception e) {
			return null;
		}
	}
}
