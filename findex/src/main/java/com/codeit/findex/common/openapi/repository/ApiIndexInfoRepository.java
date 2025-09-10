package com.codeit.findex.common.openapi.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codeit.findex.common.openapi.entity.ApiIndexInfo;

public interface ApiIndexInfoRepository extends JpaRepository<ApiIndexInfo, Long> {
	List<ApiIndexInfo> findByIndexNameAndIndexClassification(String indexName, String indexClassification);
}