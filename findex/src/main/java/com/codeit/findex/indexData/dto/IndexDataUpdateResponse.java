package com.codeit.findex.indexData.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.codeit.findex.indexData.domain.IndexData;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public record IndexDataUpdateResponse(
	Long id,
	Long indexInfoId,
	LocalDate baseDate,
	String sourceType,
	BigDecimal marketPrice,
	BigDecimal closingPrice,
	BigDecimal highPrice,
	BigDecimal lowPrice,
	BigDecimal versus,
	BigDecimal fluctuationRate,
	Long tradingQuantity,

	@JsonSerialize(using = ToStringSerializer.class)
	BigDecimal tradingPrice,

	@JsonSerialize(using = ToStringSerializer.class)
	BigDecimal marketTotalAmount
) {

	public static IndexDataUpdateResponse from(IndexData indexData) {
		Long infoId = (indexData.getIndexInfo() != null) ? indexData.getIndexInfo().getId() : null;

		return new IndexDataUpdateResponse(
			indexData.getId(),
			infoId,
			indexData.getBaseDate(),
			indexData.getSourceType(),
			indexData.getMarketPrice(),
			indexData.getClosingPrice(),
			indexData.getHighPrice(),
			indexData.getLowPrice(),
			indexData.getVersus(),
			indexData.getFluctuationRate(),
			indexData.getTradingQuantity(),
			indexData.getTradingPrice(),
			indexData.getMarketTotalAmount()
		);
	}
}