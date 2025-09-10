package com.codeit.findex.indexData.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndexPerformanceRankResponse {
	private Performance performance;
	private Integer rank;

	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Performance {
		private Long indexInfoId;
		private String indexClassification;
		private String indexName;
		private Float versus;
		private Float fluctuationRate;
		private Float currentPrice;
		private Float beforePrice;
	}
}