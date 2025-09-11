package com.codeit.findex.openApi.dto.request;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record IndexDataSyncRequest(
	@JsonProperty("indexInfoIds")
	List<Long> indexInfoIds,

	@JsonProperty("baseDateFrom")
	LocalDate baseDateFrom,

	@JsonProperty("baseDateTo")
	LocalDate baseDateTo
) {
	public Long getFirstIndexInfoId() {
		return (indexInfoIds != null && !indexInfoIds.isEmpty()) ? indexInfoIds.get(0) : null;
	}
}