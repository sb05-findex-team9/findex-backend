package com.codeit.findex.openApi.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.codeit.findex.indexInfo.domain.IndexInfo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
@Entity
@Table(name = "sync_jobs")
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class SyncJob {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "job_type", length = 16)
	private String jobType;

	@Column(name = "target_date")
	private LocalDate targetDate;

	@Column(name = "worker", length = 100)
	private String worker;

	@Column(name = "job_time")
	private LocalDateTime jobTime;

	@Column(name = "result", length = 16)
	private String result;

	//
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "index_info_id", nullable = false)
	private IndexInfo indexInfo;

	public Long getIndexInfoId() {
		return indexInfo != null ? indexInfo.getId() : null;
	}

	public String getIndexName() {
		return indexInfo != null ? indexInfo.getIndexName() : null;
	}
}
