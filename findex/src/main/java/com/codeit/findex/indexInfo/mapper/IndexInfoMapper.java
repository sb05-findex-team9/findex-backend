package com.codeit.findex.indexInfo.mapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;

import com.codeit.findex.indexInfo.domain.IndexInfo;
import com.codeit.findex.indexInfo.dto.request.IndexInfoCreateRequestDto;
import com.codeit.findex.indexInfo.dto.response.IndexInfoCreateResponseDto;
import com.codeit.findex.indexInfo.dto.response.IndexInfoGetResponseDto;

@Mapper(componentModel = "spring")
public interface IndexInfoMapper {

	// IndexInfoGetResponseDto.IndexInfoDto toIndexInfoDto(IndexInfo indexInfo);

	@Mapping(target = "content", source = "page.content")
	@Mapping(target = "size", source = "page.size")
	@Mapping(target = "totalElements", source = "page.totalElements")
	@Mapping(target = "hasNext", expression = "java(page.hasNext())")
	@Mapping(target = "nextCursor", source = "nextCursor")
	@Mapping(target = "nextIdAfter", source = "nextIdAfter")
	IndexInfoGetResponseDto toIndexInfoGetResponseDto(Page<IndexInfo> page, String nextCursor, String nextIdAfter);


	@Mapping(target="id", ignore = true)
	@Mapping(target="indexData", ignore = true)
	@Mapping(target="autoSyncConfig", ignore = true)
	@Mapping(target="syncJobs", ignore = true)
	@Mapping(target="sourceType", ignore = true)
	IndexInfo toIndexInfo(IndexInfoCreateRequestDto dto);


	IndexInfoCreateResponseDto toIndexInfoCreateResponseDto(IndexInfo indexInfo);
}

