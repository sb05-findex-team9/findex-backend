package com.codeit.findex.common.openapi.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResponseDto {
	private Response response;

	@Getter
	@Setter
	@ToString
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Response {
		private Body body;
	}

	@Getter
	@Setter
	@ToString
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Body {
		private int totalCount;
		private int numOfRows;
		private int pageNo;
		private Items items;
	}

	@Getter
	@Setter
	@ToString
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Items {
		private List<Item> item;
	}

	@Getter
	@Setter
	@ToString
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Item {
		private String basDt; // 기준일자
		private String idxNm; // 지수명
		private String idxCsf; // 지수분류
		private String epyItmsCnt; // 채용종목수
		private String clpr; // 종가
		private String vs; // 대비
		private String fltRt; // 등락률
		private String mkp; // 시가
		private String hipr; // 고가
		private String lopr; // 저가
		private String trqu; // 거래량
		private String trPrc; // 거래대금
		private String lstgMrktTotAmt; // 상장시가총액
		private String lsYrEdVsFltRg; // 전년말대비_등락폭
		private String lsYrEdVsFltRt; // 전년말대비_등락률
		private String yrWRcrdHgst; // 연중기록최고
		private String yrWRcrdHgstDt; // 연중기록최고일자
		private String yrWRcrdLwst; // 연중기록최저
		private String yrWRcrdLwstDt; // 연중기록최저일자
		private String basPntm; // 기준시점
		private String basIdx; // 기준지수
	}
}