package com.codeit.findex.indexData.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codeit.findex.indexData.domain.IndexData;
import com.codeit.findex.indexData.dto.IndexDataResponseDto;
import com.codeit.findex.indexData.dto.PagedResponseDto;
import com.codeit.findex.indexData.service.IndexDataQueryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/index-data")
@RequiredArgsConstructor
public class IndexDataQueryController {

	private final IndexDataQueryService indexDataQueryService;

	@GetMapping
	public ResponseEntity<PagedResponseDto<IndexDataResponseDto>> getIndexDataList(
		@RequestParam(required = false) Long indexInfoId,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
		@RequestParam(required = false) String cursor,
		@RequestParam(required = false) String idAfter,
		@RequestParam(required = false, defaultValue = "baseDate") String sortField,
		@RequestParam(required = false, defaultValue = "asc") String sortDirection,
		@RequestParam(required = false, defaultValue = "10") Integer size) {

		Long lastId = null;
		if (cursor != null && !cursor.trim().isEmpty()) {
			try {
				lastId = Long.parseLong(cursor);
			} catch (NumberFormatException ignored) {
			}
		} else if (idAfter != null && !idAfter.trim().isEmpty()) {
			try {
				lastId = Long.parseLong(idAfter);
			} catch (NumberFormatException ignored) {
			}
		}

		Page<IndexData> indexDataPage = indexDataQueryService.getIndexDataList(
			indexInfoId, startDate, endDate, lastId, sortField, sortDirection, size);

		List<IndexDataResponseDto> content = indexDataPage.getContent().stream()
			.map(IndexDataResponseDto::from)
			.collect(Collectors.toList());

		String nextCursor = null;
		String nextIdAfter = null;
		if (indexDataPage.hasNext() && !content.isEmpty()) {
			Long lastIdInPage = content.get(content.size() - 1).getId();
			nextCursor = lastIdInPage.toString();
			nextIdAfter = lastIdInPage.toString();
		}

		PagedResponseDto<IndexDataResponseDto> response = PagedResponseDto.<IndexDataResponseDto>builder()
			.content(content)
			.nextCursor(nextCursor)
			.nextIdAfter(nextIdAfter)
			.size(content.size())
			.totalElements((int)indexDataPage.getTotalElements())
			.hasNext(indexDataPage.hasNext())
			.build();

		return ResponseEntity.ok(response);
	}
}
