package com.revpay.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long txnId;

	private String txnType;
	private Double amount;

	private Double balanceAfterTxn;

	private LocalDateTime createdAt;

	@ManyToOne
	@JoinColumn(name = "wallet_id")
	private Wallet wallet;

	private String remark;

	public Long getTxnId() {
		return txnId;
	}

	public void setTxnId(Long txnId) {
		this.txnId = txnId;
	}

	public String getTxnType() {
		return txnType;
	}

	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public Double getBalanceAfterTxn() {
		return balanceAfterTxn;
	}

	public void setBalanceAfterTxn(Double balanceAfterTxn) {
		this.balanceAfterTxn = balanceAfterTxn;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public Wallet getWallet() {
		return wallet;
	}

	public void setWallet(Wallet wallet) {
		this.wallet = wallet;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

}
