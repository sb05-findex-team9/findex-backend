package com.codeit.findex.indexData.service;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import com.codeit.findex.indexData.repository.IndexDataRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequestMapping("/api/index-data")
@RequiredArgsConstructor
public class IndexDataService {
	private final IndexDataRepository indexDataRepository;
}
