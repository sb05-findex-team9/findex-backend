package com.codeit.findex.common.openapi.controller;

import java.util.concurrent.CompletableFuture;

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
		CompletableFuture.runAsync(() -> {
			try {
				System.out.println("Starting data collection in background...");
				System.out.println("Step 1: Collecting IndexInfo data...");
				indexInfoService.fetchAndSaveIndexInfo();

				System.out.println("Step 2: Collecting IndexData...");
				indexDataService.fetchAndSaveIndexData();

				System.out.println("All data collection complete!");
			} catch (Exception e) {
				System.err.println("Error during data collection: " + e.getMessage());
			}
		});
	}
}