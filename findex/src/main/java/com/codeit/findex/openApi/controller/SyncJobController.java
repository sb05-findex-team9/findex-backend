package com.codeit.findex.openApi.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codeit.findex.openApi.dto.request.IndexDataSyncRequest;
import com.codeit.findex.openApi.dto.response.SyncJobResponseDto;
import com.codeit.findex.openApi.service.IndexDataSyncService;
import com.codeit.findex.openApi.service.SyncService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/sync-jobs")
@RequiredArgsConstructor
public class SyncJobController {

	private final SyncService syncService;
	private final IndexDataSyncService indexDataSyncService;

	@PostMapping("/index-infos")
	public ResponseEntity<List<SyncJobResponseDto>> syncIndexInfos() {
		List<SyncJobResponseDto> responses = syncService.createIndexInfoSyncJob();
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(responses);
	}

	@PostMapping("/index-data")
	public ResponseEntity<List<SyncJobResponseDto>> syncIndexData(@RequestBody IndexDataSyncRequest request) {

		List<SyncJobResponseDto> responses = indexDataSyncService.syncIndexData(request);

		return ResponseEntity.status(HttpStatus.ACCEPTED).body(responses);
	}

}