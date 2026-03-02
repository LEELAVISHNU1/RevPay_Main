package com.revpay.dto.response;

public class BusinessSummaryResponse {

	private double walletBalance;
	private double totalReceived;
	private double totalSent;
	private double totalInvoiceRevenue;

	public double getWalletBalance() {
		return walletBalance;
	}

	public void setWalletBalance(double walletBalance) {
		this.walletBalance = walletBalance;
	}

	public double getTotalReceived() {
		return totalReceived;
	}

	public void setTotalReceived(double totalReceived) {
		this.totalReceived = totalReceived;
	}

	public double getTotalSent() {
		return totalSent;
	}

	public void setTotalSent(double totalSent) {
		this.totalSent = totalSent;
	}

	public double getTotalInvoiceRevenue() {
		return totalInvoiceRevenue;
	}

	public void setTotalInvoiceRevenue(double totalInvoiceRevenue) {
		this.totalInvoiceRevenue = totalInvoiceRevenue;
	}
}