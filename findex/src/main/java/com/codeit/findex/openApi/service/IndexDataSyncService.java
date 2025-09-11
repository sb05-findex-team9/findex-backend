package com.codeit.findex.openApi.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codeit.findex.common.openapi.service.ApiIndexDataService;
import com.codeit.findex.indexData.domain.IndexData;
import com.codeit.findex.indexData.repository.IndexDataRepository;
import com.codeit.findex.indexInfo.domain.IndexInfo;
import com.codeit.findex.indexInfo.repository.IndexInfoRepository;
import com.codeit.findex.openApi.domain.SyncJob;
import com.codeit.findex.openApi.dto.request.IndexDataSyncRequest;
import com.codeit.findex.openApi.dto.response.SyncJobResponse;
import com.codeit.findex.openApi.repository.SyncJobRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class IndexDataSyncService {

	private final SyncJobRepository syncJobRepository;
	private final ApiIndexDataService apiIndexDataService;
	private final IndexInfoRepository indexInfoRepository;
	private final IndexDataRepository indexDataRepository;

	public List<SyncJobResponse> syncIndexData(IndexDataSyncRequest request) {
		List<SyncJobResponse> results = new ArrayList<>();

		if (request.indexInfoIds() == null || request.indexInfoIds().isEmpty()) {
			throw new IllegalArgumentException("indexInfoIds는 필수값입니다.");
		}

		Long indexInfoId = request.getFirstIndexInfoId();
		log.info("Starting index data sync for indexInfoId: {}", indexInfoId);

		IndexInfo indexInfo = indexInfoRepository.findById(indexInfoId)
			.orElseThrow(() -> new IllegalArgumentException("지수 정보를 찾을 수 없습니다: " + indexInfoId));

		try {
			apiIndexDataService.fetchAndSaveIndexData();

			List<IndexData> indexDataList = indexDataRepository.findByIndexInfoIdAndBaseDateBetween(
				indexInfoId,
				request.baseDateFrom(),
				request.baseDateTo()
			);

			log.info("Found {} index data records to sync", indexDataList.size());

			for (IndexData indexData : indexDataList) {
				SyncJob syncJob = SyncJob.builder()
					.jobType("INDEX_DATA")
					.indexInfo(indexInfo)
					.targetDate(indexData.getBaseDate())
					.worker("175.125.151.156") // TODO: 동적으로 worker IP 얻어오기
					.jobTime(LocalDateTime.now())
					.result("SUCCESS")
					.build();

				SyncJob saved = syncJobRepository.save(syncJob);
				results.add(SyncJobResponse.from(saved));
			}

			log.info("Successfully synced {} index data records", results.size());

		} catch (Exception e) {
			log.error("Failed to sync index data for indexInfoId: {}", indexInfoId, e);

			SyncJob failedJob = SyncJob.builder()
				.jobType("INDEX_DATA")
				.indexInfo(indexInfo)
				.targetDate(request.baseDateFrom())
				.worker("175.125.151.156")
				.jobTime(LocalDateTime.now())
				.result("FAILED")
				.build();

			SyncJob saved = syncJobRepository.save(failedJob);
			results.add(SyncJobResponse.from(saved));
		}

		return results;
	}
}