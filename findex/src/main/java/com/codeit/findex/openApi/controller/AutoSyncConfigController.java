package com.codeit.findex.openApi.controller;

import com.codeit.findex.openApi.dto.AutoSyncConfigDto;
import com.codeit.findex.openApi.dto.request.AutoSyncConfigUpdateRequest;
import com.codeit.findex.openApi.dto.response.CursorPageResponseAutoSyncConfigDto;
import com.codeit.findex.openApi.service.AutoSyncConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auto-sync-configs")
public class AutoSyncConfigController {

	private final AutoSyncConfigService autoSyncConfigService;

	@GetMapping
	public ResponseEntity<CursorPageResponseAutoSyncConfigDto> getList(
		@RequestParam(required = false) Long indexInfoId,
		@RequestParam(required = false) Boolean enabled,
		@RequestParam(required = false) Long idAfter,
		@RequestParam(required = false) String cursor,
		@RequestParam(defaultValue = "indexInfo.indexName") String sortField,
		@RequestParam(defaultValue = "asc") String sortDirection,
		@RequestParam(defaultValue = "10") Integer size
	) {
		Sort.Direction dir = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
		CursorPageResponseAutoSyncConfigDto body =
			autoSyncConfigService.getList(indexInfoId, enabled, idAfter, cursor, sortField, dir, size);
		return ResponseEntity.ok(body);
	}

	@PatchMapping("/{id}")
	public ResponseEntity<AutoSyncConfigDto> update(
		@PathVariable("id") Long id,
		@Valid @RequestBody AutoSyncConfigUpdateRequest request
	) {
		AutoSyncConfigDto result = autoSyncConfigService.update(id, request);
		return ResponseEntity.ok(result);
	}
}
