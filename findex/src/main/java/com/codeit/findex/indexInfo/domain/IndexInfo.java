package com.codeit.findex.indexInfo.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import com.codeit.findex.indexData.domain.IndexData;
import com.codeit.findex.openApi.domain.AutoSyncConfig;
import com.codeit.findex.openApi.domain.SyncJob;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
@Entity
@Table(name = "index_infos")
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class IndexInfo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "index_name", length = 240, nullable = false)
	private String indexName;

	@Column(name = "index_classification", length = 20)
	private String indexClassification;

	@Column(name = "employed_items_count")
	private Integer employedItemsCount;

	@Column(name = "base_point_in_time")
	private LocalDate basePointInTime;

	@Column(name = "base_index", precision = 18, scale = 4)
	private BigDecimal baseIndex;

	@Column(name = "source_type", length = 16)
	private String sourceType;

	@Column(name = "favorite")
	private Boolean favorite;

	//
	@OneToMany(mappedBy = "indexInfo", orphanRemoval = true, cascade = {CascadeType.ALL})
	private Set<IndexData> indexData;

	@OneToOne(mappedBy = "indexInfo")
	private AutoSyncConfig autoSyncConfig;

	@OneToMany(mappedBy = "indexInfo",orphanRemoval = true, cascade = {CascadeType.ALL})
	private Set<SyncJob> syncJobs;
}
