package com.codeit.findex.indexData.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndexChartResponse {
	private Long indexInfoId;
	private String indexClassification;
	private String indexName;
	private String periodType;
	private List<DataPoint> dataPoints;
	private List<DataPoint> ma5DataPoints;
	private List<DataPoint> ma20DataPoints;

	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class DataPoint {
		private String date;
		private Float value;
	}
}