package com.codeit.findex.openApi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codeit.findex.openApi.service.AutoSyncConfigService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auto-sync-configs")
@RequiredArgsConstructor
public class AutoSyncConfigController {
	private final AutoSyncConfigService autoSyncConfigService;

	@GetMapping
	public String test() {
		return "hiu";
	}
}
