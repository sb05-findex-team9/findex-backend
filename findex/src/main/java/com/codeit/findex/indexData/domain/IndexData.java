package com.codeit.findex.indexData.domain;

import java.math.BigDecimal;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
@Entity
@Table(name = "index_data")
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class IndexData {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "base_date", nullable = false)
	private LocalDate baseDate;

	@Column(name = "source_type", length = 16)
	private String sourceType;

	@Column(name = "market_price", precision = 18, scale = 4)
	private BigDecimal marketPrice;

	@Column(name = "closing_price", precision = 18, scale = 4)
	private BigDecimal closingPrice;

	@Column(name = "high_price", precision = 18, scale = 4)
	private BigDecimal highPrice;

	@Column(name = "low_price", precision = 18, scale = 4)
	private BigDecimal lowPrice;

	@Column(name = "versus", precision = 18, scale = 4)
	private BigDecimal versus;

	@Column(name = "fluctuation_rate", precision = 18, scale = 4)
	private BigDecimal fluctuationRate;

	@Column(name = "trading_quantity")
	private Long tradingQuantity;

	@Column(name = "trading_price", precision = 21, scale = 0)
	private BigDecimal tradingPrice;

	@Column(name = "market_total_amount", precision = 21, scale = 0)
	private BigDecimal marketTotalAmount;

	//
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "index_info_id", nullable = false, unique = true)
	private IndexInfo indexInfo;

}
