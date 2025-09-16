package com.codeit.findex.common.openapi.controller;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.codeit.findex.common.openapi.service.ApiIndexDataService;
import com.codeit.findex.common.openapi.service.ApiIndexInfoService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ApiDataInitializer implements CommandLineRunner {

	private final ApiIndexInfoService indexInfoService;
	private final ApiIndexDataService indexDataService;

	@Override
	public void run(String... args) throws Exception {
		System.out.println("Starting data collection...");

		System.out.println("Step 1: Collecting IndexInfo data...");
		indexInfoService.fetchAndSaveIndexInfo();

		System.out.println("Step 2: Collecting IndexData...");
		indexDataService.fetchAndSaveIndexData();

		System.out.println("All data collection complete!");
	}
}