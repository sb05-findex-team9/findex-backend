package com.codeit.findex.openApi.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PagedSyncJobResponse {
	private List<SyncJobResponse> content;
	private String nextCursor;
	private String nextIdAfter;
	private Integer size;
	private Integer totalElements;
	private Boolean hasNext;
}