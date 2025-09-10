package com.codeit.findex.indexInfo.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndexInfoGetRequestDto {

	private String indexClassification;

	private String indexName;

	private Boolean favorite;

	private Integer idAfter;

	private String cursor;

	@Builder.Default
	private String sortField = "indexClassification";

	@Builder.Default
	private String sortDirection ="asc";

	@Builder.Default
	private Integer size = 10;
}
