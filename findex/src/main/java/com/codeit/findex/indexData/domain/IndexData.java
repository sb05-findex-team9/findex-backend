package com.codeit.findex.indexData.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.codeit.findex.indexData.dto.IndexDataUpdateRequest;
import com.codeit.findex.indexInfo.domain.IndexInfo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
@Table(name = "index_data",
	uniqueConstraints = {
		@UniqueConstraint(columnNames = {"index_info_id", "base_date"})
	},
	indexes = {
		@Index(name = "idx_index_data_base_date", columnList = "base_date"),
		@Index(name = "idx_index_data_index_info_base_date", columnList = "index_info_id, base_date")
	}
)
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

	@Builder.Default
	@Column(name = "source_type", nullable = false) // NOT NULL 제약조건 추가
	private String sourceType = "OPEN_API"; // 소스 타입

	@Column(name = "market_price", precision = 18, scale = 4)
	private BigDecimal marketPrice; // 시가 (mkp)

	@Column(name = "closing_price", precision = 18, scale = 4)
	private BigDecimal closingPrice; // 종가 (clpr)

	@Column(name = "high_price", precision = 18, scale = 4)
	private BigDecimal highPrice; // 고가 (hipr)

	@Column(name = "low_price", precision = 18, scale = 4)
	private BigDecimal lowPrice; // 저가 (lopr)

	@Column(name = "versus", precision = 18, scale = 4)
	private BigDecimal versus; // 전일 대비 등락

	@Column(name = "fluctuation_rate", precision = 18, scale = 4)
	private BigDecimal fluctuationRate; // 등락률 (fltRt)

	@Column(name = "trading_quantity") // 거래량 (trqu)
	private Long tradingQuantity;

	@Column(name = "trading_price", precision = 21, scale = 0)
	private BigDecimal tradingPrice; // 거래대금 (trPrc)

	@Column(name = "market_total_amount", precision = 21, scale = 0)
	private BigDecimal marketTotalAmount; // 상장시가총액 (lstgMrktTotAmt)

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "index_info_id", nullable = false)
	private IndexInfo indexInfo;

	public void update(IndexDataUpdateRequest request) {
		boolean hasUpdates = false;

		if (request.marketPrice() != null) {
			this.marketPrice = request.marketPrice();
			hasUpdates = true;
		}
		if (request.closingPrice() != null) {
			this.closingPrice = request.closingPrice();
			hasUpdates = true;
		}
		if (request.highPrice() != null) {
			this.highPrice = request.highPrice();
			hasUpdates = true;
		}
		if (request.lowPrice() != null) {
			this.lowPrice = request.lowPrice();
			hasUpdates = true;
		}
		if (request.versus() != null) {
			this.versus = request.versus();
			hasUpdates = true;
		}
		if (request.fluctuationRate() != null) {
			this.fluctuationRate = request.fluctuationRate();
			hasUpdates = true;
		}
		if (request.tradingQuantity() != null) {
			this.tradingQuantity = request.tradingQuantity();
			hasUpdates = true;
		}
		if (request.tradingPrice() != null) {
			this.tradingPrice = request.tradingPrice();
			hasUpdates = true;
		}
		if (request.marketTotalAmount() != null) {
			this.marketTotalAmount = request.marketTotalAmount();
			hasUpdates = true;
		}

		if (hasUpdates) {
			this.sourceType = "USER";
		}
	}


	public boolean hasValidClosingPrice() {
		return closingPrice != null && closingPrice.compareTo(BigDecimal.ZERO) > 0;
	}

	public boolean hasValidPriceData() {
		return hasValidClosingPrice() &&
			marketPrice != null && marketPrice.compareTo(BigDecimal.ZERO) > 0;
	}
}
