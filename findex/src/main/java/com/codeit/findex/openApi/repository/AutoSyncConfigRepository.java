package com.codeit.findex.openApi.repository;

import com.codeit.findex.openApi.domain.AutoSyncConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;

public interface AutoSyncConfigRepository extends
	JpaRepository<AutoSyncConfig, Long>,
	JpaSpecificationExecutor<AutoSyncConfig> {

	boolean existsByIndexInfoId(Long indexInfoId);

	@Override
	@EntityGraph(attributePaths = {"indexInfo"})
	Page<AutoSyncConfig> findAll(Specification<AutoSyncConfig> spec, Pageable pageable);
}
