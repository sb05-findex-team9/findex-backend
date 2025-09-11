package com.codeit.findex.indexData.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.codeit.findex.indexData.domain.IndexData;
import com.codeit.findex.indexData.dto.IndexPerformanceDto;
import com.codeit.findex.indexInfo.domain.IndexInfo;

public interface IndexDataRepository extends JpaRepository<IndexData, Long> {

	@Query("select d.baseDate from IndexData d " +
		"where d.indexInfo = :indexInfo and d.baseDate in :dates")
	List<LocalDate> findExistingDates(@Param("indexInfo") IndexInfo indexInfo,
		@Param("dates") List<LocalDate> dates);

	@Query("SELECT id FROM IndexData id " +
		"LEFT JOIN FETCH id.indexInfo ii " +
		"LEFT JOIN FETCH ii.autoSyncConfig " +
		"WHERE (:indexInfoId IS NULL OR id.indexInfo.id = :indexInfoId) " +
		"AND (:startDate IS NULL OR id.baseDate >= :startDate) " +
		"AND (:endDate IS NULL OR id.baseDate <= :endDate)")
	Page<IndexData> findIndexDataWithFilters(
		@Param("indexInfoId") Long indexInfoId,
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate,
		Pageable pageable);

	@Query("SELECT id FROM IndexData id " +
		"LEFT JOIN FETCH id.indexInfo ii " +
		"LEFT JOIN FETCH ii.autoSyncConfig " +
		"WHERE (:indexInfoId IS NULL OR id.indexInfo.id = :indexInfoId) " +
		"AND (:startDate IS NULL OR id.baseDate >= :startDate) " +
		"AND (:endDate IS NULL OR id.baseDate <= :endDate) " +
		"AND (CAST(:lastBaseDate AS date) IS NULL OR id.baseDate > CAST(:lastBaseDate AS date) OR (id.baseDate = CAST(:lastBaseDate AS date) AND id.id > :lastId))")
	Page<IndexData> findIndexDataWithFiltersAfterIdAsc(
		@Param("indexInfoId") Long indexInfoId,
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate,
		@Param("lastBaseDate") LocalDate lastBaseDate,
		@Param("lastId") Long lastId,
		Pageable pageable);

	@Query("SELECT id FROM IndexData id " +
		"LEFT JOIN FETCH id.indexInfo ii " +
		"LEFT JOIN FETCH ii.autoSyncConfig " +
		"WHERE (:indexInfoId IS NULL OR id.indexInfo.id = :indexInfoId) " +
		"AND (:startDate IS NULL OR id.baseDate >= :startDate) " +
		"AND (:endDate IS NULL OR id.baseDate <= :endDate) " +
		"AND (CAST(:lastBaseDate AS date) IS NULL OR id.baseDate < CAST(:lastBaseDate AS date) OR (id.baseDate = CAST(:lastBaseDate AS date) AND id.id < :lastId))")
	Page<IndexData> findIndexDataWithFiltersAfterIdDesc(
		@Param("indexInfoId") Long indexInfoId,
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate,
		@Param("lastBaseDate") LocalDate lastBaseDate,
		@Param("lastId") Long lastId,
		Pageable pageable);

	@Query("SELECT id FROM IndexData id " +
		"LEFT JOIN FETCH id.indexInfo ii " +
		"LEFT JOIN FETCH ii.autoSyncConfig " +
		"WHERE id.indexInfo.id = :indexInfoId " +
		"AND id.baseDate >= :startDate " +
		"ORDER BY id.baseDate ASC")
	List<IndexData> findByIndexInfoIdAndBaseDateGreaterThanEqualOrderByBaseDateAsc(
		@Param("indexInfoId") Long indexInfoId,
		@Param("startDate") LocalDate startDate);

	@Query("SELECT id FROM IndexData id " +
		"LEFT JOIN FETCH id.indexInfo ii " +
		"LEFT JOIN FETCH ii.autoSyncConfig " +
		"WHERE id.indexInfo.id = :indexInfoId " +
		"ORDER BY id.baseDate ASC")
	List<IndexData> findByIndexInfoIdOrderByBaseDateAsc(@Param("indexInfoId") Long indexInfoId);

	@Query("SELECT id FROM IndexData id " +
		"LEFT JOIN FETCH id.indexInfo ii " +
		"LEFT JOIN FETCH ii.autoSyncConfig " +
		"WHERE id.indexInfo.id = :indexInfoId " +
		"AND id.baseDate = :baseDate")
	Optional<IndexData> findByIndexInfoIdAndBaseDate(
		@Param("indexInfoId") Long indexInfoId,
		@Param("baseDate") LocalDate baseDate);

