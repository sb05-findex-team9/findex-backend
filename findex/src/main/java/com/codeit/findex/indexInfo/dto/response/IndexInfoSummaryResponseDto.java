package com.codeit.findex.indexInfo.dto.
	response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndexInfoSummaryResponseDto {
	private Long id;
	private String indexClassification;
	private String indexName;
}
