package com.revpay.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallets")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long walletId;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    private Double balance;

    private String status; // ACTIVE / BLOCKED

    private LocalDateTime updatedAt;

	public Long getWalletId() {
		return walletId;
	}

	public void setWalletId(Long walletId) {
		this.walletId = walletId;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Double getBalance() {
		return balance;
	}

	public void setBalance(Double balance) {
		this.balance = balance;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

    // getters & setters
    
    
}
