package com.revpay.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import com.revpay.entity.MoneyRequest;
import com.revpay.entity.User;
import com.revpay.repository.MoneyRequestRepository;
import com.revpay.repository.UserRepository;
import com.revpay.service.interfaces.RequestService;
import com.revpay.service.interfaces.UserService;
import com.revpay.service.interfaces.WalletService;

import jakarta.transaction.Transactional;

@Service
public class RequestServiceImpl implements RequestService {

    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private WalletService walletService;
    @Autowired private MoneyRequestRepository requestRepository;

    @Override
    public void createRequest(String senderEmail, Double amount, String note) {

        User receiver = userService.getCurrentUser();
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        MoneyRequest req = new MoneyRequest();
        req.setSender(sender);
        req.setReceiver(receiver);
        req.setAmount(amount);
        req.setNote(note);
        req.setStatus("PENDING");
        req.setCreatedAt(LocalDateTime.now());

        requestRepository.save(req);
    }

    @Override
    @Transactional
    public void acceptRequest(Long requestId) {

        MoneyRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        walletService.sendMoney(
                req.getReceiver().getEmail(),
                req.getAmount(),
                "Request accepted"
        );

        req.setStatus("ACCEPTED");
        requestRepository.save(req);
    }

    @Override
    public void declineRequest(Long requestId) {

        MoneyRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        req.setStatus("DECLINED");
        requestRepository.save(req);
    }

    @Override
    public List<MoneyRequest> myIncomingRequests() {
        User user = userService.getCurrentUser();
        return requestRepository.findBySenderAndStatus(user, "PENDING");
    }
}