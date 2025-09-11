package com.codeit.findex.indexData.dto;

import java.math.BigDecimal;

import com.codeit.findex.indexInfo.domain.IndexInfo;

public record IndexFavoritePerformance(
	Long indexInfoId,
	String indexName,
	String indexClassification,
	BigDecimal versus,
	BigDecimal fluctuationRate,
	BigDecimal currentPrice,
	BigDecimal beforePrice
) {
	public static IndexFavoritePerformance of(IndexInfo indexInfo, BigDecimal versus,
		BigDecimal fluctuationRate, BigDecimal currentPrice,
		BigDecimal beforePrice) {
		return new IndexFavoritePerformance(
			indexInfo.getId(),
			indexInfo.getIndexName(),
			indexInfo.getIndexClassification(),
			versus,
			fluctuationRate,
			currentPrice,
			beforePrice
		);
	}
}
