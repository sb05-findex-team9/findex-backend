package com.codeit.findex.indexData.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Slice;
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

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/index-data")
@RequiredArgsConstructor
public class IndexDataQueryController {

	private final IndexDataQueryService indexDataQueryService;

	// 지수 데이터 목록 조회
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

		// 요청 정보 상세 로깅
		String referer = request.getHeader("Referer");
		String userAgent = request.getHeader("User-Agent");

		log.info("🚀 ===========================================");
		log.info("🔍 API 요청 상세 정보:");
		log.info("   - indexInfoId: {}", indexInfoId);
		log.info("   - startDate: {}", startDate);
		log.info("   - endDate: {}", endDate);
		log.info("   - cursor: {}", cursor);
		log.info("   - idAfter: {}", idAfter);
		log.info("   - sortField: {}", sortField);
		log.info("   - sortDirection: {}", sortDirection);
		log.info("   - size: {}", size);
		log.info("   - referer: {}", referer);
		log.info("   - userAgent: {}",
			userAgent != null ? userAgent.substring(0, Math.min(100, userAgent.length())) : "null");

		Long lastId = null;
		if (cursor != null && !cursor.trim().isEmpty()) {
			try {
				lastId = Long.parseLong(cursor);
				log.info("✅ cursor를 lastId로 변환: {} -> {}", cursor, lastId);
			} catch (NumberFormatException e) {
				log.warn("❌ 잘못된 cursor 형식: {}", cursor);
			}
		} else if (idAfter != null && !idAfter.trim().isEmpty()) {
			try {
				lastId = Long.parseLong(idAfter);
				log.info("✅ idAfter를 lastId로 변환: {} -> {}", idAfter, lastId);
			} catch (NumberFormatException e) {
				log.warn("❌ 잘못된 idAfter 형식: {}", idAfter);
			}
		}

		// 첫 번째 요청인지 확인
		boolean isFirstRequest = (cursor == null || cursor.trim().isEmpty()) &&
			(idAfter == null || idAfter.trim().isEmpty());
		log.info("🎯 요청 타입: {}", isFirstRequest ? "첫 번째 요청" : "무한 스크롤 요청");

		// 페이지네이션된 데이터 조회
		Slice<IndexData> indexDataSlice = indexDataQueryService.getIndexDataList(
			indexInfoId, startDate, endDate, lastId, sortField, sortDirection, size);

		List<IndexDataResponseDto> content = indexDataSlice.getContent().stream()
			.map(IndexDataResponseDto::from)
			.collect(Collectors.toList());

		String nextCursor = null;
		String nextIdAfter = null;
		if (indexDataSlice.hasNext() && !content.isEmpty()) {
			Long lastIdInSlice = content.get(content.size() - 1).getId();
			nextCursor = lastIdInSlice.toString();
			nextIdAfter = lastIdInSlice.toString();
			log.info("🔗 다음 커서 생성: lastIdInSlice={} -> nextCursor={}", lastIdInSlice, nextCursor);
		} else {
			log.info("🚫 다음 페이지 없음: hasNext={}, contentSize={}", indexDataSlice.hasNext(), content.size());
		}

		// 전체 조건에 맞는 실제 총 개수를 DB에서 조회
		long actualTotalElements = indexDataQueryService.getTotalCount(indexInfoId, startDate, endDate);

		// 응답 상세 로깅
		log.info("📊 응답 상세 정보:");
		log.info("   - content.size(): {}", content.size());
		log.info("   - totalElements: {}", actualTotalElements);
		log.info("   - hasNext: {}", indexDataSlice.hasNext());
		log.info("   - nextCursor: {}", nextCursor);
		log.info("   - nextIdAfter: {}", nextIdAfter);

		if (!content.isEmpty()) {
			log.info("   - 첫 번째 아이템 ID: {}", content.get(0).getId());
			log.info("   - 마지막 아이템 ID: {}", content.get(content.size() - 1).getId());
		}

		PagedResponseDto<IndexDataResponseDto> response = PagedResponseDto.<IndexDataResponseDto>builder()
			.content(content)
			.nextCursor(nextCursor)
			.nextIdAfter(nextIdAfter)
			.size(content.size())
			.totalElements((int)actualTotalElements)
			.hasNext(indexDataSlice.hasNext())
			.build();

		log.info("🏁 ===========================================");

		return ResponseEntity.ok(response);
	}

	// 무한 스크롤 테스트용 엔드포인트
	@GetMapping("/test-infinite")
	public ResponseEntity<String> testInfiniteScroll(
		@RequestParam(required = false) Long indexInfoId,
		@RequestParam(required = false) String cursor,
		@RequestParam(required = false, defaultValue = "10") Integer size) {

		StringBuilder result = new StringBuilder();
		result.append("=== 무한 스크롤 테스트 ===\n");

		// 첫 번째 페이지
		if (cursor == null) {
			result.append("1. 첫 번째 페이지 요청\n");
			result.append("   URL: /api/index-data?indexInfoId=")
				.append(indexInfoId)
				.append("&size=")
				.append(size)
				.append("\n");
		} else {
			result.append("2. 다음 페이지 요청\n");
			result.append("   URL: /api/index-data?indexInfoId=")
				.append(indexInfoId)
				.append("&cursor=")
				.append(cursor)
				.append("&size=")
				.append(size)
				.append("\n");
		}

		try {
			Long lastId = cursor != null ? Long.parseLong(cursor) : null;
			Slice<IndexData> slice = indexDataQueryService.getIndexDataList(indexInfoId, null, null, lastId, "baseDate",
				"asc", size);

			result.append("   응답:\n");
			result.append("   - 조회된 데이터 개수: ").append(slice.getContent().size()).append("\n");
			result.append("   - hasNext: ").append(slice.hasNext()).append("\n");

			if (slice.hasNext() && !slice.getContent().isEmpty()) {
				Long nextCursorId = slice.getContent().get(slice.getContent().size() - 1).getId();
				result.append("   - 다음 커서: ").append(nextCursorId).append("\n");
				result.append("   - 다음 요청 URL: /api/index-data/test-infinite?indexInfoId=")
					.append(indexInfoId)
					.append("&cursor=")
					.append(nextCursorId)
					.append("&size=")
					.append(size);
			} else {
				result.append("   - 마지막 페이지입니다.");
			}

		} catch (Exception e) {
			result.append("   오류: ").append(e.getMessage());
		}

		return ResponseEntity.ok(result.toString());
	}
}