package com.codeit.findex.indexInfo.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndexInfoCreateRequestDto {

	@NotNull(message = "Index Classification is required")
	private String indexClassification;

	@NotNull(message = "Index Name is required")
	private String indexName;

	@NotNull(message = "Employed Items Count is required")
	private Integer employedItemsCount;

	@NotNull(message = "Base Point In Time is required")
	private String basePointInTime;

	@NotNull(message = "Base Index is required")
	private Float baseIndex;

	@NotNull(message = "Source Type is required")
	private Boolean favorite;
}
