package com.revpay.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    private static final Logger logger =
            LogManager.getLogger(RequestServiceImpl.class);

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

    // Creates a new money request and notifies the receiver
    @Override
    public void createRequest(String receiverEmail, Double amount, String note) {

        User sender = userService.getCurrentUser();

        logger.info("User {} creating money request to {}",
                sender.getEmail(), receiverEmail);

        User receiver = userRepository.findByEmail(receiverEmail)
                .orElseThrow(() -> {
                    logger.error("Receiver not found: {}", receiverEmail);
                    return new RuntimeException("User not found");
                });

        if (sender.getUserId().equals(receiver.getUserId())) {
            logger.warn("User attempted self money request");
            throw new RuntimeException("You cannot request money from yourself");
        }

        if (amount == null || amount <= 0) {
            logger.warn("Invalid money request amount");
            throw new RuntimeException("Invalid amount");
        }

        MoneyRequest req = new MoneyRequest();
        req.setSender(sender);
        req.setReceiver(receiver);
        req.setAmount(amount);
        req.setNote(note);
        req.setStatus("PENDING");
        req.setCreatedAt(LocalDateTime.now());

        requestRepository.save(req);

        notificationService.notify(
                receiver,
                "Money Request Received",
                "You have a new money request of ₹" + amount
        );
    }

    // Accepts a request, transfers money, and notifies the sender
    @Override
    @Transactional
    public void acceptRequest(Long requestId) {

        logger.info("Attempting to accept money request id: {}", requestId);

        MoneyRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> {
                    logger.error("Request not found id: {}", requestId);
                    return new RuntimeException("Request not found");
                });

        if (!req.getStatus().equals("PENDING")) {
            logger.warn("Request already processed id: {}", requestId);
            throw new RuntimeException("Request already processed");
        }

        User currentUser = userService.getCurrentUser();

        if (!req.getReceiver().getUserId().equals(currentUser.getUserId())) {
            logger.error("Unauthorized request acceptance attempt by {}",
                    currentUser.getEmail());
            throw new RuntimeException("Unauthorized action");
        }

        walletService.sendMoneyInternal(
                currentUser,
                req.getSender(),
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

    // Rejects a request and notifies the sender
    @Override
    public void rejectRequest(Long requestId) {

        logger.info("Attempting to reject money request id: {}", requestId);

        MoneyRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> {
                    logger.error("Request not found id: {}", requestId);
                    return new RuntimeException("Request not found");
                });

        if (!req.getStatus().equals("PENDING")) {
            logger.warn("Request already processed id: {}", requestId);
            throw new RuntimeException("Request already processed");
        }

        User currentUser = userService.getCurrentUser();

        if (!req.getReceiver().getUserId().equals(currentUser.getUserId())) {
            logger.error("Unauthorized reject attempt by {}",
                    currentUser.getEmail());
            throw new RuntimeException("Unauthorized action");
        }

        req.setStatus("REJECTED");
        requestRepository.save(req);

        notificationService.notify(
                req.getSender(),
                "Request Rejected",
                "Your money request was rejected"
        );
    }

    // Returns all pending requests where current user is receiver
    @Override
    public List<MoneyRequest> getIncomingRequests() {

        User currentUser = userService.getCurrentUser();

        logger.info("Fetching incoming requests for {}",
                currentUser.getEmail());

        return requestRepository.findByReceiverAndStatus(currentUser, "PENDING");
    }

    // Returns all money requests sent by the current user
    @Override
    public List<MoneyRequest> mySentRequests() {

        User currentUser = userService.getCurrentUser();

        logger.info("Fetching sent requests for {}",
                currentUser.getEmail());

        return requestRepository.findBySender(currentUser);
    }
}