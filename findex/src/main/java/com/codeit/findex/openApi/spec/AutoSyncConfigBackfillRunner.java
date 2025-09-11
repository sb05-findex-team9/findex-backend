package com.codeit.findex.openApi.spec;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.codeit.findex.indexInfo.domain.IndexInfo;
import com.codeit.findex.indexInfo.repository.IndexInfoRepository;
import com.codeit.findex.openApi.domain.AutoSyncConfig;
import com.codeit.findex.openApi.repository.AutoSyncConfigRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AutoSyncConfigBackfillRunner {

	private final IndexInfoRepository indexInfoRepository;
	private final AutoSyncConfigRepository autoSyncConfigRepository;

	@EventListener(ApplicationReadyEvent.class)
	@Transactional
	public void backfill() {
		var all = indexInfoRepository.findAll();
		int created = 0;
		for (IndexInfo info : all) {
			if (!autoSyncConfigRepository.existsByIndexInfoId(info.getId())) {
				AutoSyncConfig cfg = new AutoSyncConfig();
				cfg.setIndexInfo(info);
				cfg.setEnabled(false);
				autoSyncConfigRepository.save(cfg);
				created++;
			}
		}
		log.info("[AutoSync] backfill created {} configs", created);
	}
}
