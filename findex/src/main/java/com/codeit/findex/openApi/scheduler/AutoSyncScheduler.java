package com.codeit.findex.openApi.scheduler;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.codeit.findex.openApi.service.AutoSyncService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "auto-sync.scheduler", name = "enabled", havingValue = "true")
public class AutoSyncScheduler {

	private final AutoSyncService autoSyncService;

	// 기본: 매일 06:05 (KST). 로컬에선 아래 3)에서 cron 바꿔서 빠르게 테스트 가능.
	@Scheduled(
		// cron = "${auto-sync.scheduler.cron:20 0 0 * * *}",
		cron = "${auto-sync.scheduler.cron:0 0 6 * * *}",
		zone = "${auto-sync.scheduler.zone:Asia/Seoul}"
	)
	public void run() {
		try {
			int processed = autoSyncService.syncEnabledConfigs(null);
			log.info("[AutoSyncScheduler] processed={}", processed);
		} catch (IllegalStateException e) {
			// 이미 실행 중(락 실패) → 조용히 스킵
			log.info("[AutoSyncScheduler] already running, skip");
		} catch (Exception e) {
			// 예기치 못한 실패 로깅
			log.error("[AutoSyncScheduler] failed", e);
		}
	}
}
