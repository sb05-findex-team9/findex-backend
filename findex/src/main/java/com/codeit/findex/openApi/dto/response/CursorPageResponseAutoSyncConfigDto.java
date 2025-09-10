package com.codeit.findex.openApi.dto.response;

import java.util.List;

import com.codeit.findex.openApi.dto.AutoSyncConfigDto;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.ALWAYS) // 무조건 JSON에 포함. 값이 null이든 기본값이든 상관없이 키는 항상 나감.
public class CursorPageResponseAutoSyncConfigDto {

	@Builder.Default
	private List<AutoSyncConfigDto> content = List.of();

	@Builder.Default
	private String nextCursor = "";

	@Builder.Default
	private Long nextIdAfter = 0L;

	@Builder.Default
	private Integer size = 0;

	@Builder.Default
	private Long totalElements = 0L;

	@Builder.Default
	private Boolean hasNext = false;


	// private List<AutoSyncConfigDto> content;
	//
	//
	// private String nextCursor;
	//
	//
	// private Long nextIdAfter;
	//
	//
	// private Integer size;
	//
	//
	// private Long totalElements;
	//
	//
	// private Boolean hasNext;

}
