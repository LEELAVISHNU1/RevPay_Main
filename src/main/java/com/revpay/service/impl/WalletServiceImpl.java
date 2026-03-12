package com.revpay.service.impl;

import com.revpay.entity.PaymentMethod;
import com.revpay.entity.Transaction;
import com.revpay.entity.User;
import com.revpay.entity.Wallet;
import com.revpay.repository.PaymentMethodRepository;
import com.revpay.repository.TransactionRepository;
import com.revpay.repository.UserRepository;
import com.revpay.repository.WalletRepository;
import com.revpay.service.interfaces.NotificationService;
import com.revpay.service.interfaces.TransactionService;
import com.revpay.service.interfaces.UserService;
import com.revpay.service.interfaces.WalletService;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Service
public class WalletServiceImpl implements WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final Logger logger = LogManager.getLogger(WalletServiceImpl.class);

    // Creates a wallet for a new user with default balance
    @Override
    public void createWallet(User user) {

        logger.info("Creating wallet for user: {}", user.getEmail());

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setBalance(0.0);
        wallet.setStatus("ACTIVE");
        wallet.setUpdatedAt(LocalDateTime.now());

        walletRepository.save(wallet);

        logger.info("Wallet created successfully for user: {}", user.getEmail());
    }

    // Returns wallet of the currently logged-in user
    @Override
    public Wallet getMyWallet() {

        User user = userService.getCurrentUser();

        return walletRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
    }

    // Adds money to wallet and records a transaction
    @Override
    @Transactional
    public void addMoney(Double amount, String remark) {

        logger.info("Adding money to wallet. Amount={}", amount);

        if (amount == null || amount <= 0) {
            logger.warn("Invalid addMoney amount");
            throw new RuntimeException("Invalid amount");
        }

        User user = userService.getCurrentUser();

        Wallet wallet = getMyWallet();

        wallet.setBalance(wallet.getBalance() + amount);
        wallet.setUpdatedAt(LocalDateTime.now());

        walletRepository.save(wallet);

        logger.info("Wallet credited successfully. New balance={}", wallet.getBalance());

        Transaction txn = new Transaction();
        txn.setWallet(wallet);
        txn.setAmount(amount);
        txn.setTxnType("MONEY_ADDED");
        txn.setBalanceAfterTxn(wallet.getBalance());
        txn.setCreatedAt(LocalDateTime.now());
        txn.setRemark(remark != null ? remark : "Money added to wallet");

        transactionRepository.save(txn);

        notificationService.notify(
                user,
                "Wallet Credited",
                "₹" + amount + " added to your wallet"
        );
    }

    // Sends money from wallet to another user using transaction PIN
    @Override
    @Transactional
    public void sendMoney(String receiverEmail, Double amount, String remark, String inputPin) {

        User sender = userService.getCurrentUser();

        logger.info("User {} attempting to send money to {}", sender.getEmail(), receiverEmail);

        if (!passwordEncoder.matches(inputPin, sender.getTransactionPin())) {
            logger.error("Invalid transaction PIN attempt by user: {}", sender.getEmail());
            throw new RuntimeException("Invalid Transaction PIN");
        }

        if (amount == null || amount <= 0) {
            logger.warn("Invalid send amount");
            throw new RuntimeException("Invalid amount");
        }

        User receiver = userRepository.findByEmail(receiverEmail)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        if (sender.getUserId().equals(receiver.getUserId()))
            throw new RuntimeException("Cannot send money to yourself");

        Wallet senderWallet = walletRepository.findByUser(sender)
                .orElseThrow(() -> new RuntimeException("Sender wallet missing"));

        Wallet receiverWallet = walletRepository.findByUser(receiver)
                .orElseThrow(() -> new RuntimeException("Receiver wallet missing"));

        if (senderWallet.getBalance() < amount)
            throw new RuntimeException("Insufficient balance");

        senderWallet.setBalance(senderWallet.getBalance() - amount);
        receiverWallet.setBalance(receiverWallet.getBalance() + amount);

        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);

        Transaction sendTxn = new Transaction();
        sendTxn.setWallet(senderWallet);
        sendTxn.setAmount(-amount);
        sendTxn.setTxnType("SEND");
        sendTxn.setBalanceAfterTxn(senderWallet.getBalance());
        sendTxn.setCreatedAt(LocalDateTime.now());
        sendTxn.setRemark("Sent to " + receiverEmail + " : " + remark);
        transactionRepository.save(sendTxn);

        Transaction receiveTxn = new Transaction();
        receiveTxn.setWallet(receiverWallet);
        receiveTxn.setAmount(amount);
        receiveTxn.setTxnType("RECEIVE");
        receiveTxn.setBalanceAfterTxn(receiverWallet.getBalance());
        receiveTxn.setCreatedAt(LocalDateTime.now());
        receiveTxn.setRemark("Received from " + sender.getEmail());
        transactionRepository.save(receiveTxn);

        notificationService.notify(sender, "Money Sent",
                "You sent ₹" + amount + " to " + receiver.getEmail());

        notificationService.notify(receiver, "Money Received",
                "You received ₹" + amount + " from " + sender.getEmail());

        logger.info("Money transfer successful from {} to {} amount={}",
                sender.getEmail(), receiverEmail, amount);
    }

    // Adds money from card to wallet after deducting from card balance
    @Override
    @Transactional
    public void addMoneyViaCard(Long cardId, Double amount) {

        if (amount == null || amount <= 0)
            throw new RuntimeException("Invalid amount");

        User user = userService.getCurrentUser();

        PaymentMethod card = paymentMethodRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        if (!card.getUser().getUserId().equals(user.getUserId()))
            throw new RuntimeException("Unauthorized card");

        if (card.getAvailableBalance() < amount)
            throw new RuntimeException("Insufficient bank balance");

        card.setAvailableBalance(card.getAvailableBalance() - amount);
        paymentMethodRepository.save(card);

        Wallet wallet = getMyWallet();
        wallet.setBalance(wallet.getBalance() + amount);
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);

        Transaction txn = new Transaction();
        txn.setWallet(wallet);
        txn.setAmount(amount);
        txn.setTxnType("CARD_TO_WALLET");
        txn.setBalanceAfterTxn(wallet.getBalance());
        txn.setCreatedAt(LocalDateTime.now());
        txn.setRemark("Added via card ending " +
                card.getCardNumber().substring(card.getCardNumber().length() - 4));

        transactionRepository.save(txn);
    }

    // Credits wallet of a specific user (used for loan approval)
    @Override
    public void creditUser(User user, Double amount, String remark) {

        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        double newBalance = wallet.getBalance() + amount;
        wallet.setBalance(newBalance);
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);

        Transaction txn = new Transaction();
        txn.setWallet(wallet);
        txn.setAmount(amount);
        txn.setTxnType("LOAN_CREDIT");
        txn.setBalanceAfterTxn(newBalance);
        txn.setCreatedAt(LocalDateTime.now());
        txn.setRemark(remark);

        transactionRepository.save(txn);
    }

    // Deducts money from wallet (used for EMI payments)
    @Override
    public void debitUser(User user, Double amount, String remark) {

        if (amount == null || amount <= 0)
            throw new RuntimeException("Invalid amount");

        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        if (wallet.getBalance() < amount)
            throw new RuntimeException("Insufficient balance");

        double newBalance = wallet.getBalance() - amount;

        wallet.setBalance(newBalance);
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);

        Transaction txn = new Transaction();
        txn.setWallet(wallet);
        txn.setAmount(-amount);
        txn.setTxnType("EMI_PAYMENT");
        txn.setBalanceAfterTxn(newBalance);
        txn.setCreatedAt(LocalDateTime.now());
        txn.setRemark(remark);

        transactionRepository.save(txn);
    }

    // Pays invoice using card and credits receiver wallet
    @Override
    @Transactional
    public void payUsingCard(Long cardId, String receiverEmail, Double amount, String remark) {

        User sender = userService.getCurrentUser();

        PaymentMethod card = paymentMethodRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        if (!card.getUser().getUserId().equals(sender.getUserId()))
            throw new RuntimeException("Unauthorized card");

        if (card.getAvailableBalance() < amount)
            throw new RuntimeException("Insufficient bank balance");

        User receiver = userRepository.findByEmail(receiverEmail)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        Wallet receiverWallet = walletRepository.findByUser(receiver)
                .orElseThrow(() -> new RuntimeException("Receiver wallet missing"));

        card.setAvailableBalance(card.getAvailableBalance() - amount);
        paymentMethodRepository.save(card);

        receiverWallet.setBalance(receiverWallet.getBalance() + amount);
        walletRepository.save(receiverWallet);

        Wallet senderWallet = walletRepository.findByUser(sender)
                .orElseThrow(() -> new RuntimeException("Sender wallet missing"));

        Transaction senderTxn = new Transaction();
        senderTxn.setWallet(senderWallet);
        senderTxn.setAmount(-amount);
        senderTxn.setTxnType("CARD_INVOICE_PAYMENT");
        senderTxn.setBalanceAfterTxn(senderWallet.getBalance());
        senderTxn.setCreatedAt(LocalDateTime.now());
        senderTxn.setRemark("Invoice paid via card to " + receiver.getEmail());
        transactionRepository.save(senderTxn);

        Transaction receiverTxn = new Transaction();
        receiverTxn.setWallet(receiverWallet);
        receiverTxn.setAmount(amount);
        receiverTxn.setTxnType("INVOICE_RECEIVED");
        receiverTxn.setBalanceAfterTxn(receiverWallet.getBalance());
        receiverTxn.setCreatedAt(LocalDateTime.now());
        receiverTxn.setRemark("Invoice paid by " + sender.getEmail());
        transactionRepository.save(receiverTxn);

        notificationService.notify(sender,
                "Invoice Paid (Card)",
                "₹" + amount + " paid using card");

        notificationService.notify(receiver,
                "Invoice Payment Received",
                "₹" + amount + " received via card");
    }

    // Pays invoice using wallet balance
    @Override
    @Transactional
    public void payToUser(User receiver, Double amount, String remark) {

        User sender = userService.getCurrentUser();

        Wallet senderWallet = walletRepository.findByUser(sender)
                .orElseThrow(() -> new RuntimeException("Wallet missing"));

        if (senderWallet.getBalance() < amount)
            throw new RuntimeException("Insufficient balance");

        Wallet receiverWallet = walletRepository.findByUser(receiver)
                .orElseThrow(() -> new RuntimeException("Receiver wallet missing"));

        senderWallet.setBalance(senderWallet.getBalance() - amount);
        walletRepository.save(senderWallet);

        receiverWallet.setBalance(receiverWallet.getBalance() + amount);
        walletRepository.save(receiverWallet);

        Transaction senderTxn = new Transaction();
        senderTxn.setWallet(senderWallet);
        senderTxn.setAmount(-amount);
        senderTxn.setTxnType("INVOICE_PAYMENT");
        senderTxn.setBalanceAfterTxn(senderWallet.getBalance());
        senderTxn.setCreatedAt(LocalDateTime.now());
        senderTxn.setRemark("Paid invoice to " + receiver.getEmail());
        transactionRepository.save(senderTxn);

        Transaction receiverTxn = new Transaction();
        receiverTxn.setWallet(receiverWallet);
        receiverTxn.setAmount(amount);
        receiverTxn.setTxnType("INVOICE_RECEIVED");
        receiverTxn.setBalanceAfterTxn(receiverWallet.getBalance());
        receiverTxn.setCreatedAt(LocalDateTime.now());
        receiverTxn.setRemark("Invoice paid by " + sender.getEmail());
        transactionRepository.save(receiverTxn);

        notificationService.notify(sender,
                "Invoice Paid",
                "You paid ₹" + amount + " to " + receiver.getEmail());

        notificationService.notify(receiver,
                "Invoice Payment Received",
                "You received ₹" + amount + " from " + sender.getEmail());
    }

    // Deducts EMI from card balance
    @Override
    @Transactional
    public void payLoanUsingCard(Long cardId, Double amount, String remark) {

        User user = userService.getCurrentUser();

        PaymentMethod card = paymentMethodRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        if (!card.getUser().getUserId().equals(user.getUserId()))
            throw new RuntimeException("Unauthorized card");

        if (card.getAvailableBalance() < amount)
            throw new RuntimeException("Insufficient bank balance");

        card.setAvailableBalance(card.getAvailableBalance() - amount);
        paymentMethodRepository.save(card);
    }

    // Sends money using card directly to receiver wallet
    @Override
    @Transactional
    public void sendMoneyUsingCard(Long cardId, String receiverEmail, Double amount, String remark) {

        User sender = userService.getCurrentUser();

        PaymentMethod card = paymentMethodRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        if (!card.getUser().getUserId().equals(sender.getUserId()))
            throw new RuntimeException("Unauthorized card");

        if (card.getAvailableBalance() < amount)
            throw new RuntimeException("Insufficient bank balance");

        User receiver = userRepository.findByEmail(receiverEmail)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        Wallet receiverWallet = walletRepository.findByUser(receiver)
                .orElseThrow(() -> new RuntimeException("Receiver wallet missing"));

        card.setAvailableBalance(card.getAvailableBalance() - amount);
        paymentMethodRepository.save(card);

        receiverWallet.setBalance(receiverWallet.getBalance() + amount);
        walletRepository.save(receiverWallet);

        Transaction receiveTxn = new Transaction();
        receiveTxn.setWallet(receiverWallet);
        receiveTxn.setAmount(amount);
        receiveTxn.setTxnType("CARD_TRANSFER");
        receiveTxn.setBalanceAfterTxn(receiverWallet.getBalance());
        receiveTxn.setCreatedAt(LocalDateTime.now());
        receiveTxn.setRemark("Received via card from " + sender.getEmail());
        transactionRepository.save(receiveTxn);

        notificationService.notify(sender,
                "Money Sent (Card)",
                "₹" + amount + " sent via card");

        notificationService.notify(receiver,
                "Money Received",
                "₹" + amount + " received from " + sender.getEmail());
    }

    // Internal wallet transfer used when accepting money requests
    @Override
    @Transactional
    public void sendMoneyInternal(User sender, User receiver, Double amount, String remark) {

        if (amount == null || amount <= 0)
            throw new RuntimeException("Invalid amount");

        Wallet senderWallet = walletRepository.findByUser(sender)
                .orElseThrow(() -> new RuntimeException("Sender wallet missing"));

        Wallet receiverWallet = walletRepository.findByUser(receiver)
                .orElseThrow(() -> new RuntimeException("Receiver wallet missing"));

        if (senderWallet.getBalance() < amount)
            throw new RuntimeException("Insufficient balance");

        senderWallet.setBalance(senderWallet.getBalance() - amount);
        receiverWallet.setBalance(receiverWallet.getBalance() + amount);

        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);

        Transaction sendTxn = new Transaction();
        sendTxn.setWallet(senderWallet);
        sendTxn.setAmount(-amount);
        sendTxn.setTxnType("REQUEST_PAYMENT");
        sendTxn.setBalanceAfterTxn(senderWallet.getBalance());
        sendTxn.setCreatedAt(LocalDateTime.now());
        sendTxn.setRemark("Paid request to " + receiver.getEmail());
        transactionRepository.save(sendTxn);

        Transaction receiveTxn = new Transaction();
        receiveTxn.setWallet(receiverWallet);
        receiveTxn.setAmount(amount);
        receiveTxn.setTxnType("REQUEST_RECEIVED");
        receiveTxn.setBalanceAfterTxn(receiverWallet.getBalance());
        receiveTxn.setCreatedAt(LocalDateTime.now());
        receiveTxn.setRemark("Request accepted by " + sender.getEmail());
        transactionRepository.save(receiveTxn);
    }

    // Returns wallet of a specific user
    @Override
    public Wallet getWalletByUser(User user) {
        return walletRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
    }
}