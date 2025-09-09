package com.codeit.findex.openApi.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SyncService {

	private final AutoSyncConfigService autoSyncConfigService;
}
