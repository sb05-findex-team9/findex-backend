package com.codeit.findex.openApi.controller;

import com.codeit.findex.openApi.dto.response.PagedSyncJobResponse;
import com.codeit.findex.openApi.dto.request.SyncJobListRequest;
import com.codeit.findex.openApi.dto.request.IndexDataSyncRequest;
import com.codeit.findex.openApi.dto.response.SyncJobResponse;
import com.codeit.findex.openApi.service.SyncJobQueryService;
import com.codeit.findex.openApi.service.IndexDataSyncService;
import com.codeit.findex.openApi.service.SyncService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/sync-jobs")
@RequiredArgsConstructor
public class SyncJobController {

	private final SyncJobQueryService syncJobQueryService;
	private final IndexDataSyncService indexDataSyncService;
	private final SyncService syncService;

	@GetMapping
	public ResponseEntity<PagedSyncJobResponse> getSyncJobList(
		@ModelAttribute @Valid SyncJobListRequest request) {

		PagedSyncJobResponse response = syncJobQueryService.getSyncJobList(request);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/index-data")
	public ResponseEntity<List<SyncJobResponse>> syncIndexData(
		@RequestBody @Valid IndexDataSyncRequest request) {

		List<SyncJobResponse> result = indexDataSyncService.syncIndexData(request);
		return ResponseEntity.ok(result);
	}

	@PostMapping("/index-infos")
	public ResponseEntity<List<SyncJobResponse>> syncIndexInfos() {
		List<SyncJobResponse> result = syncService.createIndexInfoSyncJob();
		return ResponseEntity.ok(result);
	}
}