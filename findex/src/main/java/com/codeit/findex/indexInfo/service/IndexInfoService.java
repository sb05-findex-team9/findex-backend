package com.codeit.findex.indexInfo.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.codeit.findex.indexInfo.domain.IndexInfo;
import com.codeit.findex.indexInfo.dto.request.IndexInfoCreateRequestDto;
import com.codeit.findex.indexInfo.dto.request.IndexInfoGetRequestDto;
import com.codeit.findex.indexInfo.dto.request.IndexInfoUpdateRequestDto;
import com.codeit.findex.indexInfo.dto.response.IndexInfoCreateResponseDto;
import com.codeit.findex.indexInfo.dto.response.IndexInfoGetByIdResponseDto;
import com.codeit.findex.indexInfo.dto.response.IndexInfoGetResponseDto;
import com.codeit.findex.indexInfo.dto.response.IndexInfoSummaryResponseDto;
import com.codeit.findex.indexInfo.dto.response.IndexInfoUpdateResponseDto;
import com.codeit.findex.indexInfo.mapper.IndexInfoMapper;
import com.codeit.findex.indexInfo.repository.IndexInfoQueryDslRepositoryImpl;
import com.codeit.findex.indexInfo.repository.IndexInfoRepository;
import com.codeit.findex.indexInfo.repository.dto.FindAllDto;
import com.codeit.findex.indexInfo.repository.dto.IndexInfoQueryDslMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IndexInfoService {

	private final IndexInfoRepository indexInfoRepository;
	private final IndexInfoMapper indexInfoMapper;
	private final IndexInfoQueryDslMapper indexInfoQueryDslMapper;

	public IndexInfoGetResponseDto getIndexInfos(IndexInfoGetRequestDto dto) {
		FindAllDto findAllDto = indexInfoQueryDslMapper.toFindAllDto(dto);
		Long totalElements = indexInfoRepository.countByFilters(
			dto.getIndexClassification(), dto.getIndexName(), dto.getFavorite()
		);

		List<IndexInfo> indexInfos = indexInfoRepository.findAllByConditionWithPage(findAllDto);

		List<IndexInfo> actualContent;
		String nextCursor = null;
		String nextIdAfter = null;
		boolean hasNext = false;

		if (indexInfos.size() > dto.getSize()) {
			hasNext = true;
			actualContent = indexInfos.subList(0, dto.getSize());
			IndexInfo lastIndexInfo = actualContent.get(actualContent.size() - 1);
			nextCursor = getCursorValue(lastIndexInfo, dto.getSortField());
			nextIdAfter = lastIndexInfo.getId().toString();
		} else {
			actualContent = indexInfos;
		}

		return indexInfoMapper.toIndexInfoGetResponseDto(
			actualContent, dto.getSize(), totalElements, nextCursor, nextIdAfter, hasNext
		);
	}



	private String getCursorValue(IndexInfo indexInfo, String sortField) {
		switch (sortField) {
			case "indexClassification":
				return indexInfo.getIndexClassification();
			case "indexName":
				return indexInfo.getIndexName();
			case "employedItemsCount":
				return indexInfo.getEmployedItemsCount() != null ?
					indexInfo.getEmployedItemsCount().toString() : null;
			default:
				throw new IllegalArgumentException("Invalid sort field: " + sortField);
		}
	}


	public IndexInfoCreateResponseDto saveIndexInfo(IndexInfoCreateRequestDto dto){
		IndexInfo indexInfo = indexInfoRepository.save(indexInfoMapper.toIndexInfo(dto, "USER"));
		return indexInfoMapper.toIndexInfoCreateResponseDto(indexInfo);
	}

	public IndexInfoGetByIdResponseDto getIndexInfoById(Integer id){
		IndexInfo indexInfo = indexInfoRepository.getIndexInfoById(Long.valueOf(id))
			.orElseThrow(NoSuchElementException::new);

		return indexInfoMapper.toIndexInfoGetByIdResponseDto(indexInfo);
	}

	public void deleteIndexInfoById(Integer id){
		IndexInfo indexInfoOptional = indexInfoRepository.getIndexInfoById(Long.valueOf(id)).orElseThrow(NoSuchElementException::new);
		indexInfoRepository.delete(indexInfoOptional);
	}

	public IndexInfoUpdateResponseDto updateIndexInfo(Integer id, IndexInfoUpdateRequestDto dto){
		IndexInfo originIndexInfo = indexInfoRepository.getIndexInfoById(Long.valueOf(id))
			.orElseThrow(NoSuchElementException::new);

		boolean isChanged = false;

		if (dto.getEmployedItemsCount() != null) {
			originIndexInfo.setEmployedItemsCount(dto.getEmployedItemsCount());
			isChanged = true;
		}
		if (dto.getBasePointInTime() != null) {
			originIndexInfo.setBasePointInTime(LocalDate.parse(dto.getBasePointInTime()));
			isChanged = true;
		}
		if (dto.getBaseIndex() != null) {
			originIndexInfo.setBaseIndex(BigDecimal.valueOf(dto.getBaseIndex()));
			isChanged = true;
		}
		if (dto.getFavorite() != null) {
			originIndexInfo.setFavorite(dto.getFavorite());
			isChanged = true;
		}

		if(isChanged){
			originIndexInfo.setSourceType("USER");
		}

		IndexInfo updatedIndexInfo = indexInfoRepository.save(originIndexInfo);

		return indexInfoMapper.toIndexInfoUpdateResponseDto(
			updatedIndexInfo);
	}

	public List<IndexInfoSummaryResponseDto> getSummarizedIndexInfos(){
		List<IndexInfoSummaryResponseDto> summarizedIndexInfos = new ArrayList<>();
		List<IndexInfo> indexInfos = indexInfoRepository.findAll();
		for (IndexInfo indexInfo : indexInfos) {
			summarizedIndexInfos.add(indexInfoMapper.toIndexInfoSummaryResponseDto(indexInfo));
		}
		return summarizedIndexInfos;
	}
}
