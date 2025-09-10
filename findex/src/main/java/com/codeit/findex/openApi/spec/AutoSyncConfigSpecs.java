package com.codeit.findex.openApi.spec;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.codeit.findex.openApi.domain.AutoSyncConfig;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;

public final class AutoSyncConfigSpecs {

	private AutoSyncConfigSpecs() {}

	public static Specification<AutoSyncConfig> indexInfoIdEq(Long indexInfoId) {
		return (root, q, cb) -> indexInfoId == null ? cb.conjunction()
			: cb.equal(root.join("indexInfo").get("id"), indexInfoId);
	}

	public static Specification<AutoSyncConfig> enabledEq(Boolean enabled) {
		return (root, q, cb) -> enabled == null ? cb.conjunction()
			: cb.equal(root.get("enabled"), enabled);
	}

	// idAfter (id 단일 키셋) - 정렬 방향 고려
	public static Specification<AutoSyncConfig> idAfter(Long idAfter, Sort.Direction dir) {
		if (idAfter == null) return (root, q, cb) -> cb.conjunction();
		return (root, q, cb) -> dir == Sort.Direction.DESC
			? cb.lessThan(root.get("id"), idAfter)
			: cb.greaterThan(root.get("id"), idAfter);
	}

	// (indexInfo.indexName, id) 키셋 커서
	public static Specification<AutoSyncConfig> afterIndexName(String lastName, Long lastId, Sort.Direction dir) {
		if (lastName == null || lastId == null) return (root, q, cb) -> cb.conjunction();

		return (root, q, cb) -> {
			// var 금지 → 타입 명시
			Join<Object, Object> idx = root.join("indexInfo", JoinType.INNER);
			if (dir == Sort.Direction.DESC) {
				return cb.or(
					cb.lessThan(idx.get("indexName"), lastName),
					cb.and(
						cb.equal(idx.get("indexName"), lastName),
						cb.lessThan(root.get("id"), lastId)
					)
				);
			} else {
				return cb.or(
					cb.greaterThan(idx.get("indexName"), lastName),
					cb.and(
						cb.equal(idx.get("indexName"), lastName),
						cb.greaterThan(root.get("id"), lastId)
					)
				);
			}
		};
	}

	// (enabled, id) 키셋 커서 (false < true 가정)
	public static Specification<AutoSyncConfig> afterEnabled(Boolean lastEnabled, Long lastId, Sort.Direction dir) {
		if (lastEnabled == null || lastId == null) return (root, q, cb) -> cb.conjunction();

		if (dir == Sort.Direction.DESC) {
			// DESC: true 먼저 → (enabled < lastEnabled) OR (enabled == lastEnabled AND id < lastId)
			return (root, q, cb) -> cb.or(
				lastEnabled ? cb.isFalse(root.get("enabled")) : cb.disjunction(),
				cb.and(cb.equal(root.get("enabled"), lastEnabled), cb.lessThan(root.get("id"), lastId))
			);
		} else {
			// ASC: false 먼저 → (enabled > lastEnabled) OR (enabled == lastEnabled AND id > lastId)
			return (root, q, cb) -> cb.or(
				!lastEnabled ? cb.isTrue(root.get("enabled")) : cb.disjunction(),
				cb.and(cb.equal(root.get("enabled"), lastEnabled), cb.greaterThan(root.get("id"), lastId))
			);
		}
	}
}
