package com.codeit.findex.indexInfo.repository.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class FindAllDto {
	String sortField;
	String sortDirection;
	String indexClassification;
	Boolean favorite;
	String indexName;
	Integer idAfter;
	String cursor;
	int size;
}
