package com.codeit.findex.openApi.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

@Getter
@Setter
public class SyncJobListRequest {
	private String jobType;
	private Long indexInfoId;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate baseDateFrom;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate baseDateTo;

	private String worker;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	private LocalDateTime jobTimeFrom;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	private LocalDateTime jobTimeTo;

	private String status;
	private String idAfter;
	private String cursor;
	private String sortField = "jobTime";
	private String sortDirection = "desc";

	@Min(1)
	@Max(100)
	private Integer size = 10;

	public void setSize(Integer size) {
		this.size = (size == null || size <= 0) ? 10 : size;
	}
}