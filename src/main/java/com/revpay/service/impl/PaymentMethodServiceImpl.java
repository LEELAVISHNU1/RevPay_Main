package com.revpay.service.impl;

import com.revpay.entity.PaymentMethod;
import com.revpay.entity.User;
import com.revpay.repository.PaymentMethodRepository;
import com.revpay.service.interfaces.PaymentMethodService;
import com.revpay.service.interfaces.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Service
public class PaymentMethodServiceImpl implements PaymentMethodService {

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private UserService userService;

    @Override
    public void addCard(String number, String holder, String expiry, String cvv) {

        // 1️⃣ Card number validation (16 digits)
        if (!number.matches("\\d{16}"))
            throw new RuntimeException("Invalid card number");

        // 2️⃣ CVV validation (3 digits)
        if (!cvv.matches("\\d{3}"))
            throw new RuntimeException("Invalid CVV");

        // 3️⃣ Expiry format validation (MM/YY)
        if (!expiry.matches("(0[1-9]|1[0-2])/\\d{2}"))
            throw new RuntimeException("Invalid expiry format MM/YY");

        // 4️⃣ Holder validation
        if (holder == null || holder.isBlank())
            throw new RuntimeException("Card holder name required");

        // 5️⃣ Expiry date must not be in the past
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yy");
            YearMonth expiryDate = YearMonth.parse(expiry, formatter);
            YearMonth currentMonth = YearMonth.now();

            if (expiryDate.isBefore(currentMonth)) {
                throw new RuntimeException("Card expiry date cannot be in the past");
            }

        } catch (DateTimeParseException e) {
            throw new RuntimeException("Invalid expiry format MM/YY");
        }

        // 6️⃣ Save card if everything is valid
        PaymentMethod card = new PaymentMethod();
        card.setUser(userService.getCurrentUser());
        card.setCardNumber(number);
        card.setCardHolderName(holder);
        card.setExpiry(expiry);
        card.setCvv(cvv);
        card.setAvailableBalance(50000.0); // simulated bank balance
        card.setCreatedAt(LocalDateTime.now());

        paymentMethodRepository.save(card);
    }

    @Override
    public List<PaymentMethod> myCards() {
        return paymentMethodRepository.findByUser(userService.getCurrentUser());
    }

    @Override
    public void deleteCard(Long id) {

        User user = userService.getCurrentUser();

        PaymentMethod card = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        if(!card.getUser().getUserId().equals(user.getUserId()))
            throw new RuntimeException("Unauthorized");

        paymentMethodRepository.delete(card);
    }
}
