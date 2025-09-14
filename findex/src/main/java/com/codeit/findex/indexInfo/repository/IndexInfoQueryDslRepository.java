package com.codeit.findex.indexInfo.repository;

import java.util.List;

import com.codeit.findex.indexInfo.domain.IndexInfo;
import com.codeit.findex.indexInfo.repository.dto.FindAllDto;

public interface IndexInfoQueryDslRepository {

	List<IndexInfo> findAllByConditionWithPage(FindAllDto findAllDto);
}
