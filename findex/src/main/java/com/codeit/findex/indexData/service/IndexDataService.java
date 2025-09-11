package com.codeit.findex.indexData.service;

import java.util.NoSuchElementException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.codeit.findex.indexData.domain.IndexData;
import com.codeit.findex.indexData.dto.IndexDataRequestDto;
import com.codeit.findex.indexData.dto.IndexDataUpdateRequest;
import com.codeit.findex.indexData.repository.IndexDataRepository;
import com.codeit.findex.indexInfo.domain.IndexInfo;
import com.codeit.findex.indexInfo.repository.IndexInfoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class IndexDataService {
	private final IndexDataRepository indexDataRepository;
	private final IndexInfoRepository indexInfoRepository;

	public IndexData createIndexData(IndexDataRequestDto requestDto) {
		// 필수 필드 검증
		if (requestDto.getIndexInfoId() == null) {
			throw new IllegalArgumentException("지수 정보 ID는 필수입니다.");
		}

		if (requestDto.getBaseDate() == null) {
			throw new IllegalArgumentException("기준일자는 필수입니다.");
		}

		// sourceType 검증 및 기본값 설정
		if (!StringUtils.hasText(requestDto.getSourceType())) {
			log.warn("sourceType이 null 또는 빈 값입니다. 기본값 '사용자'로 설정합니다.");
		}

		// IndexInfo 조회 및 검증
		IndexInfo indexInfo = indexInfoRepository.findById(requestDto.getIndexInfoId())
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지수 정보입니다."));

		// 중복 데이터 체크 (같은 지수, 같은 날짜)
		boolean exists = indexDataRepository.findByIndexInfoIdAndBaseDate(
			requestDto.getIndexInfoId(),
			requestDto.getBaseDate()
		).isPresent();

		if (exists) {
			throw new IllegalArgumentException("중복 데이터");
		}

		// 엔티티 생성
		IndexData indexData = requestDto.toEntity(indexInfo);

		log.info("지수 데이터 등록: indexInfoId={}, baseDate={}, sourceType={}",
			requestDto.getIndexInfoId(), requestDto.getBaseDate(), indexData.getSourceType());

		try {
			return indexDataRepository.save(indexData);
		} catch (DataIntegrityViolationException e) {
			log.error("데이터 무결성 위반: {}", e.getMessage());
			throw new IllegalArgumentException("데이터 저장 실패");
		}
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