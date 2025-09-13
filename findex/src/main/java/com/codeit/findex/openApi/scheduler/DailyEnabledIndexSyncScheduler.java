package com.codeit.findex.openApi.scheduler;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.codeit.findex.common.openapi.service.ApiIndexDataService;
import com.codeit.findex.openApi.domain.AutoSyncConfig;
import com.codeit.findex.openApi.repository.AutoSyncConfigRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "auto-sync.scheduler", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DailyEnabledIndexSyncScheduler {

	private final AutoSyncConfigRepository autoSyncConfigRepository;
	private final ApiIndexDataService apiIndexDataService;

	@Scheduled(cron = "${auto-sync.scheduler.cron}", zone = "${auto-sync.scheduler.zone}")
	public void runDaily() {
		LocalDate targetDate = LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(1);

		// var → 명시적 타입
		List<AutoSyncConfig> enabled = autoSyncConfigRepository.findAllByEnabledTrueWithIndexInfo();
		if (enabled.isEmpty()) {
			log.info("[AutoSync] enabled=true 대상 없음. skip. targetDate={}", targetDate);
			return;
		}

		Set<String> allowedKeys = enabled.stream()
			.map(cfg -> cfg.getIndexInfo().getIndexName() + "_" + String.valueOf(cfg.getIndexInfo().getIndexClassification()))
			.collect(Collectors.toSet());

		try {
			int saved = apiIndexDataService.fetchAndSaveIndexDataFiltered(allowedKeys, targetDate);
			log.info("[AutoSync] 완료 targetDate={} 대상={}건, 저장/업데이트={}건",
				targetDate, allowedKeys.size(), saved);
		} catch (Exception e) {
			log.error("[AutoSync] 실패 targetDate={} keys={}", targetDate, allowedKeys, e);
		}
	}
}
