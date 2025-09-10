package com.codeit.findex.indexData.dto;

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
	private Integer id;
	private Integer indexInfoId;
	private String baseDate;
	private String sourceType;
	private Double marketPrice;
	private Double closingPrice;
	private Double highPrice;
	private Double lowPrice;
	private Double versus;
	private Double fluctuationRate;
	private Integer tradingQuantity;
	private Integer tradingPrice;
	private Integer marketTotalAmount;

	public static IndexDataResponseDto from(IndexData indexData) {
		return IndexDataResponseDto.builder()
			.id(indexData.getId() != null ? indexData.getId().intValue() : null)
			.indexInfoId(indexData.getIndexInfo() != null ? indexData.getIndexInfo().getId().intValue() : null)
			.baseDate(indexData.getBaseDate() != null ? indexData.getBaseDate().toString() : null)
			.sourceType(indexData.getSourceType())
			.marketPrice(indexData.getMarketPrice() != null ? indexData.getMarketPrice().doubleValue() : null)
			.closingPrice(indexData.getClosingPrice() != null ? indexData.getClosingPrice().doubleValue() : null)
			.highPrice(indexData.getHighPrice() != null ? indexData.getHighPrice().doubleValue() : null)
			.lowPrice(indexData.getLowPrice() != null ? indexData.getLowPrice().doubleValue() : null)
			.versus(indexData.getVersus() != null ? indexData.getVersus().doubleValue() : null)
			.fluctuationRate(
				indexData.getFluctuationRate() != null ? indexData.getFluctuationRate().doubleValue() : null)
			.tradingQuantity(indexData.getTradingQuantity() != null ? indexData.getTradingQuantity().intValue() : null)
			.tradingPrice(indexData.getTradingPrice() != null ? indexData.getTradingPrice().intValue() : null)
			.marketTotalAmount(
				indexData.getMarketTotalAmount() != null ? indexData.getMarketTotalAmount().intValue() : null)
			.build();
	}
}