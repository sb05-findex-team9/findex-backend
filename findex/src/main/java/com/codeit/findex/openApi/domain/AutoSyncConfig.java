package com.codeit.findex.openApi.domain;

import com.codeit.findex.indexInfo.domain.IndexInfo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
@Entity
@Table(name = "auto_sync_configs")
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class AutoSyncConfig {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "enabled", nullable = false)
	private Boolean enabled;

	//
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "index_info_id", nullable = false, unique = true)
	private IndexInfo indexInfo;

}
