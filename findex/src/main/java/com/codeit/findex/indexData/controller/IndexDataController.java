package com.codeit.findex.indexData.controller;

import java.time.LocalDate;
import java.util.Base64;
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
import com.codeit.findex.indexData.service.IndexDataService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/index-data")
@RequiredArgsConstructor
public class IndexDataController {
	private final IndexDataService indexDataService;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@GetMapping
	public ResponseEntity<com.codeit.findex.indexData.dto.PagedResponseDto<IndexDataResponseDto>> getIndexDataList(
		@RequestParam(required = false) Long indexInfoId,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
		@RequestParam(required = false) Long idAfter,
		@RequestParam(required = false) String cursor,
		@RequestParam(required = false, defaultValue = "id") String sortField,
		@RequestParam(required = false, defaultValue = "asc") String sortDirection,
		@RequestParam(required = false, defaultValue = "10") int size) {

		Long actualIdAfter = idAfter;
		if (cursor != null && !cursor.isEmpty()) {
			actualIdAfter = decodeCursor(cursor);
		}

		Page<IndexData> indexDataPage = indexDataService.getIndexDataList(
			indexInfoId, startDate, endDate, actualIdAfter, sortField, sortDirection, size);

		List<IndexDataResponseDto> content = indexDataPage.getContent().stream()
			.map(IndexDataResponseDto::from)
			.collect(Collectors.toList());

		String nextCursor = null;
		String nextIdAfter = null;
		if (indexDataPage.hasNext() && !content.isEmpty()) {
			Long lastId = content.get(content.size() - 1).getId();
			nextCursor = encodeCursor(lastId);
			nextIdAfter = encodeCursor(lastId);
		}

		com.codeit.findex.indexData.dto.PagedResponseDto<IndexDataResponseDto> response = com.codeit.findex.indexData.dto.PagedResponseDto.<IndexDataResponseDto>builder()
			.content(content)
			.nextCursor(nextCursor)
			.nextIdAfter(nextIdAfter)
			.size(content.size())
			.totalElements(indexDataPage.getTotalElements())
			.hasNext(indexDataPage.hasNext())
			.build();

		return ResponseEntity.ok(response);
	}

	private String encodeCursor(Long id) {
		try {
			String json = String.format("{\"id\":%d}", id);
			return Base64.getEncoder().encodeToString(json.getBytes());
		} catch (Exception e) {
			return null;
		}
	}

	private Long decodeCursor(String cursor) {
		try {
			String json = new String(Base64.getDecoder().decode(cursor));
			String idStr = json.replaceAll("[{}\"id:]", "");
			return Long.parseLong(idStr.trim());
		} catch (Exception e) {
			return null;
		}
	}
}
