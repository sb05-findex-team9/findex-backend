package com.codeit.findex.openApi.mapper;

import com.codeit.findex.indexInfo.domain.IndexInfo;
import com.codeit.findex.openApi.domain.AutoSyncConfig;
import com.codeit.findex.openApi.dto.AutoSyncConfigDto;

public class AutoSyncConfigMapper {

	public static AutoSyncConfigDto toDto(AutoSyncConfig entity) {
		if (entity == null) return null;

		IndexInfo idx = entity.getIndexInfo();

		return AutoSyncConfigDto.builder()
			.id(entity.getId())
			.indexInfoId(idx != null ? idx.getId() : null)
			.indexName(idx != null ? idx.getIndexName() : null)
			.indexClassification(idx != null ? idx.getIndexClassification() : null)
			.enabled(Boolean.TRUE.equals(entity.getEnabled()))
			.build();
	}
}
