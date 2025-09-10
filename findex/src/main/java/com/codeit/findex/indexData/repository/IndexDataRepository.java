package com.codeit.findex.indexData.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.codeit.findex.indexData.domain.IndexData;

public interface IndexDataRepository extends JpaRepository<IndexData, Long> {

	// 기본 조회 (첫 페이지)
	@Query("SELECT id FROM IndexData id " +
		"LEFT JOIN FETCH id.indexInfo ii " +
		"WHERE (:indexInfoId IS NULL OR id.indexInfo.id = :indexInfoId) " +
		"AND (:startDate IS NULL OR id.baseDate >= :startDate) " +
		"AND (:endDate IS NULL OR id.baseDate <= :endDate)")
	Page<IndexData> findIndexDataWithFilters(
		@Param("indexInfoId") Long indexInfoId,
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate,
		Pageable pageable);

	// 오름차순 정렬용 커서 조회 (ID보다 큰 값)
	@Query("SELECT id FROM IndexData id " +
		"LEFT JOIN FETCH id.indexInfo ii " +
		"WHERE (:indexInfoId IS NULL OR id.indexInfo.id = :indexInfoId) " +
		"AND (:startDate IS NULL OR id.baseDate >= :startDate) " +
		"AND (:endDate IS NULL OR id.baseDate <= :endDate) " +
		"AND id.id > :lastId")
	Page<IndexData> findIndexDataWithFiltersAfterIdAsc(
		@Param("indexInfoId") Long indexInfoId,
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate,
		@Param("lastId") Long lastId,
		Pageable pageable);

	// 내림차순 정렬용 커서 조회 (ID보다 작은 값)
	@Query("SELECT id FROM IndexData id " +
		"LEFT JOIN FETCH id.indexInfo ii " +
		"WHERE (:indexInfoId IS NULL OR id.indexInfo.id = :indexInfoId) " +
		"AND (:startDate IS NULL OR id.baseDate >= :startDate) " +
		"AND (:endDate IS NULL OR id.baseDate <= :endDate) " +
		"AND id.id < :lastId")
	Page<IndexData> findIndexDataWithFiltersAfterIdDesc(
		@Param("indexInfoId") Long indexInfoId,
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate,
		@Param("lastId") Long lastId,
		Pageable pageable);

	List<IndexData> findByIndexInfoIdAndBaseDateGreaterThanEqualOrderByBaseDateAsc(Long indexInfoId,
		LocalDate startDate);

	List<IndexData> findByIndexInfoIdOrderByBaseDateAsc(Long indexInfoId);

	Optional<IndexData> findByIndexInfoIdAndBaseDate(Long indexInfoId, LocalDate baseDate);

	@Query("SELECT MAX(id.baseDate) FROM IndexData id")
	Optional<LocalDate> findMaxBaseDate();

	@Query("SELECT MAX(id.baseDate) FROM IndexData id WHERE id.indexInfo.id = :indexInfoId")
	Optional<LocalDate> findMaxBaseDateByIndexInfoId(@Param("indexInfoId") Long indexInfoId);

}