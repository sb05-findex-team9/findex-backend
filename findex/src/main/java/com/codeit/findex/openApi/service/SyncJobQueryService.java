package com.codeit.findex.openApi.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codeit.findex.openApi.domain.SyncJob;
import com.codeit.findex.openApi.dto.PagedSyncJobResponse;
import com.codeit.findex.openApi.dto.SyncJobListRequest;
import com.codeit.findex.openApi.dto.SyncJobResponse;
import com.codeit.findex.openApi.repository.SyncJobRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SyncJobQueryService {

	private final SyncJobRepository syncJobRepository;
	private final EntityManager entityManager;

	public PagedSyncJobResponse getSyncJobList(SyncJobListRequest request) {
		// 커서 처리
		Long lastId = parseLastId(request.getCursor(), request.getIdAfter());

		// 정렬 설정
		Sort.Direction direction = "desc".equalsIgnoreCase(request.getSortDirection())
			? Sort.Direction.DESC : Sort.Direction.ASC;

		// Criteria API를 사용한 안전한 쿼리 빌딩
		List<SyncJob> syncJobs = buildCriteriaQuery(request, lastId, direction);

		// 다음 페이지 존재 여부 확인을 위해 size + 1로 조회
		boolean hasNext = syncJobs.size() > request.getSize();
		if (hasNext) {
			syncJobs = syncJobs.subList(0, request.getSize());
		}

		// 응답 생성
		List<SyncJobResponse> content = syncJobs.stream()
			.map(SyncJobResponse::from)
			.collect(Collectors.toList());

		String nextCursor = null;
		String nextIdAfter = null;
		if (hasNext && !content.isEmpty()) {
			Long lastIdInPage = content.get(content.size() - 1).getId();
			nextCursor = lastIdInPage.toString();
			nextIdAfter = lastIdInPage.toString();
		}

		return PagedSyncJobResponse.builder()
			.content(content)
			.nextCursor(nextCursor)
			.nextIdAfter(nextIdAfter)
			.size(content.size())
			.totalElements(content.size()) // 정확한 총 개수는 별도 쿼리 필요
			.hasNext(hasNext)
			.build();
	}

	private List<SyncJob> buildCriteriaQuery(SyncJobListRequest request, Long lastId, Sort.Direction direction) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<SyncJob> query = cb.createQuery(SyncJob.class);
		Root<SyncJob> root = query.from(SyncJob.class);

		// IndexInfo fetch join으로 N+1 문제 해결
		root.fetch("indexInfo", JoinType.LEFT);

		List<Predicate> predicates = new ArrayList<>();

		// 필터 조건 추가
		if (request.getJobType() != null && !request.getJobType().trim().isEmpty()) {
			predicates.add(cb.equal(root.get("jobType"), request.getJobType()));
		}

		if (request.getIndexInfoId() != null) {
			predicates.add(cb.equal(root.get("indexInfo").get("id"), request.getIndexInfoId()));
		}

		if (request.getBaseDateFrom() != null) {
			predicates.add(cb.greaterThanOrEqualTo(root.get("targetDate"), request.getBaseDateFrom()));
		}

		if (request.getBaseDateTo() != null) {
			predicates.add(cb.lessThanOrEqualTo(root.get("targetDate"), request.getBaseDateTo()));
		}

		if (request.getWorker() != null && !request.getWorker().trim().isEmpty()) {
			predicates.add(cb.equal(root.get("worker"), request.getWorker()));
		}

		if (request.getJobTimeFrom() != null) {
			predicates.add(cb.greaterThanOrEqualTo(root.get("jobTime"), request.getJobTimeFrom()));
		}

		if (request.getJobTimeTo() != null) {
			predicates.add(cb.lessThanOrEqualTo(root.get("jobTime"), request.getJobTimeTo()));
		}

		if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
			predicates.add(cb.equal(root.get("result"), request.getStatus()));
		}

		// 커서 기반 페이지네이션 조건
		if (lastId != null) {
			if (direction == Sort.Direction.DESC) {
				predicates.add(cb.lessThan(root.get("id"), lastId));
			} else {
				predicates.add(cb.greaterThan(root.get("id"), lastId));
			}
		}

		query.where(predicates.toArray(new Predicate[0]));

		// 정렬 설정
		String sortField = mapSortField(request.getSortField());
		List<Order> orders = new ArrayList<>();

		if (direction == Sort.Direction.DESC) {
			orders.add(cb.desc(root.get(sortField)));
			orders.add(cb.asc(root.get("id"))); // 안정적인 정렬을 위한 보조 정렬
		} else {
			orders.add(cb.asc(root.get(sortField)));
			orders.add(cb.asc(root.get("id")));
		}

		query.orderBy(orders);

		TypedQuery<SyncJob> typedQuery = entityManager.createQuery(query);
		typedQuery.setMaxResults(request.getSize() + 1); // hasNext 판단을 위해 +1

		return typedQuery.getResultList();
	}

	private Long parseLastId(String cursor, String idAfter) {
		if (cursor != null && !cursor.trim().isEmpty()) {
			try {
				return Long.parseLong(cursor);
			} catch (NumberFormatException ignored) {
			}
		}
		if (idAfter != null && !idAfter.trim().isEmpty()) {
			try {
				return Long.parseLong(idAfter);
			} catch (NumberFormatException ignored) {
			}
		}
		return null;
	}

	private String mapSortField(String apiSortField) {
		return switch (apiSortField) {
			case "targetDate" -> "targetDate";
			case "jobTime" -> "jobTime";
			default -> "jobTime";
		};
	}
}