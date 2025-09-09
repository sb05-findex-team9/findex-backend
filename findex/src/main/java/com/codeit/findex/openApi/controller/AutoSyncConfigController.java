package com.codeit.findex.openApi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.codeit.findex.openApi.service.AutoSyncConfigService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/api/auto-sync-configs")
@RequiredArgsConstructor
public class AutoSyncConfigController {
	private final AutoSyncConfigService autoSyncConfigService;
}
