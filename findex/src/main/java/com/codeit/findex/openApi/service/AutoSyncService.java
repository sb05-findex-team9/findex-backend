package com.codeit.findex.openApi.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codeit.findex.openApi.domain.AutoSyncConfig;
import com.codeit.findex.openApi.repository.AutoSyncConfigRepository;
import com.codeit.findex.openApi.repository.DbAdvisoryLockRepository;
import com.codeit.findex.openApi.spec.AutoSyncConfigSpecs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutoSyncService {
	private static final long LOCK_KEY = 814_001L;

	private final AutoSyncConfigRepository configRepo;
	private final DbAdvisoryLockRepository lockRepo;

	@Transactional
	public int syncEnabledConfigs(Long onlyIndexInfoId) {

		if (!lockRepo.tryAdvisoryXactLock(LOCK_KEY)) {
			throw new IllegalStateException("Sync is already running");
		}

		Specification<AutoSyncConfig> spec = (root, q, cb) -> cb.conjunction();
		spec = spec.and(AutoSyncConfigSpecs.enabledEq(true));
		if (onlyIndexInfoId != null) {
			spec = spec.and(AutoSyncConfigSpecs.indexInfoIdEq(onlyIndexInfoId));
		}

		int processed = 0;
		int page = 0;
		final int size = 500;
		while (true) {
			Page<AutoSyncConfig> batch = configRepo.findAll(spec, PageRequest.of(page, size));
			if (batch.isEmpty()) break;

			for (AutoSyncConfig cfg : batch.getContent()) {
				Long indexInfoId = cfg.getIndexInfo().getId();
				// TODO: 외부 API 호출 → UPSERT 저장
				processed++;
				log.info("Synced indexInfoId={}", indexInfoId);
			}

			if (!batch.hasNext()) break;
			page++;
		}
		return processed;
	}
}
