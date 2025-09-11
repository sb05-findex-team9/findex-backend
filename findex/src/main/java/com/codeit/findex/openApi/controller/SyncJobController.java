package com.codeit.findex.openApi.controller;

import com.codeit.findex.openApi.dto.response.PagedSyncJobResponse;
import com.codeit.findex.openApi.dto.request.SyncJobListRequest;
import com.codeit.findex.openApi.dto.request.IndexDataSyncRequest;
import com.codeit.findex.openApi.dto.response.SyncJobResponse;
import com.codeit.findex.openApi.service.SyncJobQueryService;
import com.codeit.findex.openApi.service.IndexDataSyncService;
import com.codeit.findex.openApi.service.SyncService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/sync-jobs")
@RequiredArgsConstructor
public class SyncJobController {

	private final SyncJobQueryService syncJobQueryService;
	private final IndexDataSyncService indexDataSyncService;
	private final SyncService syncService;

	@GetMapping
	public ResponseEntity<PagedSyncJobResponse> getSyncJobList(
		@ModelAttribute SyncJobListRequest request) {

		// 무한 스크롤을 위한 간단한 전처리
		preprocessForInfiniteScroll(request);

		log.info("Processing sync job list request: jobType={}, size={}, cursor={}, hasNext={}",
			request.getJobType(), request.getSize(), request.getCursor(),
			request.getCursor() != null ? "continuing" : "initial");

		PagedSyncJobResponse response = syncJobQueryService.getSyncJobList(request);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/index-data")
	public ResponseEntity<List<SyncJobResponse>> syncIndexData(
		@RequestBody @Valid IndexDataSyncRequest request) {

		log.info("Index data sync requested for indexInfoIds: {}", request.indexInfoIds());
		List<SyncJobResponse> result = indexDataSyncService.syncIndexData(request);
		return ResponseEntity.ok(result);
	}

	@PostMapping("/index-infos")
	public ResponseEntity<List<SyncJobResponse>> syncIndexInfos() {
		log.info("Index info sync requested");
		List<SyncJobResponse> result = syncService.createIndexInfoSyncJob();
		return ResponseEntity.ok(result);
	}

	/**
	 * 무한 스크롤을 위한 최소한의 전처리
	 */
	private void preprocessForInfiniteScroll(SyncJobListRequest request) {
		// 빈 문자열을 null로 변환
		request.setJobType(nullifyEmptyString(request.getJobType()));
		request.setStatus(nullifyEmptyString(request.getStatus()));
		request.setWorker(nullifyEmptyString(request.getWorker()));
		request.setCursor(nullifyEmptyString(request.getCursor()));
		request.setIdAfter(nullifyEmptyString(request.getIdAfter()));

		// 무한 스크롤 요청인지 확인 (cursor나 idAfter가 있으면 무한 스크롤)
		boolean isInfiniteScroll = request.getCursor() != null || request.getIdAfter() != null;

		if (!isInfiniteScroll) {
			// 초기 요청이나 필터 변경 시에만 페이지네이션 상태 초기화
			if (hasFilterChange(request)) {
				log.debug("Filter change detected, resetting pagination");
				request.setCursor(null);
				request.setIdAfter(null);
			}
		} else {
			log.debug("Infinite scroll request - preserving cursor: {}", request.getCursor());
		}

		// Size 기본값 설정
		if (request.getSize() == null || request.getSize() <= 0) {
			request.setSize(10);
		}

		// 정렬 기본값 설정
		if (request.getSortField() == null) {
			request.setSortField("jobTime");
		}
		if (request.getSortDirection() == null) {
			request.setSortDirection("desc");
		}
	}

	/**
	 * 필터 변경이 있었는지 확인
	 */
	private boolean hasFilterChange(SyncJobListRequest request) {
		// jobType이나 다른 주요 필터가 설정된 경우 필터 변경으로 간주
		return request.getJobType() != null ||
			request.getIndexInfoId() != null ||
			request.getStatus() != null ||
			request.getBaseDateFrom() != null ||
			request.getBaseDateTo() != null;
	}

	private String nullifyEmptyString(String value) {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		return value.trim();
	}
}