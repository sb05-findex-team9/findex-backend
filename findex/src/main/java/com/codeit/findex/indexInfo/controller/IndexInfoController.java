package com.codeit.findex.indexInfo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codeit.findex.indexInfo.dto.request.IndexInfoCreateRequestDto;
import com.codeit.findex.indexInfo.dto.request.IndexInfoGetRequestDto;
import com.codeit.findex.indexInfo.dto.request.IndexInfoUpdateRequestDto;
import com.codeit.findex.indexInfo.dto.response.IndexInfoCreateResponseDto;
import com.codeit.findex.indexInfo.dto.response.IndexInfoGetByIdResponseDto;
import com.codeit.findex.indexInfo.dto.response.IndexInfoGetResponseDto;
import com.codeit.findex.indexInfo.dto.response.IndexInfoUpdateResponseDto;
import com.codeit.findex.indexInfo.service.IndexInfoService;

import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/index-infos")
@RequiredArgsConstructor
public class IndexInfoController {
	private final IndexInfoService indexInfoService;

	@GetMapping
	public ResponseEntity<IndexInfoGetResponseDto> getAll(
		@ModelAttribute IndexInfoGetRequestDto indexInfoGetRequestDto
	) {
		IndexInfoGetResponseDto indexInfos = indexInfoService.getIndexInfos(indexInfoGetRequestDto);
		return ResponseEntity.ok(indexInfos);
	}

	@PostMapping
	public ResponseEntity<IndexInfoCreateResponseDto> save(
		@RequestBody IndexInfoCreateRequestDto indexInfoCreateRequestDto
	){
		IndexInfoCreateResponseDto indexInfoCreateResponseDto = indexInfoService.saveIndexInfo(
			indexInfoCreateRequestDto);
		return ResponseEntity.ok(indexInfoCreateResponseDto);
	}


	@GetMapping("{id}")
	public ResponseEntity<IndexInfoGetByIdResponseDto> getById(
		@PathVariable Integer id
	){
		IndexInfoGetByIdResponseDto indexInfoById = indexInfoService.getIndexInfoById(id);
		return ResponseEntity.ok(indexInfoById);
	}

	@DeleteMapping("{id}")
	public ResponseEntity<Void> deleteById(@PathVariable Integer id){

		indexInfoService.deleteIndexInfoById(id);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@PatchMapping("{id}")
	public ResponseEntity<IndexInfoUpdateResponseDto> updateById(
		@PathVariable Integer id,
		@RequestBody IndexInfoUpdateRequestDto indexInfoUpdateRequestDto
	){
		IndexInfoUpdateResponseDto indexInfoUpdateResponseDto = indexInfoService.updateIndexInfo(id,
			indexInfoUpdateRequestDto);
		return ResponseEntity.ok(indexInfoUpdateResponseDto);
	}
}
