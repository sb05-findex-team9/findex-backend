package com.codeit.findex.indexInfo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codeit.findex.indexInfo.service.IndexInfoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/index-infos")
@RequiredArgsConstructor
public class IndexInfoController {
	private final IndexInfoService indexInfoService;
}
