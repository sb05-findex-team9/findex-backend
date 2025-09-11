// com.codeit.findex.common.openapi.scheduler.ApiFullFetchScheduler
package com.codeit.findex.common.scheduler;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.codeit.findex.common.openapi.service.ApiIndexDataService;
import com.codeit.findex.common.openapi.service.ApiIndexInfoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiFullFetchScheduler {

	private final ApiIndexInfoService indexInfoService;
	private final ApiIndexDataService indexDataService;
	private final JdbcTemplate jdbcTemplate; // 동일 커넥션에서 lock/unlock 수행

	@Value("${findex.scheduler.fullfetch.enabled:true}")
	private boolean enabled;

	@Value("${findex.scheduler.fullfetch.run-on-startup:false}")
	private boolean runOnStartup;

	private static final String LOCK_KEY_EXPR = "hashtext('findex_fullfetch_daily')";

	//서버 시작 직후 1회 실행
	@EventListener(ApplicationReadyEvent.class)
	public void runOnceOnStartup() {
		if (runOnStartup) {
			log.info("[FullFetch] run once on startup");
			runOnceWithLock();
		}
	}

	@Scheduled(
		cron = "${findex.scheduler.fullfetch.cron}",
		zone = "${findex.scheduler.fullfetch.zone:Asia/Seoul}"
	)
	public void runFullFetch() {
		runOnceWithLock();
	}

	private void runOnceWithLock() {
		if (!enabled) {
			log.info("[FullFetch] disabled; skip.");
			return;
		}

		jdbcTemplate.execute((ConnectionCallback<Void>)con -> {
			try (var stmt = con.createStatement()) {
				// 1) try lock
				boolean locked = false;
				try (ResultSet rs = stmt.executeQuery(
					"SELECT pg_try_advisory_lock(" + LOCK_KEY_EXPR + ")")) {
					if (rs.next()) {
						locked = rs.getBoolean(1);
					}
				}
				if (!locked) {
					log.warn("[FullFetch] another instance is running; skip.");
					return null;
				}

				// 2) 실제 작업
				try {
					log.info("[FullFetch] start: IndexInfo -> IndexData (team parser services)");
					indexInfoService.fetchAndSaveIndexInfo();  // 팀원 파싱/저장 서비스
					indexDataService.fetchAndSaveIndexData();  // 팀원 파싱/저장 서비스
					log.info("[FullFetch] done");
				} catch (Exception e) {
					log.error("[FullFetch] failed", e);
					throw e;
				} finally {
					// 3) unlock (결과 boolean 반환)
					try (ResultSet rs2 = stmt.executeQuery(
						"SELECT pg_advisory_unlock(" + LOCK_KEY_EXPR + ")")) {
						if (rs2.next()) {
							boolean unlocked = rs2.getBoolean(1);
							log.debug("[FullFetch] unlock={}", unlocked);
						}
					} catch (SQLException unlockEx) {
						log.warn("[FullFetch] unlock failed", unlockEx);
					}
				}
			}
			return null;
		});
	}
}
