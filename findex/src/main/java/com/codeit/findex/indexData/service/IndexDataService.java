package com.codeit.findex.indexData.service;

import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codeit.findex.indexData.domain.IndexData;
import com.codeit.findex.indexData.dto.IndexDataRequestDto;
import com.codeit.findex.indexData.dto.IndexDataUpdateRequest;
import com.codeit.findex.indexData.repository.IndexDataRepository;
import com.codeit.findex.indexInfo.domain.IndexInfo;
import com.codeit.findex.indexInfo.repository.IndexInfoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class IndexDataService {
	private final IndexDataRepository indexDataRepository;
	private final IndexInfoRepository indexInfoRepository;

	public IndexData createIndexData(IndexDataRequestDto requestDto) {
		IndexInfo indexInfo = null;
		if (requestDto.getIndexInfoId() != null) {
			indexInfo = indexInfoRepository.findById(requestDto.getIndexInfoId())
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지수 정보 ID입니다: " + requestDto.getIndexInfoId()));
		}
		IndexData indexData = requestDto.toEntity(indexInfo);

		return indexDataRepository.save(indexData);
	}

	public void delete(Long id) {
		indexDataRepository.findById(id)
			.orElseThrow(() -> new NoSuchElementException("ID " + id + "에 해당하는 데이터를 찾을 수 없습니다."));

		indexDataRepository.deleteById(id);
	}

	public IndexData update(Long id, IndexDataUpdateRequest request) {
		IndexData indexData = indexDataRepository.findById(id)
			.orElseThrow(() -> new NoSuchElementException("ID " + id + "에 해당하는 데이터를 찾을 수 없습니다."));

		indexData.update(request);

		return indexData;
	}

}