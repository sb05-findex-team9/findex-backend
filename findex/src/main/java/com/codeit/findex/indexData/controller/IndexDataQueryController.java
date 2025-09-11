package com.codeit.findex.indexData.controller;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codeit.findex.indexData.dto.IndexDataResponseDto;
import com.codeit.findex.indexData.dto.PagedResponseDto;
import com.codeit.findex.indexData.service.IndexDataQueryService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
		@RequestParam(required = false, defaultValue = "10") Integer size,
		HttpServletRequest request) {

		log.info("🚀 지수 데이터 조회 - indexInfoId: {}, cursor: {}, size: {}", indexInfoId, cursor, size);

		// cursor를 page 번호로 변환 (cursor가 없으면 첫 페이지)
		int page = 0;
		if (cursor != null && !cursor.trim().isEmpty()) {
			try {
				page = Integer.parseInt(cursor);
			} catch (NumberFormatException e) {
				log.warn("❌ 잘못된 cursor 형식: {}, 첫 페이지로 설정", cursor);
				page = 0;
			}
		} else if (idAfter != null && !idAfter.trim().isEmpty()) {
			try {
				page = Integer.parseInt(idAfter);
			} catch (NumberFormatException e) {
				log.warn("❌ 잘못된 idAfter 형식: {}, 첫 페이지로 설정", idAfter);
				page = 0;
			}
		}

		// 페이지네이션된 데이터 조회
		Page<IndexDataResponseDto> indexDataPage = indexDataQueryService.getIndexDataList(
			indexInfoId, startDate, endDate, page, size, sortField, sortDirection);

		// 다음 페이지 커서 설정
		String nextCursor = null;
		String nextIdAfter = null;
		if (indexDataPage.hasNext()) {
			int nextPage = page + 1;
			nextCursor = String.valueOf(nextPage);
			nextIdAfter = String.valueOf(nextPage);
		}

		// 응답 생성
		PagedResponseDto<IndexDataResponseDto> response = PagedResponseDto.<IndexDataResponseDto>builder()
			.content(indexDataPage.getContent())
			.nextCursor(nextCursor)
			.nextIdAfter(nextIdAfter)
			.size(indexDataPage.getContent().size())
			.totalElements((int)indexDataPage.getTotalElements())
			.hasNext(indexDataPage.hasNext())
			.build();

		log.info("✅ 응답 완료 - page: {}, size: {}, total: {}, hasNext: {}",
			page, response.getSize(), response.getTotalElements(), response.isHasNext());

		return ResponseEntity.ok(response);
	}

	// 무한 스크롤 테스트용 엔드포인트
	@GetMapping("/test-infinite")
	public ResponseEntity<String> testInfiniteScroll(
		@RequestParam(required = false) Long indexInfoId,
		@RequestParam(required = false) String cursor,
		@RequestParam(required = false, defaultValue = "10") Integer size) {

		StringBuilder result = new StringBuilder();
		result.append("=== 무한 스크롤 테스트 (OFFSET 기반) ===\n");

		int page = 0;
		if (cursor != null) {
			try {
				page = Integer.parseInt(cursor);
			} catch (NumberFormatException e) {
				page = 0;
			}
		}

		result.append("현재 페이지: ").append(page).append("\n");
		result.append("URL: /api/index-data?indexInfoId=")
			.append(indexInfoId)
			.append("&cursor=")
			.append(page)
			.append("&size=")
			.append(size)
			.append("\n");

		try {
			Page<IndexDataResponseDto> pageResult = indexDataQueryService.getIndexDataList(
				indexInfoId, null, null, page, size, "baseDate", "asc");

			result.append("응답:\n");
			result.append("- 조회된 데이터 개수: ").append(pageResult.getContent().size()).append("\n");
			result.append("- 전체 데이터 개수: ").append(pageResult.getTotalElements()).append("\n");
			result.append("- 전체 페이지 수: ").append(pageResult.getTotalPages()).append("\n");
			result.append("- hasNext: ").append(pageResult.hasNext()).append("\n");

			if (pageResult.hasNext()) {
				int nextPage = page + 1;
				result.append("- 다음 페이지: ").append(nextPage).append("\n");
				result.append("- 다음 요청 URL: /api/index-data/test-infinite?indexInfoId=")
					.append(indexInfoId)
					.append("&cursor=")
					.append(nextPage)
					.append("&size=")
					.append(size);
			} else {
				result.append("- 마지막 페이지입니다.");
			}

		} catch (Exception e) {
			result.append("오류: ").append(e.getMessage());
		}

		return ResponseEntity.ok(result.toString());
	}
}