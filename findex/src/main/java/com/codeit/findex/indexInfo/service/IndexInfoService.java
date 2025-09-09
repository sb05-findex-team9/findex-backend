package com.codeit.findex.indexInfo.service;

import org.springframework.stereotype.Service;

import com.codeit.findex.indexInfo.repository.IndexInfoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IndexInfoService {

	private final IndexInfoRepository indexInfoRepository;
}
