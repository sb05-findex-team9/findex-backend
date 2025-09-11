package com.codeit.findex.openApi.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class SyncJobListRequest {
	private String jobType;  // INDEX_INFO, INDEX_DATA
	private Long indexInfoId;
	private LocalDate baseDateFrom;
	private LocalDate baseDateTo;
	private String worker;
	private LocalDateTime jobTimeFrom;
	private LocalDateTime jobTimeTo;
	private String status;  // SUCCESS, FAILED
	private String idAfter;
	private String cursor;
	private String sortField = "jobTime";  // targetDate, jobTime
	private String sortDirection = "desc";
	private Integer size = 10;
}