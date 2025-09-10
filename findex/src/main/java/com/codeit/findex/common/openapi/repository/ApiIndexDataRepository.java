package com.codeit.findex.common.openapi.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.codeit.findex.common.openapi.entity.ApiIndexData;
import com.codeit.findex.common.openapi.entity.ApiIndexInfo;

public interface ApiIndexDataRepository extends JpaRepository<ApiIndexData, Long> {

	@Query("select d.baseDate from ApiIndexData d " +
		"where d.indexInfo = :indexInfo and d.baseDate in :dates")
	List<LocalDate> findExistingDates(@Param("indexInfo") ApiIndexInfo indexInfo,
		@Param("dates") List<LocalDate> dates);
}