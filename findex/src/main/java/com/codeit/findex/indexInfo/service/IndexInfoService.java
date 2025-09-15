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
import com.codeit.findex.indexInfo.repository.IndexInfoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IndexInfoService {

	private final IndexInfoRepository indexInfoRepository;
	private final IndexInfoMapper indexInfoMapper;

	public IndexInfoGetResponseDto getIndexInfos(IndexInfoGetRequestDto dto){
		Pageable pageable = PageRequest.of(0, dto.getSize() + 1);
		Long totalElements = indexInfoRepository.countByFilters(
			dto.getIndexClassification(), dto.getIndexName(), dto.getFavorite()
		);

		Slice<IndexInfo> indexInfosPage = getIndexInfosPageBySortField(dto, pageable);

		List<IndexInfo> actualContent;
		String nextCursor = null;
		String nextIdAfter = null;
		boolean hasNext = false;

		if (indexInfosPage.getContent().size() > dto.getSize()) {
			hasNext = true;
			actualContent = indexInfosPage.getContent().subList(0, dto.getSize());
			IndexInfo lastIndexInfo = actualContent.get(actualContent.size() - 1);
			nextCursor = getCursorValue(lastIndexInfo, dto.getSortField());
			nextIdAfter = lastIndexInfo.getId().toString();
		} else {
			actualContent = indexInfosPage.getContent();
		}

		return indexInfoMapper.toIndexInfoGetResponseDto(actualContent, dto.getSize(), totalElements, nextCursor, nextIdAfter, hasNext);
	}

	private Slice<IndexInfo> getIndexInfosPageBySortField(IndexInfoGetRequestDto dto, Pageable pageable) {
		switch (dto.getSortField()) {
			case "indexClassification":
				return indexInfoRepository.findAllByIndexClassificationCursor(
					dto.getIndexClassification(), dto.getIndexName(), dto.getFavorite(), dto.getIdAfter(),
					dto.getCursor(), dto.getSortDirection(), pageable);

			case "indexName":
				return indexInfoRepository.findAllByIndexNameCursor(
					dto.getIndexClassification(), dto.getIndexName(), dto.getFavorite(), dto.getIdAfter(),
					dto.getCursor(), dto.getSortDirection(), pageable);

			case "employedItemsCount":
				Integer employedItemsCountCursor = parseEmployedItemsCountCursor(dto.getCursor());
				return indexInfoRepository.findAllByEmployedItemsCountCursor(
					dto.getIndexClassification(), dto.getIndexName(), dto.getFavorite(), dto.getIdAfter(),
					employedItemsCountCursor, dto.getSortDirection(), pageable);

			default:
				throw new IllegalArgumentException("Invalid sort field: " + dto.getSortField());
		}
	}

	private Integer parseEmployedItemsCountCursor(String cursor) {
		if (cursor == null || cursor.isEmpty()) {
			return null;
		}
		try {
			return Integer.parseInt(cursor);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid cursor value for employedItemsCount sorting: " + cursor);
		}
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

		if (dto.getEmployedItemsCount() != null) {
			originIndexInfo.setEmployedItemsCount(dto.getEmployedItemsCount());
			originIndexInfo.setSourceType("USER");
		}
		if (dto.getBasePointInTime() != null) {
			originIndexInfo.setBasePointInTime(LocalDate.parse(dto.getBasePointInTime()));
			originIndexInfo.setSourceType("USER");
		}
		if (dto.getBaseIndex() != null) {
			originIndexInfo.setBaseIndex(BigDecimal.valueOf(dto.getBaseIndex()));
			originIndexInfo.setSourceType("USER");
		}
		if (dto.getFavorite() != null) {
			originIndexInfo.setFavorite(dto.getFavorite());
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
