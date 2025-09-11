package com.codeit.findex.openApi.dto.response;

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
	private Long totalElements;
	private Boolean hasNext;
}