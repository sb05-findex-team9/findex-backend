package com.codeit.findex.openApi.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codeit.findex.common.openapi.service.ApiIndexInfoService;
import com.codeit.findex.indexInfo.domain.IndexInfo;
import com.codeit.findex.indexInfo.repository.IndexInfoRepository;
import com.codeit.findex.openApi.domain.SyncJob;
import com.codeit.findex.openApi.dto.response.SyncJobResponseDto;
import com.codeit.findex.openApi.repository.SyncJobRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SyncService {

	private final AutoSyncConfigService autoSyncConfigService;
	private final SyncJobRepository syncJobRepository;
	private final ApiIndexInfoService apiIndexInfoService;
	private final IndexInfoRepository indexInfoRepository;

	public List<SyncJobResponseDto> createIndexInfoSyncJob() {
		List<SyncJobResponseDto> results = new ArrayList<>();

		try {
			apiIndexInfoService.fetchAndSaveIndexInfo();

			List<IndexInfo> allIndexInfos = indexInfoRepository.findAll();

			for (IndexInfo indexInfo : allIndexInfos) {
				SyncJob syncJob = SyncJob.builder()
					.jobType("INDEX_INFO")
					.indexInfo(indexInfo)
					.targetDate(null)
					.worker("175.125.151.156")
					.jobTime(LocalDateTime.now())
					.result("SUCCESS")
					.build();

				SyncJob saved = syncJobRepository.save(syncJob);
				results.add(SyncJobResponseDto.from(saved));
			}

		} catch (Exception e) {

		}
		return results;
	}

	@Transactional(readOnly = true)
	public List<SyncJob> getRecentSyncJobs(int limit) {
		return syncJobRepository.findTop10ByOrderByJobTimeDesc();
	}
}