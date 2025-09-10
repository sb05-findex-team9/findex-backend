package com.codeit.findex.indexData;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.codeit.findex.indexInfo.dto.request.IndexInfoCreateRequestDto;
import com.codeit.findex.indexInfo.dto.request.IndexInfoGetRequestDto;
import com.codeit.findex.indexInfo.dto.response.IndexInfoGetResponseDto;
import com.codeit.findex.indexInfo.repository.IndexInfoRepository;
import com.codeit.findex.indexInfo.service.IndexInfoService;

@SpringBootTest
public class IndexInfoServiceTest {

	@Autowired
	private IndexInfoRepository indexInfoRepository;
	@Autowired
	private IndexInfoService indexInfoService;

	@BeforeEach
	@Transactional
	public void setUp() {
		for (int i = 1; i <= 100; i++) {
			IndexInfoCreateRequestDto dto = IndexInfoCreateRequestDto.builder()
				.indexClassification("classification-" + i)
				.indexName("indexName-" + i)
				.employedItemsCount(i)
				.basePointInTime("2025-09-" + String.format("%02d", (i % 30) + 1))
				.baseIndex(i * 1.0f)
				.favorite(i % 2 == 0)
				.build();

			indexInfoService.saveIndexInfo(dto);
		}
	}

	@Test
	public void getIndexInfosTest(){
		IndexInfoGetRequestDto dto = IndexInfoGetRequestDto.builder()
			.indexClassification("classification-10")
			.favorite(true)
			.idAfter(10)
			.sortField("indexClassification")
			.sortDirection("asc")
			.size(10)
			.build();
		IndexInfoGetResponseDto indexInfos = indexInfoService.getIndexInfos(dto);

		System.out.println(indexInfos);
		System.out.println(indexInfos.getContent().size());
		System.out.println(indexInfos.getTotalElements());
		System.out.println(indexInfos.getHasNext());
		System.out.println(indexInfos.getNextIdAfter());
		System.out.println(indexInfos.getNextCursor());
		System.out.println(indexInfos.getSize());
		System.out.println(indexInfos.getContent());
	}

}
