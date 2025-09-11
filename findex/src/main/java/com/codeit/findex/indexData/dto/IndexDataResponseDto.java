package com.codeit.findex.indexData.dto;

import java.time.LocalDate;

import com.codeit.findex.indexData.domain.IndexData;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndexDataResponseDto {
	private Long id;
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

	public static IndexDataResponseDto from(IndexData indexData) {
		return IndexDataResponseDto.builder()
			.id(indexData.getId())
			.baseDate(indexData.getBaseDate())
			.sourceType(indexData.getSourceType())
			.marketPrice(indexData.getMarketPrice() != null ? indexData.getMarketPrice().doubleValue() : null)
			.closingPrice(indexData.getClosingPrice() != null ? indexData.getClosingPrice().doubleValue() : null)
			.highPrice(indexData.getHighPrice() != null ? indexData.getHighPrice().doubleValue() : null)
			.lowPrice(indexData.getLowPrice() != null ? indexData.getLowPrice().doubleValue() : null)
			.versus(indexData.getVersus() != null ? indexData.getVersus().doubleValue() : null)
			.fluctuationRate(
				indexData.getFluctuationRate() != null ? indexData.getFluctuationRate().doubleValue() : null)
			.tradingQuantity(indexData.getTradingQuantity())
			.tradingPrice(indexData.getTradingPrice() != null ? indexData.getTradingPrice().doubleValue() : null)
			.marketTotalAmount(
				indexData.getMarketTotalAmount() != null ? indexData.getMarketTotalAmount().doubleValue() : null)
			.build();
	}
}