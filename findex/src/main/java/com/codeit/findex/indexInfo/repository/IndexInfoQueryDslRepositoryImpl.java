package com.codeit.findex.indexInfo.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.codeit.findex.indexInfo.domain.IndexInfo;
import com.codeit.findex.indexInfo.repository.dto.FindAllDto;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import static com.codeit.findex.indexInfo.domain.QIndexInfo.indexInfo;

@Repository
@RequiredArgsConstructor
public class IndexInfoQueryDslRepositoryImpl implements IndexInfoQueryDslRepository {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<IndexInfo> findAllByConditionWithPage(FindAllDto dto) {
		return queryFactory
			.selectFrom(indexInfo)
			.where(
				contains(indexInfo.indexClassification, dto.getIndexClassification()),
				contains(indexInfo.indexName, dto.getIndexName()),
				cursorCondition(dto)
			)
			.orderBy(getOrderSpecifiers(dto))
			.limit(dto.getSize())
			.fetch();
	}

	private BooleanExpression contains(com.querydsl.core.types.dsl.StringPath field, String value) {
		return value == null ? null : field.contains(value);
	}

	private BooleanExpression cursorCondition(FindAllDto dto) {
		if (dto.getCursor() == null) return null;

		boolean isAsc = "asc".equalsIgnoreCase(dto.getSortDirection());
		String sortField = dto.getSortField();

		switch (sortField) {
			case "indexClassification":
				return isAsc
					? indexInfo.indexClassification.gt(dto.getCursor()).or(indexInfo.indexClassification.eq(dto.getCursor()).and(indexInfo.id.gt(dto.getIdAfter())))
					: indexInfo.indexClassification.lt(dto.getCursor()).or(indexInfo.indexClassification.eq(dto.getCursor()).and(indexInfo.id.lt(dto.getIdAfter())));
			case "indexName":
				return isAsc
					? indexInfo.indexName.gt(dto.getCursor()).or(indexInfo.indexName.eq(dto.getCursor()).and(indexInfo.id.gt(dto.getIdAfter())))
					: indexInfo.indexName.lt(dto.getCursor()).or(indexInfo.indexName.eq(dto.getCursor()).and(indexInfo.id.lt(dto.getIdAfter())));
			case "employedItemsCount":
				Integer count = Integer.parseInt(dto.getCursor());
				return isAsc
					? indexInfo.employedItemsCount.gt(count).or(indexInfo.employedItemsCount.eq(count).and(indexInfo.id.gt(dto.getIdAfter())))
					: indexInfo.employedItemsCount.lt(count).or(indexInfo.employedItemsCount.eq(count).and(indexInfo.id.lt(dto.getIdAfter())));
			default:
				return null;
		}
	}

	private OrderSpecifier<?>[] getOrderSpecifiers(FindAllDto dto) {
		boolean isAsc = "asc".equalsIgnoreCase(dto.getSortDirection());

		OrderSpecifier<?> primary;
		switch (dto.getSortField()) {
			case "indexClassification":
				primary = isAsc ? indexInfo.indexClassification.asc() : indexInfo.indexClassification.desc();
				break;
			case "indexName":
				primary = isAsc ? indexInfo.indexName.asc() : indexInfo.indexName.desc();
				break;
			case "employedItemsCount":
				primary = isAsc ? indexInfo.employedItemsCount.asc() : indexInfo.employedItemsCount.desc();
				break;
			default:
				primary = indexInfo.id.asc();
		}

		return new OrderSpecifier<?>[] {
			primary,
			isAsc ? indexInfo.id.asc() : indexInfo.id.desc()
		};
	}
}