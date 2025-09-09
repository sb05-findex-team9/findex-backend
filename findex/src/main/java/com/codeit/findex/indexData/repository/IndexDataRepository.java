package com.codeit.findex.indexData.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codeit.findex.indexData.domain.IndexData;

public interface IndexDataRepository extends JpaRepository<Long, IndexData> {
}
