package com.codeit.findex.indexData.domain;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;

import com.codeit.findex.indexInfo.domain.IndexInfo;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
@Entity
@Table(name = "index_data", uniqueConstraints = {
	@UniqueConstraint(columnNames = {"index_info_id", "base_date"})
})
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class IndexData {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "base_date", nullable = false)
	private LocalDate baseDate; // 기준일자 (basDt)

	@Column(name="source_type", precision = 18, scale = 4)
	private String sourceType; // 소스 타입

	@Column(name = "market_price", precision = 18, scale = 4)
	private BigDecimal marketPrice; // 시가 (mkp)

	@Column(name = "closing_price", precision = 18, scale = 4)
	private BigDecimal closingPrice; // 종가 (clpr)

	@Column(name = "high_price", precision = 18, scale = 4)
	private BigDecimal highPrice; // 고가 (hipr)

	@Column(name = "low_price", precision = 18, scale = 4)
	private BigDecimal lowPrice; // 저가 (lopr)

	@Column(name="versus", precision = 18, scale = 4)
	private BigDecimal versus; // 전일 대비 등락

	@Column(name = "fluctuation_rate", precision = 18, scale = 4)
	private BigDecimal fluctuationRate; // 등락률 (fltRt)

	@Column(name ="trading_quantity") // 거래량 (trqu)
	private Long tradingQuantity;

	@Column(name = "trading_price", precision = 21, scale = 0)
	private BigDecimal tradingPrice; // 거래대금 (trPrc)

	@Column(name = "market_total_amount", precision = 21, scale = 0)
	private BigDecimal marketTotalAmount; // 상장시가총액 (lstgMrktTotAmt)

	//
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "index_info_id", nullable = false, unique = true)
	private IndexInfo indexInfo;

}
