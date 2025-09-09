package com.codeit.findex.openApi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codeit.findex.openApi.domain.SyncJob;

public interface SyncJobRepository extends JpaRepository<SyncJob,Long> {
}
