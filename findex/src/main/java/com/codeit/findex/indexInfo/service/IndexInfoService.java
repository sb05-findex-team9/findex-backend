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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.codeit.findex.indexInfo.domain.IndexInfo;
import com.codeit.findex.indexInfo.dto.request.IndexInfoCreateRequestDto;
import com.codeit.findex.indexInfo.dto.request.IndexInfoGetRequestDto;
import com.codeit.findex.indexInfo.dto.request.IndexInfoUpdateRequestDto;
import com.codeit.findex.indexInfo.dto.response.IndexInfoCreateResponseDto;
import com.codeit.findex.indexInfo.dto.response.IndexInfoGetByIdResponseDto;
import com.codeit.findex.indexInfo.dto.response.IndexInfoGetResponseDto;
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

		Page<IndexInfo> indexInfosPage;
		String nextCursor = null;
		String nextIdAfter = null;
		boolean hasNext = false;
		List<IndexInfo> actualContent;
		Long totalElements;

		totalElements = indexInfoRepository.countByFilters(
			dto.getIndexClassification(), dto.getIndexName(), dto.getFavorite()
		);

		switch (dto.getSortField()){
			case "indexClassification":
				indexInfosPage = indexInfoRepository.findAllByIndexClassificationCursor(
					dto.getIndexClassification(), dto.getIndexName(), dto.getFavorite(), dto.getIdAfter(),
					dto.getCursor(),dto.getSortDirection() ,pageable);

				if (indexInfosPage.getContent().size() > dto.getSize()) {
					hasNext = true;
					actualContent = indexInfosPage.getContent().subList(0, dto.getSize());
					IndexInfo lastIndexInfo = actualContent.get(actualContent.size() - 1);
					nextCursor = lastIndexInfo.getIndexClassification();
					nextIdAfter = lastIndexInfo.getId().toString();
				} else {
					actualContent = indexInfosPage.getContent();
				}
				break;

			case "indexName":
				indexInfosPage = indexInfoRepository.findAllByIndexNameCursor(
					dto.getIndexClassification(), dto.getIndexName(), dto.getFavorite(), dto.getIdAfter(),
					dto.getCursor(),dto.getSortDirection(), pageable);

				if (indexInfosPage.getContent().size() > dto.getSize()) {
					hasNext = true;
					actualContent = indexInfosPage.getContent().subList(0, dto.getSize());
					IndexInfo lastIndexInfo = actualContent.get(actualContent.size() - 1);
					nextCursor = lastIndexInfo.getIndexName();
					nextIdAfter = lastIndexInfo.getId().toString();
				} else {
					actualContent = indexInfosPage.getContent();
				}
				break;

			case "employedItemsCount":
				Integer employedItemsCountCursor = null;
				if (dto.getCursor() != null && !dto.getCursor().isEmpty()) {
					try {
						employedItemsCountCursor = Integer.parseInt(dto.getCursor());
					} catch (NumberFormatException e) {
						throw new IllegalArgumentException("Invalid cursor value for employedItemsCount sorting: " + dto.getCursor());
					}
				}

				indexInfosPage = indexInfoRepository.findAllByEmployedItemsCountCursor(
					dto.getIndexClassification(), dto.getIndexName(), dto.getFavorite(), dto.getIdAfter(),
					employedItemsCountCursor,dto.getSortDirection(), pageable);

				if (indexInfosPage.getContent().size() > dto.getSize()) {
					hasNext = true;
					actualContent = indexInfosPage.getContent().subList(0, dto.getSize());
					IndexInfo lastIndexInfo = actualContent.get(actualContent.size() - 1);
					nextCursor = lastIndexInfo.getEmployedItemsCount() != null ?
						lastIndexInfo.getEmployedItemsCount().toString() : null;
					nextIdAfter = lastIndexInfo.getId().toString();
				} else {
					actualContent = indexInfosPage.getContent();
				}
				break;

			default:
				throw new IllegalArgumentException("Invalid sort field: " + dto.getSortField());
		}

		return indexInfoMapper.toIndexInfoGetResponseDto(actualContent, dto.getSize(), totalElements, nextCursor, nextIdAfter, hasNext);
	}

	public IndexInfoCreateResponseDto saveIndexInfo(IndexInfoCreateRequestDto dto){
		IndexInfo indexInfo = indexInfoRepository.save(indexInfoMapper.toIndexInfo(dto));
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
		}
		if (dto.getBasePointInTime() != null) {
			originIndexInfo.setBasePointInTime(LocalDate.parse(dto.getBasePointInTime()));
		}
		if (dto.getBaseIndex() != null) {
			originIndexInfo.setBaseIndex(BigDecimal.valueOf(dto.getBaseIndex()));
		}
		if (dto.getFavorite() != null) {
			originIndexInfo.setFavorite(dto.getFavorite());
		}

		IndexInfo updatedIndexInfo = indexInfoRepository.save(originIndexInfo);

		return indexInfoMapper.toIndexInfoUpdateResponseDto(
			updatedIndexInfo);
	}

}
