package com.codeit.findex.openApi.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.codeit.findex.openApi.domain.SyncJob;

public interface SyncJobRepository extends JpaRepository<SyncJob, Long> {

	@Query("SELECT sj FROM SyncJob sj WHERE " +
		"(:jobType IS NULL OR sj.jobType = :jobType) AND " +
		"(:indexInfoId IS NULL OR sj.indexInfo.id = :indexInfoId) AND " +
		"(:baseDateFrom IS NULL OR sj.targetDate >= :baseDateFrom) AND " +
		"(:baseDateTo IS NULL OR sj.targetDate <= :baseDateTo) AND " +
		"(:worker IS NULL OR sj.worker = :worker) AND " +
		"(:jobTimeFrom IS NULL OR sj.jobTime >= :jobTimeFrom) AND " +
		"(:jobTimeTo IS NULL OR sj.jobTime <= :jobTimeTo) AND " +
		"(:status IS NULL OR sj.result = :status)")
	Page<SyncJob> findWithFilters(
		@Param("jobType") String jobType,
		@Param("indexInfoId") Long indexInfoId,
		@Param("baseDateFrom") LocalDate baseDateFrom,
		@Param("baseDateTo") LocalDate baseDateTo,
		@Param("worker") String worker,
		@Param("jobTimeFrom") LocalDateTime jobTimeFrom,
		@Param("jobTimeTo") LocalDateTime jobTimeTo,
		@Param("status") String status,
		Pageable pageable);

	@Query("SELECT sj FROM SyncJob sj WHERE " +
		"(:jobType IS NULL OR sj.jobType = :jobType) AND " +
		"(:indexInfoId IS NULL OR sj.indexInfo.id = :indexInfoId) AND " +
		"(:baseDateFrom IS NULL OR sj.targetDate >= :baseDateFrom) AND " +
		"(:baseDateTo IS NULL OR sj.targetDate <= :baseDateTo) AND " +
		"(:worker IS NULL OR sj.worker = :worker) AND " +
		"(:jobTimeFrom IS NULL OR sj.jobTime >= :jobTimeFrom) AND " +
		"(:jobTimeTo IS NULL OR sj.jobTime <= :jobTimeTo) AND " +
		"(:status IS NULL OR sj.result = :status) AND " +
		"sj.id > :lastId")
	Page<SyncJob> findWithFiltersAfterIdAsc(
		@Param("jobType") String jobType,
		@Param("indexInfoId") Long indexInfoId,
		@Param("baseDateFrom") LocalDate baseDateFrom,
		@Param("baseDateTo") LocalDate baseDateTo,
		@Param("worker") String worker,
		@Param("jobTimeFrom") LocalDateTime jobTimeFrom,
		@Param("jobTimeTo") LocalDateTime jobTimeTo,
		@Param("status") String status,
		@Param("lastId") Long lastId,
		Pageable pageable);

	@Query("SELECT sj FROM SyncJob sj WHERE " +
		"(:jobType IS NULL OR sj.jobType = :jobType) AND " +
		"(:indexInfoId IS NULL OR sj.indexInfo.id = :indexInfoId) AND " +
		"(:baseDateFrom IS NULL OR sj.targetDate >= :baseDateFrom) AND " +
		"(:baseDateTo IS NULL OR sj.targetDate <= :baseDateTo) AND " +
		"(:worker IS NULL OR sj.worker = :worker) AND " +
		"(:jobTimeFrom IS NULL OR sj.jobTime >= :jobTimeFrom) AND " +
		"(:jobTimeTo IS NULL OR sj.jobTime <= :jobTimeTo) AND " +
		"(:status IS NULL OR sj.result = :status) AND " +
		"sj.id < :lastId")
	Page<SyncJob> findWithFiltersAfterIdDesc(
		@Param("jobType") String jobType,
		@Param("indexInfoId") Long indexInfoId,
		@Param("baseDateFrom") LocalDate baseDateFrom,
		@Param("baseDateTo") LocalDate baseDateTo,
		@Param("worker") String worker,
		@Param("jobTimeFrom") LocalDateTime jobTimeFrom,
		@Param("jobTimeTo") LocalDateTime jobTimeTo,
		@Param("status") String status,
		@Param("lastId") Long lastId,
		Pageable pageable);

	List<SyncJob> findTop10ByOrderByJobTimeDesc();
}