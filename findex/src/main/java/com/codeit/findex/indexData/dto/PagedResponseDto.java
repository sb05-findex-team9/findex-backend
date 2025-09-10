package com.codeit.findex.indexData.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponseDto<T> {
	private List<T> content;
	private String nextCursor;
	private String nextIdAfter;
	private Integer size;
	private Integer totalElements;
	private boolean hasNext;
}