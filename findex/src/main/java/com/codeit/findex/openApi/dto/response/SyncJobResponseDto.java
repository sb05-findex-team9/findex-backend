package com.codeit.findex.openApi.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.codeit.findex.openApi.domain.SyncJob;

public record SyncJobResponseDto(
	Long id,
	String jobType,
	Long indexInfoId,
	LocalDate targetDate,
	String worker,
	LocalDateTime jobTime,
	String result
) {
	public static SyncJobResponseDto from(SyncJob syncJob) {
		return new SyncJobResponseDto(
			syncJob.getId(),
			syncJob.getJobType(),
			syncJob.getIndexInfo() != null ? syncJob.getIndexInfo().getId() : null,
			syncJob.getTargetDate(),
			syncJob.getWorker(),
			syncJob.getJobTime(),
			syncJob.getResult()
		);
	}
}