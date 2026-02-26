package com.revpay.service.interfaces;

import com.revpay.dto.response.PageResponse;
import com.revpay.entity.User;

public interface TransactionService {

	PageResponse<?> myTransactions(int page, int size);

	PageResponse<?> searchTransactions(int page, int size, String type, String from, String to, String sort);
	
	void createTransaction(User user, Double amount, String description);
}
