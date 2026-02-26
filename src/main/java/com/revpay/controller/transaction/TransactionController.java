package com.revpay.controller.transaction;

import com.revpay.dto.response.ApiResponse;
import com.revpay.entity.Transaction;
import com.revpay.entity.Wallet;
import com.revpay.service.interfaces.TransactionService;
import com.revpay.service.interfaces.WalletService;
import com.revpay.repository.TransactionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@PreAuthorize("isAuthenticated()")
public class TransactionController {

	@Autowired
	private WalletService walletService;

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private TransactionRepository transactionRepository;

	@GetMapping
	public ApiResponse<?> transactions(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "5") int size, @RequestParam(required = false) String type,
			@RequestParam(required = false) String from, @RequestParam(required = false) String to,
			@RequestParam(required = false) String sort) {

		return new ApiResponse<>(true, "Transactions fetched",
				transactionService.searchTransactions(page, size, type, from, to, sort));
	}
}