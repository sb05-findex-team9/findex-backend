package com.codeit.findex.openApi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codeit.findex.openApi.domain.AutoSyncConfig;

public interface AutoSyncConfigRepository extends JpaRepository<AutoSyncConfig,Long> {
}
