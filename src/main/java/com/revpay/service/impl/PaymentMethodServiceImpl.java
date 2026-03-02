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

/**
 * Service implementation for managing payment methods (Cards).
 *
 * Responsibilities:
 * - Add new card
 * - Validate card details
 * - Fetch user cards
 * - Delete card securely
 */
@Service
public class PaymentMethodServiceImpl implements PaymentMethodService {

    // Logger to track card-related operations
    private static final Logger logger =
            LogManager.getLogger(PaymentMethodServiceImpl.class);

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private UserService userService;

    /**
     * Adds a new card for the currently logged-in user.
     *
     * Validations performed:
     * - Card number must be 16 digits
     * - CVV must be 3 digits
     * - Expiry format must be MM/YY
     * - Expiry must not be in the past
     * - Card holder name must not be empty
     *
     * If validation passes:
     * - Card is saved with default balance (50000.0)
     */
    @Override
    public void addCard(String number, String holder, String expiry, String cvv) {

        logger.info("Attempting to add new card");

        // Validate card number (must be exactly 16 digits)
        if (!number.matches("\\d{16}")) {
            logger.warn("Invalid card number format");
            throw new RuntimeException("Invalid card number");
        }

        // Validate CVV (must be exactly 3 digits)
        if (!cvv.matches("\\d{3}")) {
            logger.warn("Invalid CVV format");
            throw new RuntimeException("Invalid CVV");
        }

        // Validate expiry format (MM/YY)
        if (!expiry.matches("(0[1-9]|1[0-2])/\\d{2}")) {
            logger.warn("Invalid expiry format");
            throw new RuntimeException("Invalid expiry format MM/YY");
        }

        // Validate card holder name
        if (holder == null || holder.isBlank()) {
            logger.warn("Card holder name missing");
            throw new RuntimeException("Card holder name required");
        }

        // Validate expiry date is not in the past
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

        // Get currently logged-in user
        User user = userService.getCurrentUser();

        logger.info("Saving card for user: {}", user.getEmail());

        // Create and populate PaymentMethod entity
        PaymentMethod card = new PaymentMethod();
        card.setUser(user);
        card.setCardNumber(number);
        card.setCardHolderName(holder);
        card.setExpiry(expiry);
        card.setCvv(cvv);

        // Default available balance for demo purposes
        card.setAvailableBalance(50000.0);

        card.setCreatedAt(LocalDateTime.now());

        // Save card to database
        paymentMethodRepository.save(card);

        logger.info("Card added successfully for user: {}", user.getEmail());
    }

    /**
     * Returns all cards belonging to the currently logged-in user.
     *
     * Used in:
     * - Cards page
     * - Send money page
     * - Invoice payment page
     */
    @Override
    public List<PaymentMethod> myCards() {

        User user = userService.getCurrentUser();

        logger.info("Fetching cards for user: {}", user.getEmail());

        return paymentMethodRepository.findByUser(user);
    }

    /**
     * Deletes a card by ID.
     *
     * Security checks:
     * - Card must exist
     * - Card must belong to currently logged-in user
     *
     * Prevents:
     * - Deleting someone else's card
     */
    @Override
    public void deleteCard(Long id) {

        User user = userService.getCurrentUser();

        logger.warn("User {} attempting to delete card id: {}", user.getEmail(), id);

        // Fetch card from DB
        PaymentMethod card = paymentMethodRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Card not found with id: {}", id);
                    return new RuntimeException("Card not found");
                });

        // Ensure card belongs to logged-in user
        if (!card.getUser().getUserId().equals(user.getUserId())) {
            logger.error("Unauthorized card deletion attempt by user: {}", user.getEmail());
            throw new RuntimeException("Unauthorized");
        }

        // Delete card
        paymentMethodRepository.delete(card);

        logger.info("Card deleted successfully by user: {}", user.getEmail());
    }
}