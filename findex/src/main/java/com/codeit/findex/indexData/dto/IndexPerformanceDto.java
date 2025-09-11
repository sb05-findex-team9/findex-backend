// IndexPerformanceDto.java
package com.codeit.findex.indexData.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class IndexPerformanceDto {
	private Long indexDataId;
	private LocalDate baseDate;
	private BigDecimal closingPrice;
	private Long indexInfoId;
	private String indexName;
	private String indexClassification;

	public float getClosingPriceAsFloat() {
		return closingPrice != null ? closingPrice.floatValue() : 0f;
	}
}