package com.codeit.findex.openApi.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codeit.findex.openApi.domain.SyncJob;
import com.codeit.findex.openApi.dto.response.PagedSyncJobResponse;
import com.codeit.findex.openApi.dto.request.SyncJobListRequest;
import com.codeit.findex.openApi.dto.response.SyncJobResponse;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SyncJobQueryService {

	private final EntityManager entityManager;

	public PagedSyncJobResponse getSyncJobList(SyncJobListRequest request) {
		log.info("Query request: jobType={}, indexInfoId={}, status={}, size={}, cursor={}",
			request.getJobType(), request.getIndexInfoId(), request.getStatus(),
			request.getSize(), request.getCursor());

		// 커서 파싱
		Long lastId = parseLastId(request.getCursor(), request.getIdAfter());
		if (lastId != null) {
			log.info("Parsed cursor lastId: {}", lastId);
		}

		// 정렬 설정
		Sort.Direction direction = "desc".equalsIgnoreCase(request.getSortDirection())
			? Sort.Direction.DESC : Sort.Direction.ASC;

		// 전체 카운트 조회 (모든 요청에서 조회)
		Long totalElements = getTotalCount(request);
		log.info("Total count for current filters: {}", totalElements);

		// 쿼리 실행
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

		log.info("Query result: contentSize={}, requestedSize={}, hasNext={}, nextCursor={}",
			content.size(), request.getSize(), hasNext, nextCursor);

		return PagedSyncJobResponse.builder()
			.content(content)
			.nextCursor(nextCursor)
			.nextIdAfter(nextIdAfter)
			.size(request.getSize()) // 원래 요청한 사이즈 유지
			.totalElements(totalElements)
			.hasNext(hasNext)
			.build();
	}

	private Long getTotalCount(SyncJobListRequest request) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
		Root<SyncJob> root = countQuery.from(SyncJob.class);

		List<Predicate> predicates = buildPredicates(cb, root, request);
		countQuery.select(cb.count(root)).where(predicates.toArray(new Predicate[0]));

		return entityManager.createQuery(countQuery).getSingleResult();
	}

	private List<SyncJob> buildCriteriaQuery(SyncJobListRequest request, Long lastId, Sort.Direction direction) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<SyncJob> query = cb.createQuery(SyncJob.class);
		Root<SyncJob> root = query.from(SyncJob.class);

		// IndexInfo fetch join으로 N+1 문제 해결
		root.fetch("indexInfo", JoinType.LEFT);

		List<Predicate> predicates = buildPredicates(cb, root, request);

		// 커서 기반 페이지네이션 조건 (무한 스크롤의 핵심)
		if (lastId != null) {
			if (direction == Sort.Direction.DESC) {
				predicates.add(cb.lessThan(root.get("id"), lastId));
				log.debug("Added cursor condition: id < {}", lastId);
			} else {
				predicates.add(cb.greaterThan(root.get("id"), lastId));
				log.debug("Added cursor condition: id > {}", lastId);
			}
		}

		query.where(predicates.toArray(new Predicate[0]));

		// 정렬 설정 (일관된 정렬을 위해 id를 보조 정렬로 사용)
		String sortField = mapSortField(request.getSortField());
		List<Order> orders = new ArrayList<>();

		if (direction == Sort.Direction.DESC) {
			orders.add(cb.desc(root.get(sortField)));
			orders.add(cb.desc(root.get("id"))); // 안정적인 정렬을 위한 보조 정렬
		} else {
			orders.add(cb.asc(root.get(sortField)));
			orders.add(cb.asc(root.get("id")));
		}

		query.orderBy(orders);

		TypedQuery<SyncJob> typedQuery = entityManager.createQuery(query);
		typedQuery.setMaxResults(request.getSize() + 1); // hasNext 판단을 위해 +1

		List<SyncJob> result = typedQuery.getResultList();
		log.debug("Query executed, returned {} records", result.size());

		return result;
	}

	private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<SyncJob> root, SyncJobListRequest request) {
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

		return predicates;
	}

	private Long parseLastId(String cursor, String idAfter) {
		// cursor 우선 처리
		if (cursor != null && !cursor.trim().isEmpty()) {
			try {
				Long parsedId = Long.parseLong(cursor);
				log.debug("Successfully parsed cursor: {}", parsedId);
				return parsedId;
			} catch (NumberFormatException e) {
				log.warn("Failed to parse cursor '{}': {}", cursor, e.getMessage());
			}
		}

		// idAfter 처리
		if (idAfter != null && !idAfter.trim().isEmpty()) {
			try {
				Long parsedId = Long.parseLong(idAfter);
				log.debug("Successfully parsed idAfter: {}", parsedId);
				return parsedId;
			} catch (NumberFormatException e) {
				log.warn("Failed to parse idAfter '{}': {}", idAfter, e.getMessage());
			}
		}

		return null;
	}

	private String mapSortField(String apiSortField) {
		return switch (apiSortField) {
			case "targetDate" -> "targetDate";
			case "jobTime" -> "jobTime";
			case "id" -> "id";
			default -> "jobTime";
		};
	}
}