	@Query("SELECT MAX(id.baseDate) FROM IndexData id")
	Optional<LocalDate> findMaxBaseDate();

	@Query("SELECT COUNT(id) FROM IndexData id " +
		"WHERE id.baseDate = :targetDate " +
		"AND id.closingPrice IS NOT NULL")
	long countByBaseDateAndClosingPriceIsNotNull(@Param("targetDate") LocalDate targetDate);

	@Query("SELECT id FROM IndexData id " +
		"LEFT JOIN FETCH id.indexInfo ii " +
		"LEFT JOIN FETCH ii.autoSyncConfig " +
		"WHERE id.baseDate = :targetDate " +
		"AND id.closingPrice IS NOT NULL")
	List<IndexData> findAllByBaseDateWithIndexInfo(@Param("targetDate") LocalDate targetDate);

	@Query("SELECT id FROM IndexData id " +
		"LEFT JOIN FETCH id.indexInfo ii " +
		"LEFT JOIN FETCH ii.autoSyncConfig " +
		"WHERE id.baseDate IN :dates " +
		"AND id.closingPrice IS NOT NULL " +
		"ORDER BY id.indexInfo.id, id.baseDate")
	List<IndexData> findAllByBaseDateInWithIndexInfo(@Param("dates") List<LocalDate> dates);

	@Query("SELECT AVG(d.closingPrice) FROM IndexData d " +
		"WHERE d.indexInfo.id = :indexInfoId " +
		"AND d.baseDate BETWEEN :startDate AND :endDate")
	Optional<Double> findAverageClosingPriceByIndexInfoBetween(
		@Param("indexInfoId") Long indexInfoId,
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate
	);

	@Query("SELECT id FROM IndexData id " +
		"LEFT JOIN FETCH id.indexInfo ii " +
		"LEFT JOIN FETCH ii.autoSyncConfig " +
		"WHERE id.indexInfo.id = :indexInfoId " +
		"AND id.baseDate = :baseDate")
	Optional<IndexData> findByIndexInfoIdAndBaseDateWithIndexInfo(
		@Param("indexInfoId") Long indexInfoId,
		@Param("baseDate") LocalDate baseDate);

	@Query("SELECT new com.codeit.findex.indexData.dto.IndexPerformanceDto(" +
		"id.id, id.baseDate, id.closingPrice, " +
		"ii.id, ii.indexName, ii.indexClassification) " +
		"FROM IndexData id " +
		"JOIN id.indexInfo ii " +
		"WHERE id.baseDate = :targetDate " +
		"AND id.closingPrice IS NOT NULL")
	List<IndexPerformanceDto> findAllByBaseDateWithIndexInfoDto(@Param("targetDate") LocalDate targetDate);

	@Query("SELECT d.indexInfo.id as indexInfoId, AVG(d.closingPrice) as avgPrice " +
		"FROM IndexData d " +
		"WHERE d.indexInfo.id IN :indexInfoIds " +
		"AND d.baseDate BETWEEN :startDate AND :endDate " +
		"AND d.closingPrice IS NOT NULL " +
		"GROUP BY d.indexInfo.id")
	List<IndexAveragePrice> findAverageClosingPriceProjectionsByIndexInfosBetween(
		@Param("indexInfoIds") List<Long> indexInfoIds,
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate
	);

	interface IndexAveragePrice {
		Long getIndexInfoId();

		Double getAvgPrice();
	}

	default Map<Long, Double> findAverageClosingPricesByIndexInfosBetween(
		List<Long> indexInfoIds,
		LocalDate startDate,
		LocalDate endDate) {

		List<IndexAveragePrice> results = findAverageClosingPriceProjectionsByIndexInfosBetween(indexInfoIds, startDate,
			endDate);
		return results.stream()
			.collect(Collectors.toMap(
				IndexAveragePrice::getIndexInfoId,
				IndexAveragePrice::getAvgPrice
			));
	}

	@Query("SELECT COUNT(i) FROM IndexData i WHERE " +
		"(:indexInfoId IS NULL OR i.indexInfo.id = :indexInfoId) AND " +
		"(:startDate IS NULL OR i.baseDate >= :startDate) AND " +
		"(:endDate IS NULL OR i.baseDate <= :endDate)")
	long countIndexDataWithFilters(@Param("indexInfoId") Long indexInfoId,
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate);

}