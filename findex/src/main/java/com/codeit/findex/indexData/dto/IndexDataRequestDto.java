package com.codeit.findex.indexData.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;

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
		// sourceType이 null이거나 빈 문자열인 경우 기본값 설정
		String finalSourceType = StringUtils.hasText(this.sourceType) ? this.sourceType : "OPEN_API";

		return IndexData.builder()
			.indexInfo(indexInfo)
			.baseDate(this.baseDate)
			.sourceType(finalSourceType) // null 체크 후 기본값 적용
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