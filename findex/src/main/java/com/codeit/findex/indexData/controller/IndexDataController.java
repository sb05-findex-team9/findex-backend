package com.codeit.findex.indexData.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codeit.findex.indexData.domain.IndexData;
import com.codeit.findex.indexData.dto.IndexDataRequestDto;
import com.codeit.findex.indexData.dto.IndexDataResponseDto;
import com.codeit.findex.indexData.dto.IndexDataUpdateRequest;
import com.codeit.findex.indexData.dto.IndexDataUpdateResponse;
import com.codeit.findex.indexData.service.IndexDataService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/index-data")
@RequiredArgsConstructor
public class IndexDataController {
	private final IndexDataService indexDataService;

	// 지수 데이터 등록
	@PostMapping
	public ResponseEntity<IndexDataResponseDto> createIndexData(@RequestBody IndexDataRequestDto requestDto) {
		IndexData createdIndexData = indexDataService.createIndexData(requestDto);
		IndexDataResponseDto response = IndexDataResponseDto.from(createdIndexData);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	// 지수 데이터 삭제
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		indexDataService.delete(id);
		return ResponseEntity.noContent().build();
	}

	// 지수 데이터 수정
	@PatchMapping("/{id}")
	public ResponseEntity<IndexDataUpdateResponse> update(@PathVariable Long id, @RequestBody IndexDataUpdateRequest request) {
		IndexData updatedIndexData = indexDataService.update(id, request);
		return ResponseEntity.ok(IndexDataUpdateResponse.from(updatedIndexData));
	}

}