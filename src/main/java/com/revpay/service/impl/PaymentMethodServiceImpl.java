package com.revpay.service.impl;

import com.revpay.entity.PaymentMethod;
import com.revpay.entity.User;
import com.revpay.repository.PaymentMethodRepository;
import com.revpay.service.interfaces.PaymentMethodService;
import com.revpay.service.interfaces.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Service
public class PaymentMethodServiceImpl implements PaymentMethodService {

    private static final Logger logger =
            LogManager.getLogger(PaymentMethodServiceImpl.class);

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private UserService userService;

    // Adds a new card after validating number, CVV, expiry, and holder name
    @Override
    public void addCard(String number, String holder, String expiry, String cvv) {

        logger.info("Attempting to add new card");

        if (!number.matches("\\d{16}")) {
            logger.warn("Invalid card number format");
            throw new RuntimeException("Invalid card number");
        }

        if (!cvv.matches("\\d{3}")) {
            logger.warn("Invalid CVV format");
            throw new RuntimeException("Invalid CVV");
        }

        if (!expiry.matches("(0[1-9]|1[0-2])/\\d{2}")) {
            logger.warn("Invalid expiry format");
            throw new RuntimeException("Invalid expiry format MM/YY");
        }

        if (holder == null || holder.isBlank()) {
            logger.warn("Card holder name missing");
            throw new RuntimeException("Card holder name required");
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yy");
            YearMonth expiryDate = YearMonth.parse(expiry, formatter);
            YearMonth currentMonth = YearMonth.now();

            if (expiryDate.isBefore(currentMonth)) {
                logger.warn("Attempted to add expired card");
                throw new RuntimeException("Card expiry date cannot be in the past");
            }

        } catch (DateTimeParseException e) {
            logger.error("Expiry parsing failed", e);
            throw new RuntimeException("Invalid expiry format MM/YY");
        }

        User user = userService.getCurrentUser();

        logger.info("Saving card for user: {}", user.getEmail());

        PaymentMethod card = new PaymentMethod();
        card.setUser(user);
        card.setCardNumber(number);
        card.setCardHolderName(holder);
        card.setExpiry(expiry);
        card.setCvv(cvv);
        card.setAvailableBalance(50000.0);
        card.setCreatedAt(LocalDateTime.now());

        paymentMethodRepository.save(card);

        logger.info("Card added successfully for user: {}", user.getEmail());
    }

    // Returns all cards belonging to the logged-in user
    @Override
    public List<PaymentMethod> myCards() {

        User user = userService.getCurrentUser();

        logger.info("Fetching cards for user: {}", user.getEmail());

        return paymentMethodRepository.findByUser(user);
    }

    // Deletes a card only if it belongs to the logged-in user
    @Override
    public void deleteCard(Long id) {

        User user = userService.getCurrentUser();

        logger.warn("User {} attempting to delete card id: {}", user.getEmail(), id);

        PaymentMethod card = paymentMethodRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Card not found with id: {}", id);
                    return new RuntimeException("Card not found");
                });

        if (!card.getUser().getUserId().equals(user.getUserId())) {
            logger.error("Unauthorized card deletion attempt by user: {}", user.getEmail());
            throw new RuntimeException("Unauthorized");
        }

        paymentMethodRepository.delete(card);

        logger.info("Card deleted successfully by user: {}", user.getEmail());
    }
}