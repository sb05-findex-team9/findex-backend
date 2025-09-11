package com.codeit.findex.indexData.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codeit.findex.indexData.service.IndexDataCsvExportService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/index-data")
@RequiredArgsConstructor
public class IndexDataCsvExportController {

	private final IndexDataCsvExportService csvExportService;

	@GetMapping("/export/csv")
	public ResponseEntity<byte[]> exportIndexDataToCsv(
		@RequestParam(value = "indexInfoId", required = false) Long indexInfoId,

		@RequestParam(value = "startDate", required = false)
		@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,

		@RequestParam(value = "endDate", required = false)
		@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,

		@RequestParam(value = "sortField", defaultValue = "baseDate") String sortField,

		@RequestParam(value = "sortDirection", defaultValue = "desc") String sortDirection) {

		try {
			byte[] csvData = csvExportService.exportToCsv(indexInfoId, startDate, endDate, sortField, sortDirection);

			String fileName = "index_data_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv";

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			headers.setContentDispositionFormData("attachment", fileName);
			headers.add("Accept", "Accept");

			return ResponseEntity.ok()
				.headers(headers)
				.body(csvData);

		} catch (IOException e) {
			return ResponseEntity.internalServerError().build();
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().build();
		}
	}
}