package com.codeit.findex.openApi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AutoSyncConfigDto {

	private Long id;
	private Long indexInfoId;
	private String indexClassification;
	private String indexName;
	private Boolean enabled;

}
