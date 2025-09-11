package com.codeit.findex.indexData.dto;

import java.math.BigDecimal;

public record IndexDataUpdateRequest(
	BigDecimal marketPrice,
	BigDecimal closingPrice,
	BigDecimal highPrice,
	BigDecimal lowPrice,
	BigDecimal versus,
	BigDecimal fluctuationRate,
	Long tradingQuantity,
	BigDecimal tradingPrice,
	BigDecimal marketTotalAmount
) {
}
