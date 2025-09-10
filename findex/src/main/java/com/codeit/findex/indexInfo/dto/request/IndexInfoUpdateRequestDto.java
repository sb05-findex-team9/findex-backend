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
public class IndexInfoUpdateRequestDto {

	private Integer employedItemsCount;

	private String basePointInTime;

	private Float baseIndex;

	private Boolean favorite;
}
