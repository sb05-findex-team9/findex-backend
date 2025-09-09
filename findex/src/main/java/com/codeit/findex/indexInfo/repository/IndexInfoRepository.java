package com.codeit.findex.indexInfo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codeit.findex.indexInfo.domain.IndexInfo;

public interface IndexInfoRepository extends JpaRepository<Long, IndexInfo> {
}
