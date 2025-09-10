package com.codeit.findex.indexInfo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndexInfoCreateResponseDto {
	private Integer id;
	private String indexClassification;
	private String indexName;
	private Integer employedItemsCount;
	private String basePointInTime;
	private Float baseIndex;
	private String sourceType;
	private Boolean favorite;
}
