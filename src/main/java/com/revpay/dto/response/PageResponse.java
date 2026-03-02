package com.revpay.dto.response;

import java.util.List;

public class PageResponse<T> {

	private List<T> content;
	private int currentPage;
	private int totalPages;
	private long totalElements;

	public PageResponse(List<T> content, int currentPage, int totalPages, long totalElements) {
		this.content = content;
		this.currentPage = currentPage;
		this.totalPages = totalPages;
		this.totalElements = totalElements;
	}

	public List<T> getContent() {
		return content;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public int getTotalPages() {
		return totalPages;
	}

	public long getTotalElements() {
		return totalElements;
	}
}