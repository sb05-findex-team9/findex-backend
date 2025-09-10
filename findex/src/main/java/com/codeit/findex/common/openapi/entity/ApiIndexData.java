package com.codeit.findex.common.openapi.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "index_data", uniqueConstraints = {
	@UniqueConstraint(columnNames = {"index_info_id", "base_date"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApiIndexData {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "index_info_id", nullable = false)
	private ApiIndexInfo indexInfo;

	@Column(name = "base_date", nullable = false)
	private LocalDate baseDate; // 기준일자 (basDt)

	@Column(name = "closing_price", precision = 18, scale = 4)
	private BigDecimal closingPrice; // 종가 (clpr)

	@Column(name = "versus", precision = 18, scale = 4)
	private BigDecimal priceChange; // 대비 (vs)

	@Column(name = "fluctuation_rate", precision = 18, scale = 4)
	private BigDecimal fluctuationRate; // 등락률 (fltRt)

	@Column(name = "market_price", precision = 18, scale = 4)
	private BigDecimal marketPrice; // 시가 (mkp)

	@Column(name = "high_price", precision = 18, scale = 4)
	private BigDecimal highPrice; // 고가 (hipr)

	@Column(name = "low_price", precision = 18, scale = 4)
	private BigDecimal lowPrice; // 저가 (lopr)

	@Column(name = "trading_quantity")
	private Long tradingVolume; // 거래량 (trqu)

	@Column(name = "trading_price", precision = 21, scale = 0)
	private BigDecimal transactionPrice; // 거래대금 (trPrc)

	@Column(name = "market_total_amount", precision = 21, scale = 0)
	private BigDecimal marketCap; // 상장시가총액 (lstgMrktTotAmt)

	@Builder
	public ApiIndexData(ApiIndexInfo indexInfo, LocalDate baseDate, BigDecimal closingPrice, BigDecimal priceChange,
		BigDecimal fluctuationRate, BigDecimal marketPrice, BigDecimal highPrice, BigDecimal lowPrice,
		Long tradingVolume, BigDecimal transactionPrice, BigDecimal marketCap) {
		this.indexInfo = indexInfo;
		this.baseDate = baseDate;
		this.closingPrice = closingPrice;
		this.priceChange = priceChange;
		this.fluctuationRate = fluctuationRate;
		this.marketPrice = marketPrice;
		this.highPrice = highPrice;
		this.lowPrice = lowPrice;
		this.tradingVolume = tradingVolume;
		this.transactionPrice = transactionPrice;
		this.marketCap = marketCap;
	}
}