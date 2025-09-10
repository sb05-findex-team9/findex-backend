package com.codeit.findex.indexData.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.codeit.findex.indexData.domain.IndexData;
import com.codeit.findex.indexInfo.domain.IndexInfo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndexDataRequestDto {
	private Long indexInfoId;
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate baseDate;
	private String sourceType;
	private Double marketPrice;
	private Double closingPrice;
	private Double highPrice;
	private Double lowPrice;
	private Double versus;
	private Double fluctuationRate;
	private Long tradingQuantity;
	private Double tradingPrice;
	private Double marketTotalAmount;

	public IndexData toEntity(IndexInfo indexInfo) {
		return IndexData.builder()
			.indexInfo(indexInfo)
			.baseDate(this.baseDate)
			.sourceType(this.sourceType)
			.marketPrice(this.marketPrice != null ? BigDecimal.valueOf(this.marketPrice) : null)
			.closingPrice(this.closingPrice != null ? BigDecimal.valueOf(this.closingPrice) : null)
			.highPrice(this.highPrice != null ? BigDecimal.valueOf(this.highPrice) : null)
			.lowPrice(this.lowPrice != null ? BigDecimal.valueOf(this.lowPrice) : null)
			.versus(this.versus != null ? BigDecimal.valueOf(this.versus) : null)
			.fluctuationRate(this.fluctuationRate != null ? BigDecimal.valueOf(this.fluctuationRate) : null)
			.tradingQuantity(this.tradingQuantity)
			.tradingPrice(this.tradingPrice != null ? BigDecimal.valueOf(this.tradingPrice) : null)
			.marketTotalAmount(this.marketTotalAmount != null ? BigDecimal.valueOf(this.marketTotalAmount) : null)
			.build();
	}
}