package com.codeit.findex.common.openapi.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
			int numOfRows = 100000;
			boolean hasMoreData = true;
			int savedCount = 0;
			int duplicateCount = 0;

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
						for (ApiResponseDto.Item item : items) {
							if (saveIndexInfo(item)) {
								savedCount++;
							} else {
								duplicateCount++;
							}
						}

						// 다음 페이지 확인
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
			throw new RuntimeException("Failed to fetch index info data", e);
		}
	}

	private boolean saveIndexInfo(ApiResponseDto.Item item) {
		try {
			String indexName = item.getIdxNm();
			String indexClassification = item.getIdxCsf();
			Integer employedItemsCount = parseInteger(item.getEpyItmsCnt());
			LocalDate basePointInTime = parseDate(item.getBasPntm());

			// 중복 데이터 확인
			List<ApiIndexInfo> existingInfos = indexInfoRepository
				.findByIndexNameAndIndexClassificationAndEmployedItemsCountAndBasePointInTime(
					indexName, indexClassification, employedItemsCount, basePointInTime);

			if (!existingInfos.isEmpty()) {
				return false;
			}

			// 데이터 저장
			ApiIndexInfo indexInfo = ApiIndexInfo.builder()
				.indexName(indexName)
				.indexClassification(indexClassification)
				.employedItemsCount(employedItemsCount)
				.basePointInTime(basePointInTime)
				.baseIndex(parseBigDecimal(item.getBasIdx()))
				.build();

			indexInfoRepository.save(indexInfo);
			return true;

		} catch (Exception e) {
			return false;
		}
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