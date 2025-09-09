package com.codeit.findex.openApi.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codeit.findex.openApi.service.SyncService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/sync-jobs")
@RequiredArgsConstructor
public class SyncJobController {

	private final SyncService syncService;
}
