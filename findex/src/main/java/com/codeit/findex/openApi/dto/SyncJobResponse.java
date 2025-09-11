package com.codeit.findex.openApi.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.codeit.findex.openApi.domain.SyncJob;

@Getter
@Builder
public class SyncJobResponse {
	private Long id;
	private String jobType;
	private Long indexInfoId;
	private String indexName;
	private LocalDate targetDate;
	private String worker;
	private String result;
	private LocalDateTime jobTime;

	public static SyncJobResponse from(SyncJob syncJob) {
		return SyncJobResponse.builder()
			.id(syncJob.getId())
			.jobType(syncJob.getJobType())
			.indexInfoId(syncJob.getIndexInfoId())
			.indexName(syncJob.getIndexName())
			.targetDate(syncJob.getTargetDate())
			.worker(syncJob.getWorker())
			.result(syncJob.getResult())
			.jobTime(syncJob.getJobTime())
			.build();
	}
}