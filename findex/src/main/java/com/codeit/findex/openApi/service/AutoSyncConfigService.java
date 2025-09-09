package com.codeit.findex.openApi.service;

import org.springframework.stereotype.Service;

import com.codeit.findex.openApi.repository.AutoSyncConfigRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AutoSyncConfigService {

	private final AutoSyncConfigRepository autoSyncConfigRepository;
}
