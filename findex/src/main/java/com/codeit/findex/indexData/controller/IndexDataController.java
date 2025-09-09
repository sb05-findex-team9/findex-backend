package com.codeit.findex.indexData.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codeit.findex.indexData.service.IndexDataService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/index-data")
@RequiredArgsConstructor
public class IndexDataController {
	private final IndexDataService indexDataService;
}
