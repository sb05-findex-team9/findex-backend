package com.codeit.findex.indexData.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.codeit.findex.indexData.domain.IndexData;

public interface IndexDataRepository extends JpaRepository<IndexData, Long> {

	@Query("SELECT id FROM IndexData id " +
		"LEFT JOIN FETCH id.indexInfo ii " +
		"WHERE (:indexInfoId IS NULL OR id.indexInfo.id = :indexInfoId) " +
		"AND (:startDate IS NULL OR id.baseDate >= :startDate) " +
		"AND (:endDate IS NULL OR id.baseDate <= :endDate) " +
		"AND (:idAfter IS NULL OR id.id > :idAfter)")
	Page<IndexData> findIndexDataWithFilters(
		@Param("indexInfoId") Integer indexInfoId,
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate,
		@Param("idAfter") Integer idAfter,
		Pageable pageable);

	Optional<IndexData> findTopByIndexInfoIdAndBaseDateLessThanOrderByBaseDateDesc(
		Integer indexInfoId, LocalDate baseDate);
}
