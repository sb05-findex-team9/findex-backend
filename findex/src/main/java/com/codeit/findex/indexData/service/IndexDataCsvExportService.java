package com.codeit.findex.indexData.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codeit.findex.indexData.domain.IndexData;
import com.codeit.findex.indexData.repository.IndexDataRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IndexDataCsvExportService {

	private final IndexDataRepository indexDataRepository;

	public byte[] exportToCsv(Long indexInfoId, LocalDate startDate, LocalDate endDate,
		String sortField, String sortDirection) throws IOException {

		Sort sort = createSort(sortField, sortDirection);

		List<IndexData> indexDataList = getFilteredIndexData(indexInfoId, startDate, endDate, sort);

		return generateCsv(indexDataList);
	}

	private Sort createSort(String sortField, String sortDirection) {
		Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection)
			? Sort.Direction.ASC
			: Sort.Direction.DESC;

		if (sortField == null || sortField.trim().isEmpty()) {
			sortField = "baseDate";
		}

		return Sort.by(direction, sortField);
	}

	private List<IndexData> getFilteredIndexData(Long indexInfoId, LocalDate startDate,
		LocalDate endDate, Sort sort) {
		if (indexInfoId != null) {
			return indexDataRepository.findByIndexInfoIdAndBaseDateBetweenOrderBy(
				indexInfoId, startDate, endDate, sort);
		} else {
			return indexDataRepository.findByBaseDateBetweenOrderBy(startDate, endDate, sort);
		}
	}

	private byte[] generateCsv(List<IndexData> indexDataList) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {

			String[] headers = {
				"ID", "지수정보ID", "기준일자", "소스타입", "시가", "종가",
				"고가", "저가", "전일대비", "등락률", "거래량", "거래대금", "상장시가총액"
			};
			writer.println(String.join(",", headers));

			DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

			for (IndexData data : indexDataList) {
				String[] row = {
					String.valueOf(data.getId()),
					String.valueOf(data.getIndexInfo().getId()),
					data.getBaseDate().format(dateFormatter),
					escapeCsv(data.getSourceType()),
					formatBigDecimal(data.getMarketPrice()),
					formatBigDecimal(data.getClosingPrice()),
					formatBigDecimal(data.getHighPrice()),
					formatBigDecimal(data.getLowPrice()),
					formatBigDecimal(data.getVersus()),
					formatBigDecimal(data.getFluctuationRate()),
					String.valueOf(data.getTradingQuantity()),
					formatBigDecimal(data.getTradingPrice()),
					formatBigDecimal(data.getMarketTotalAmount())
				};
				writer.println(String.join(",", row));
			}
		}

		return outputStream.toByteArray();
	}

	private String formatBigDecimal(BigDecimal value) {
		return value != null ? value.toPlainString() : "";
	}

	private String escapeCsv(String value) {
		if (value == null) return "";
		if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
			return "\"" + value.replace("\"", "\"\"") + "\"";
		}
		return value;
	}
}
