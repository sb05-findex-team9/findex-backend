package com.codeit.findex.indexInfo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codeit.findex.indexInfo.dto.request.IndexInfoCreateRequestDto;
import com.codeit.findex.indexInfo.dto.request.IndexInfoGetRequestDto;
import com.codeit.findex.indexInfo.dto.response.IndexInfoCreateResponseDto;
import com.codeit.findex.indexInfo.dto.response.IndexInfoGetResponseDto;
import com.codeit.findex.indexInfo.service.IndexInfoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/index-infos")
@RequiredArgsConstructor
public class IndexInfoController {
	private final IndexInfoService indexInfoService;

	@GetMapping
	public IndexInfoGetResponseDto getAllIndexInfos(
		@ModelAttribute IndexInfoGetRequestDto indexInfoGetRequestDto
	) {
		return indexInfoService.getIndexInfos(indexInfoGetRequestDto);
	}

	@PostMapping
	public IndexInfoCreateResponseDto save(
		@RequestBody IndexInfoCreateRequestDto indexInfoCreateRequestDto
	){
		return indexInfoService.saveIndexInfo(indexInfoCreateRequestDto);
	}

}
