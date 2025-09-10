package com.codeit.findex.common.openapi.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.codeit.findex.indexData.domain.IndexData;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "index_infos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApiIndexInfo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "index_name", nullable = false, unique = true, length = 240)
	private String indexName; // 지수명 (idxNm)

	@Column(name = "index_classification", length = 20)
	private String indexClassification; // 지수분류명 (idxCsf)

	@Column(name = "employed_items_count")
	private Integer employedItemsCount; // 채용종목 수 (epyItmsCnt)

	@Column(name = "base_point_in_time")
	private LocalDate basePointInTime; // 기준시점 (basPntm)

	@Column(name = "base_index", precision = 18, scale = 4)
	private BigDecimal baseIndex; // 기준지수 (basIdx)

	@OneToMany(mappedBy = "indexInfo", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<IndexData> indexDataList = new ArrayList<>();

	@Builder
	public ApiIndexInfo(String indexName, String indexClassification, Integer employedItemsCount,
		LocalDate basePointInTime, BigDecimal baseIndex) {
		this.indexName = indexName;
		this.indexClassification = indexClassification;
		this.employedItemsCount = employedItemsCount;
		this.basePointInTime = basePointInTime;
		this.baseIndex = baseIndex;
	}
}