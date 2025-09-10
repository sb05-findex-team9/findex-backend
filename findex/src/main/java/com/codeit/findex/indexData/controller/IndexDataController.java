package com.codeit.findex.indexData.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codeit.findex.indexData.domain.IndexData;
import com.codeit.findex.indexData.dto.IndexDataRequestDto;
import com.codeit.findex.indexData.dto.IndexDataResponseDto;
import com.codeit.findex.indexData.service.IndexDataService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/index-data")
@RequiredArgsConstructor
public class IndexDataController {
	private final IndexDataService indexDataService;

	// 지수 데이터 등록
	@PostMapping
	public ResponseEntity<IndexDataResponseDto> createIndexData(@RequestBody IndexDataRequestDto requestDto) {
		try {
			log.info("지수 데이터 등록 요청: {}", requestDto);
			IndexData createdIndexData = indexDataService.createIndexData(requestDto);
			IndexDataResponseDto response = IndexDataResponseDto.from(createdIndexData);
			return ResponseEntity.status(HttpStatus.CREATED).body(response);
		} catch (IllegalArgumentException e) {
			log.error("지수 데이터 등록 실패: {}", e.getMessage());
			throw e;
		} catch (Exception e) {
			log.error("지수 데이터 등록 중 예상치 못한 오류 발생", e);
			throw new RuntimeException("지수 데이터 등록 중 오류가 발생했습니다.");
		}
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
		return ResponseEntity.badRequest().body(e.getMessage());
	}

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
		return ResponseEntity.internalServerError().body(e.getMessage());
	}
}