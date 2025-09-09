package com.codeit.findex.indexInfo.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.codeit.findex.indexInfo.domain.IndexInfo;
import com.codeit.findex.indexInfo.dto.request.IndexInfoCreateRequestDto;
import com.codeit.findex.indexInfo.dto.request.IndexInfoGetRequestDto;
import com.codeit.findex.indexInfo.dto.response.IndexInfoCreateResponseDto;
import com.codeit.findex.indexInfo.dto.response.IndexInfoGetResponseDto;
import com.codeit.findex.indexInfo.mapper.IndexInfoMapper;
import com.codeit.findex.indexInfo.repository.IndexInfoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IndexInfoService {

	private final IndexInfoRepository indexInfoRepository;
	private final IndexInfoMapper indexInfoMapper;

	public IndexInfoGetResponseDto getIndexInfos(IndexInfoGetRequestDto dto){
		Pageable pageable = PageRequest.of(0, dto.getSize());

		Page<IndexInfo> indexInfosPage;
		IndexInfo lastIndexInfo;
		String nextCursor = null;
		String nextIdAfter = null;

		switch (dto.getSortField()){
			case "indexClassification":
				indexInfosPage = indexInfoRepository.findAllByIndexClassificationCursor(
					dto.getIndexClassification(), dto.getIndexName(), dto.getFavorite(), dto.getIdAfter(),
					dto.getCursor(),dto.getSortDirection() ,pageable);

				if (!indexInfosPage.getContent().isEmpty()) {
					lastIndexInfo = indexInfosPage.getContent().get(indexInfosPage.getContent().size() - 1);
					if(indexInfosPage.hasNext()) {
						nextCursor = lastIndexInfo.getIndexClassification();
						nextIdAfter = lastIndexInfo.getId().toString();
					}
				}
				break;

			case "indexName":
				indexInfosPage = indexInfoRepository.findAllByIndexNameCursor(
					dto.getIndexClassification(), dto.getIndexName(), dto.getFavorite(), dto.getIdAfter(),
					dto.getCursor(),dto.getSortDirection(), pageable);

				if (!indexInfosPage.getContent().isEmpty()) {
					lastIndexInfo = indexInfosPage.getContent().get(indexInfosPage.getContent().size() - 1);
					if(indexInfosPage.hasNext()) {
						nextCursor = lastIndexInfo.getIndexName();
						nextIdAfter = lastIndexInfo.getId().toString();
					}
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

				if (!indexInfosPage.getContent().isEmpty()) {
					lastIndexInfo = indexInfosPage.getContent().get(indexInfosPage.getContent().size() - 1);
					if(indexInfosPage.hasNext()) {
						nextCursor = lastIndexInfo.getEmployedItemsCount() != null ?
							lastIndexInfo.getEmployedItemsCount().toString() : null;
						nextIdAfter = lastIndexInfo.getId().toString();
					}
				}
				break;

			default:
				throw new IllegalArgumentException("Invalid sort field: " + dto.getSortField());
		}

		return indexInfoMapper.toIndexInfoGetResponseDto(indexInfosPage, nextCursor, nextIdAfter);
	}

	public IndexInfoCreateResponseDto saveIndexInfo(IndexInfoCreateRequestDto dto){
		IndexInfo indexInfo = indexInfoRepository.save(indexInfoMapper.toIndexInfo(dto));
		return indexInfoMapper.toIndexInfoCreateResponseDto(indexInfo);
	}


}
