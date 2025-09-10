package com.codeit.findex.indexInfo.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.codeit.findex.indexInfo.domain.IndexInfo;
import com.codeit.findex.indexInfo.dto.request.IndexInfoCreateRequestDto;
import com.codeit.findex.indexInfo.dto.response.IndexInfoCreateResponseDto;
import com.codeit.findex.indexInfo.dto.response.IndexInfoGetByIdResponseDto;
import com.codeit.findex.indexInfo.dto.response.IndexInfoGetResponseDto;
import com.codeit.findex.indexInfo.dto.response.IndexInfoSummaryResponseDto;
import com.codeit.findex.indexInfo.dto.response.IndexInfoUpdateResponseDto;

@Mapper(componentModel = "spring")
public interface IndexInfoMapper {


	@Mapping(target = "content", source = "content")
	@Mapping(target = "size", source = "size")
	@Mapping(target = "totalElements", source = "totalElements")
	@Mapping(target = "hasNext", source = "hasNext")
	@Mapping(target = "nextCursor", source = "nextCursor")
	@Mapping(target = "nextIdAfter", source = "nextIdAfter")
	IndexInfoGetResponseDto toIndexInfoGetResponseDto(List<IndexInfo> content, Integer size, Long totalElements, String nextCursor, String nextIdAfter, boolean hasNext);


	@Mapping(target="id", ignore = true)
	@Mapping(target="indexData", ignore = true)
	@Mapping(target="autoSyncConfig", ignore = true)
	@Mapping(target="syncJobs", ignore = true)
	@Mapping(target="sourceType", source="sourceType")
	IndexInfo toIndexInfo(IndexInfoCreateRequestDto dto, String sourceType);


	IndexInfoCreateResponseDto toIndexInfoCreateResponseDto(IndexInfo indexInfo);

	IndexInfoGetByIdResponseDto toIndexInfoGetByIdResponseDto(IndexInfo indexInfo);

	IndexInfoUpdateResponseDto toIndexInfoUpdateResponseDto(IndexInfo indexInfo);

	IndexInfoSummaryResponseDto toIndexInfoSummaryResponseDto(IndexInfo indexInfo);

}

