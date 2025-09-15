package com.codeit.findex.indexInfo.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.codeit.findex.indexInfo.domain.IndexInfo;
import com.codeit.findex.indexInfo.domain.QIndexInfo;
import com.codeit.findex.indexInfo.repository.dto.FindAllDto;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class IndexInfoQueryDslRepositoryImpl implements IndexInfoQueryDslRepository {

	private final JPAQueryFactory queryFactory;
	private final QIndexInfo qIndexInfo = QIndexInfo.indexInfo;

	@Override
	public List<IndexInfo> findAllByConditionWithSlice(FindAllDto dto) {
		return queryFactory
			.selectFrom(qIndexInfo)
			.where(
				contains(qIndexInfo.indexClassification, dto.getIndexClassification()),
				contains(qIndexInfo.indexName, dto.getIndexName()),
				cursorCondition(dto)
			)
			.orderBy(getOrderSpecifiers(dto))
			.limit(dto.getSize())
			.fetch();
	}

	private BooleanExpression contains(StringPath field, String value) {
		return value == null ? null : field.contains(value);
	}

	private BooleanExpression cursorCondition(FindAllDto dto) {
		if (dto.getCursor() == null) return null;

		boolean isAsc = "asc".equalsIgnoreCase(dto.getSortDirection());
		String sortField = dto.getSortField();

		switch (sortField) {
			case "indexClassification":
				return isAsc
					? qIndexInfo.indexClassification.gt(dto.getCursor()).or(
					qIndexInfo.indexClassification.eq(dto.getCursor()).and(qIndexInfo.id.gt(dto.getIdAfter())))
					: qIndexInfo.indexClassification.lt(dto.getCursor()).or(
					qIndexInfo.indexClassification.eq(dto.getCursor()).and(qIndexInfo.id.lt(dto.getIdAfter())));
			case "indexName":
				return isAsc
					? qIndexInfo.indexName.gt(dto.getCursor()).or(
					qIndexInfo.indexName.eq(dto.getCursor()).and(qIndexInfo.id.gt(dto.getIdAfter())))
					: qIndexInfo.indexName.lt(dto.getCursor()).or(
					qIndexInfo.indexName.eq(dto.getCursor()).and(qIndexInfo.id.lt(dto.getIdAfter())));
			case "employedItemsCount":
				Integer count = Integer.parseInt(dto.getCursor());
				return isAsc
					? qIndexInfo.employedItemsCount.gt(count).or(qIndexInfo.employedItemsCount.eq(count).and(qIndexInfo.id.gt(dto.getIdAfter())))
					: qIndexInfo.employedItemsCount.lt(count).or(qIndexInfo.employedItemsCount.eq(count).and(qIndexInfo.id.lt(dto.getIdAfter())));
			default:
				return null;
		}
	}

	private OrderSpecifier<?>[] getOrderSpecifiers(FindAllDto dto) {
		boolean isAsc = "asc".equalsIgnoreCase(dto.getSortDirection());

		OrderSpecifier<?> primary;
		switch (dto.getSortField()) {
			case "indexClassification":
				primary = isAsc ? qIndexInfo.indexClassification.asc() : qIndexInfo.indexClassification.desc();
				break;
			case "indexName":
				primary = isAsc ? qIndexInfo.indexName.asc() : qIndexInfo.indexName.desc();
				break;
			case "employedItemsCount":
				primary = isAsc ? qIndexInfo.employedItemsCount.asc() : qIndexInfo.employedItemsCount.desc();
				break;
			default:
				primary = qIndexInfo.id.asc();
		}

		return new OrderSpecifier<?>[] {
			primary,
			isAsc ? qIndexInfo.id.asc() : qIndexInfo.id.desc()
		};
	}
}