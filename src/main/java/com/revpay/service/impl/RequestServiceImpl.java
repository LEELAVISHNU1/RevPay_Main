package com.revpay.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.revpay.entity.MoneyRequest;
import com.revpay.entity.User;
import com.revpay.repository.MoneyRequestRepository;
import com.revpay.repository.UserRepository;
import com.revpay.service.interfaces.NotificationService;
import com.revpay.service.interfaces.RequestService;
import com.revpay.service.interfaces.UserService;
import com.revpay.service.interfaces.WalletService;

import jakarta.transaction.Transactional;

@Service
public class RequestServiceImpl implements RequestService {

	@Autowired
	private UserService userService;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private WalletService walletService;
	@Autowired
	private MoneyRequestRepository requestRepository;
	@Autowired
	private NotificationService notificationService;

	// CREATE REQUEST
	@Override
	public void createRequest(String receiverEmail, Double amount, String note) {

		User sender = userService.getCurrentUser(); // logged in user
		User receiver = userRepository.findByEmail(receiverEmail)
				.orElseThrow(() -> new RuntimeException("User not found"));

		if (sender.getUserId().equals(receiver.getUserId()))
			throw new RuntimeException("You cannot request money from yourself");

		if (amount == null || amount <= 0)
			throw new RuntimeException("Invalid amount");

		MoneyRequest req = new MoneyRequest();
		req.setSender(sender); // ✔ correct
		req.setReceiver(receiver); // ✔ correct
		req.setAmount(amount);
		req.setNote(note);
		req.setStatus("PENDING");
		req.setCreatedAt(LocalDateTime.now());

		requestRepository.save(req);

		notificationService.notify(receiver, "Money Request Received", "You have a new money request of ₹" + amount);
	}

	// ACCEPT REQUEST
	@Override
	@Transactional
	public void acceptRequest(Long requestId) {

	    MoneyRequest req = requestRepository.findById(requestId)
	            .orElseThrow(() -> new RuntimeException("Request not found"));

	    if (!req.getStatus().equals("PENDING"))
	        throw new RuntimeException("Request already processed");

	    User currentUser = userService.getCurrentUser();

	    // Only receiver can accept
	    if (!req.getReceiver().getUserId().equals(currentUser.getUserId()))
	        throw new RuntimeException("Unauthorized action");

	    // 🔥 Proper internal transfer
	    walletService.sendMoneyInternal(
	            currentUser,        // payer
	            req.getSender(),    // requester (gets money)
	            req.getAmount(),
	            "Money request accepted"
	    );

	    req.setStatus("ACCEPTED");
	    requestRepository.save(req);

	    notificationService.notify(
	            req.getSender(),
	            "Request Accepted",
	            "₹" + req.getAmount() + " has been received"
	    );
	}

	// REJECT REQUEST
	@Override
	public void rejectRequest(Long requestId) {

		MoneyRequest req = requestRepository.findById(requestId)
				.orElseThrow(() -> new RuntimeException("Request not found"));

		if (!req.getStatus().equals("PENDING"))
			throw new RuntimeException("Request already processed");

		User currentUser = userService.getCurrentUser();

		if (!req.getReceiver().getUserId().equals(currentUser.getUserId()))
			throw new RuntimeException("Unauthorized action");

		req.setStatus("REJECTED");
		requestRepository.save(req);

		notificationService.notify(req.getSender(), "Request Rejected", "Your money request was rejected");
	}

	// INCOMING REQUESTS (where current user must pay)
	// INCOMING (user must pay)
	@Override
	public List<MoneyRequest> getIncomingRequests() {
	    User currentUser = userService.getCurrentUser();
	    return requestRepository.findByReceiverAndStatus(currentUser, "PENDING");
	}

	// SENT (user requested money)
	@Override
	public List<MoneyRequest> mySentRequests() {
	    User currentUser = userService.getCurrentUser();
	    return requestRepository.findBySender(currentUser);
	}
}