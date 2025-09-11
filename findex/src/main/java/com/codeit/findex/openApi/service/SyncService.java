package com.codeit.findex.openApi.service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codeit.findex.common.openapi.service.ApiIndexInfoService;
import com.codeit.findex.indexInfo.domain.IndexInfo;
import com.codeit.findex.indexInfo.repository.IndexInfoRepository;
import com.codeit.findex.openApi.domain.SyncJob;
import com.codeit.findex.openApi.dto.response.SyncJobResponse;
import com.codeit.findex.openApi.repository.SyncJobRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SyncService {

	private final SyncJobRepository syncJobRepository;
	private final ApiIndexInfoService apiIndexInfoService;
	private final IndexInfoRepository indexInfoRepository;

	public List<SyncJobResponse> createIndexInfoSyncJob() {
		List<SyncJobResponse> results = new ArrayList<>();

		String workerIp = getLocalIpAddress();

		try {
			apiIndexInfoService.fetchAndSaveIndexInfo();

			List<IndexInfo> allIndexInfos = indexInfoRepository.findAll();

			for (IndexInfo indexInfo : allIndexInfos) {
				SyncJob syncJob = SyncJob.builder()
					.jobType("INDEX_INFO")
					.indexInfo(indexInfo)
					.targetDate(null)
					.worker(workerIp)
					.jobTime(LocalDateTime.now())
					.result("SUCCESS")
					.build();

				SyncJob saved = syncJobRepository.save(syncJob);
				results.add(SyncJobResponse.from(saved));
			}

		} catch (Exception e) {
			SyncJob failedJob = SyncJob.builder()
				.jobType("INDEX_INFO")
				.indexInfo(null)
				.targetDate(null)
				.worker(workerIp)
				.jobTime(LocalDateTime.now())
				.result("FAILED")
				.build();

			SyncJob saved = syncJobRepository.save(failedJob);
			results.add(SyncJobResponse.from(saved));
		}
		return results;
	}

	@Transactional(readOnly = true)
	public List<SyncJob> getRecentSyncJobs(int limit) {
		return syncJobRepository.findTop10ByOrderByJobTimeDesc();
	}

	private String getLocalIpAddress() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			return "unknown";
		}
	}
}