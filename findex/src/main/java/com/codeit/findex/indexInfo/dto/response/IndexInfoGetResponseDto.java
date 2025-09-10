package com.codeit.findex.indexInfo.dto.response;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndexInfoGetResponseDto {

	@Builder.Default
	private List<IndexInfoDto> content = new ArrayList<>();
	private String nextCursor;
	private String nextIdAfter;
	private Integer size;
	private Long totalElements;
	private Boolean hasNext;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class IndexInfoDto {
		private Long id;
		private String indexClassification;
		private String indexName;
		private Integer employedItemsCount;
		private String basePointInTime;
		private Integer baseIndex;
		private String sourceType;
		private Boolean favorite;
	}
}