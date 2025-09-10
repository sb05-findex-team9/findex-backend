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
import com.codeit.findex.indexInfo.domain.IndexInfo;

public interface IndexDataRepository extends JpaRepository<IndexData, Long> {

	@Query("select d.baseDate from IndexData d " +
		"where d.indexInfo = :indexInfo and d.baseDate in :dates")
	List<LocalDate> findExistingDates(@Param("indexInfo") IndexInfo indexInfo,
		@Param("dates") List<LocalDate> dates);

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

	@Query("SELECT id FROM IndexData id " +
		"LEFT JOIN FETCH id.indexInfo ii " +
		"WHERE id.indexInfo.id = :indexInfoId " +
		"AND id.baseDate >= :startDate " +
		"ORDER BY id.baseDate ASC")
	List<IndexData> findByIndexInfoIdAndBaseDateGreaterThanEqualOrderByBaseDateAsc(
		@Param("indexInfoId") Long indexInfoId,
		@Param("startDate") LocalDate startDate);

	@Query("SELECT id FROM IndexData id " +
		"LEFT JOIN FETCH id.indexInfo ii " +
		"WHERE id.indexInfo.id = :indexInfoId " +
		"ORDER BY id.baseDate ASC")
	List<IndexData> findByIndexInfoIdOrderByBaseDateAsc(@Param("indexInfoId") Long indexInfoId);

	@Query("SELECT id FROM IndexData id " +
		"LEFT JOIN FETCH id.indexInfo ii " +
		"WHERE id.indexInfo.id = :indexInfoId " +
		"AND id.baseDate = :baseDate")
	Optional<IndexData> findByIndexInfoIdAndBaseDate(
		@Param("indexInfoId") Long indexInfoId,
		@Param("baseDate") LocalDate baseDate);

	@Query("SELECT MAX(id.baseDate) FROM IndexData id")
	Optional<LocalDate> findMaxBaseDate();

	@Query("SELECT MAX(id.baseDate) FROM IndexData id WHERE id.indexInfo.id = :indexInfoId")
	Optional<LocalDate> findMaxBaseDateByIndexInfoId(@Param("indexInfoId") Long indexInfoId);

	// 성과 랭킹을 위한 최적화된 쿼리 추가
	@Query("SELECT id FROM IndexData id " +
		"LEFT JOIN FETCH id.indexInfo ii " +
		"WHERE id.baseDate = :targetDate " +
		"AND id.closingPrice IS NOT NULL")
	List<IndexData> findAllByBaseDateWithIndexInfo(@Param("targetDate") LocalDate targetDate);

	// 특정 날짜 범위의 모든 지수 데이터 조회 (배치 방식)
	@Query("SELECT id FROM IndexData id " +
		"LEFT JOIN FETCH id.indexInfo ii " +
		"WHERE id.baseDate IN :dates " +
		"AND id.closingPrice IS NOT NULL " +
		"ORDER BY id.indexInfo.id, id.baseDate")
	List<IndexData> findAllByBaseDateInWithIndexInfo(@Param("dates") List<LocalDate> dates);
}