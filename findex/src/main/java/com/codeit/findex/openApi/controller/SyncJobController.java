package com.codeit.findex.openApi.controller;

import com.codeit.findex.openApi.dto.PagedSyncJobResponse;
import com.codeit.findex.openApi.dto.SyncJobListRequest;
import com.codeit.findex.openApi.service.SyncJobQueryService;

import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/sync-jobs")
@RequiredArgsConstructor
public class SyncJobController {

	private final SyncJobQueryService syncJobQueryService;

	// 연동 작업 목록 조회
	@GetMapping
	public ResponseEntity<PagedSyncJobResponse> getSyncJobList(
		@RequestParam(required = false) String jobType,
		@RequestParam(required = false) Long indexInfoId,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate baseDateFrom,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate baseDateTo,
		@RequestParam(required = false) String worker,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime jobTimeFrom,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime jobTimeTo,
		@RequestParam(required = false) String status,
		@RequestParam(required = false) String idAfter,
		@RequestParam(required = false) String cursor,
		@RequestParam(required = false, defaultValue = "jobTime") String sortField,
		@RequestParam(required = false, defaultValue = "desc") String sortDirection,
		@RequestParam(required = false, defaultValue = "10") Integer size) {

		SyncJobListRequest request = new SyncJobListRequest();
		request.setJobType(jobType);
		request.setIndexInfoId(indexInfoId);
		request.setBaseDateFrom(baseDateFrom);
		request.setBaseDateTo(baseDateTo);
		request.setWorker(worker);
		request.setJobTimeFrom(jobTimeFrom);
		request.setJobTimeTo(jobTimeTo);
		request.setStatus(status);
		request.setIdAfter(idAfter);
		request.setCursor(cursor);
		request.setSortField(sortField);
		request.setSortDirection(sortDirection);
		request.setSize(size);

		PagedSyncJobResponse response = syncJobQueryService.getSyncJobList(request);
		return ResponseEntity.ok(response);
	}
}