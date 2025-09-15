package com.codeit.findex.indexInfo.repository.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.codeit.findex.indexInfo.dto.request.IndexInfoGetRequestDto;

@Mapper(componentModel = "spring")
public interface IndexInfoQueryDslMapper {

	@Mapping(target = "sortField", source = "sortField")
	@Mapping(target = "sortDirection", source = "sortDirection")
	@Mapping(target = "indexClassification", source = "indexClassification")
	@Mapping(target = "favorite", source = "favorite")
	@Mapping(target = "indexName", source = "indexName")
	@Mapping(target = "idAfter", source = "idAfter")
	@Mapping(target = "cursor", source = "cursor")
	@Mapping(target = "size", expression = "java(indexInfoGetRequestDto.getSize() + 1)")
	FindAllDto toFindAllDto(IndexInfoGetRequestDto indexInfoGetRequestDto);

}
