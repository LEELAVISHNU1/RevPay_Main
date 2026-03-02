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

/**
 * Service implementation for handling money requests between users.
 *
 * Responsibilities:
 * - Create money request
 * - Accept request (transfer money)
 * - Reject request
 * - Fetch incoming requests
 * - Fetch sent requests
 */
@Service
public class RequestServiceImpl implements RequestService {

    // Logger to track money request activities
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

    // ================= CREATE REQUEST =================

    /**
     * Creates a new money request.
     *
     * Flow:
     * 1. Get currently logged-in user (sender).
     * 2. Validate receiver exists.
     * 3. Prevent self-request.
     * 4. Validate amount > 0.
     * 5. Save request with status PENDING.
     * 6. Send notification to receiver.
     */
    @Override
    public void createRequest(String receiverEmail, Double amount, String note) {

        User sender = userService.getCurrentUser();

        logger.info("User {} creating money request to {}",
                sender.getEmail(), receiverEmail);

        // Validate receiver exists
        User receiver = userRepository.findByEmail(receiverEmail)
                .orElseThrow(() -> {
                    logger.error("Receiver not found: {}", receiverEmail);
                    return new RuntimeException("User not found");
                });

        // Prevent requesting money from yourself
        if (sender.getUserId().equals(receiver.getUserId())) {
            logger.warn("User attempted self money request");
            throw new RuntimeException("You cannot request money from yourself");
        }

        // Validate amount
        if (amount == null || amount <= 0) {
            logger.warn("Invalid money request amount");
            throw new RuntimeException("Invalid amount");
        }

        // Create new MoneyRequest entity
        MoneyRequest req = new MoneyRequest();
        req.setSender(sender);
        req.setReceiver(receiver);
        req.setAmount(amount);
        req.setNote(note);
        req.setStatus("PENDING");
        req.setCreatedAt(LocalDateTime.now());

        // Save request
        requestRepository.save(req);

        logger.info("Money request saved successfully");

        // Notify receiver
        notificationService.notify(
                receiver,
                "Money Request Received",
                "You have a new money request of ₹" + amount
        );
    }

    // ================= ACCEPT REQUEST =================

    /**
     * Accepts a money request.
     *
     * Flow:
     * 1. Validate request exists.
     * 2. Ensure request status is PENDING.
     * 3. Ensure only receiver can accept.
     * 4. Transfer money using walletService.
     * 5. Mark request as ACCEPTED.
     * 6. Notify sender.
     *
     * @Transactional ensures money transfer + status update happen together.
     */
    @Override
    @Transactional
    public void acceptRequest(Long requestId) {

        logger.info("Attempting to accept money request id: {}", requestId);

        // Fetch request
        MoneyRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> {
                    logger.error("Request not found id: {}", requestId);
                    return new RuntimeException("Request not found");
                });

        // Check if already processed
        if (!req.getStatus().equals("PENDING")) {
            logger.warn("Request already processed id: {}", requestId);
            throw new RuntimeException("Request already processed");
        }

        User currentUser = userService.getCurrentUser();

        // Only receiver can accept
        if (!req.getReceiver().getUserId().equals(currentUser.getUserId())) {
            logger.error("Unauthorized request acceptance attempt by {}",
                    currentUser.getEmail());
            throw new RuntimeException("Unauthorized action");
        }

        // Transfer money from receiver to sender
        walletService.sendMoneyInternal(
                currentUser,
                req.getSender(),
                req.getAmount(),
                "Money request accepted"
        );

        // Update status
        req.setStatus("ACCEPTED");
        requestRepository.save(req);

        logger.info("Request accepted successfully id: {}", requestId);

        // Notify sender
        notificationService.notify(
                req.getSender(),
                "Request Accepted",
                "₹" + req.getAmount() + " has been received"
        );
    }

    // ================= REJECT REQUEST =================

    /**
     * Rejects a money request.
     *
     * Flow:
     * 1. Validate request exists.
     * 2. Ensure status is PENDING.
     * 3. Ensure only receiver can reject.
     * 4. Update status to REJECTED.
     * 5. Notify sender.
     */
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

        // Only receiver can reject
        if (!req.getReceiver().getUserId().equals(currentUser.getUserId())) {
            logger.error("Unauthorized reject attempt by {}",
                    currentUser.getEmail());
            throw new RuntimeException("Unauthorized action");
        }

        // Update status
        req.setStatus("REJECTED");
        requestRepository.save(req);

        logger.info("Request rejected successfully id: {}", requestId);

        // Notify sender
        notificationService.notify(
                req.getSender(),
                "Request Rejected",
                "Your money request was rejected"
        );
    }

    // ================= GET INCOMING REQUESTS =================

    /**
     * Returns all PENDING money requests
     * where current user is the receiver.
     *
     * Used in:
     * - Incoming requests page
     * - Dashboard
     */
    @Override
    public List<MoneyRequest> getIncomingRequests() {

        User currentUser = userService.getCurrentUser();

        logger.info("Fetching incoming requests for {}",
                currentUser.getEmail());

        return requestRepository.findByReceiverAndStatus(currentUser, "PENDING");
    }

    // ================= GET SENT REQUESTS =================

    /**
     * Returns all money requests created by current user.
     *
     * Used in:
     * - Dashboard (sent requests section)
     */
    @Override
    public List<MoneyRequest> mySentRequests() {

        User currentUser = userService.getCurrentUser();

        logger.info("Fetching sent requests for {}",
                currentUser.getEmail());

        return requestRepository.findBySender(currentUser);
    }
}