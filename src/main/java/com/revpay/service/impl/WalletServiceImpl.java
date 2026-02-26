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

    

   
    @Override
    public void createWallet(User user) {

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setBalance(0.0);
        wallet.setStatus("ACTIVE");
        wallet.setUpdatedAt(LocalDateTime.now());

        walletRepository.save(wallet);
    }

    @Override
    public Wallet getMyWallet() {

        User user = userService.getCurrentUser();

        return walletRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
    }
    
    @Override
    public void addMoney(Double amount, String remark) {

        if (amount == null || amount <= 0)
            throw new RuntimeException("Invalid amount");

        Wallet wallet = getMyWallet();
        double newBalance = wallet.getBalance() + amount;

        wallet.setBalance(newBalance);
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);

        // transaction record
        Transaction txn = new Transaction();
        txn.setWallet(wallet);
        txn.setAmount(amount);
        txn.setTxnType("ADD_MONEY");
        txn.setBalanceAfterTxn(newBalance);
        txn.setCreatedAt(LocalDateTime.now());
        txn.setRemark(remark == null || remark.isBlank()
                ? "Money added to wallet"
                : remark);

        transactionRepository.save(txn);

        notificationService.notify(wallet.getUser(),
                "Wallet Credited",
                "₹" + amount + " added to wallet");
    }
    
    @Override
    @Transactional
    public void sendMoney(String receiverEmail, Double amount, String remark, String inputPin) {
    	
    	User sender = userService.getCurrentUser();

    	if (!passwordEncoder.matches(inputPin, sender.getTransactionPin())) {
    	    throw new RuntimeException("Invalid Transaction PIN");
    	}

        if (amount == null || amount <= 0)
            throw new RuntimeException("Invalid amount");

//        User sender = userService.getCurrentUser();
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

        // update balances
        senderWallet.setBalance(senderWallet.getBalance() - amount);
        receiverWallet.setBalance(receiverWallet.getBalance() + amount);

        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);

        // sender transaction
        Transaction sendTxn = new Transaction();
        sendTxn.setWallet(senderWallet);
        sendTxn.setAmount(-amount);
        sendTxn.setTxnType("SEND");
        sendTxn.setBalanceAfterTxn(senderWallet.getBalance());
        sendTxn.setCreatedAt(LocalDateTime.now());
        sendTxn.setRemark("Sent to " + receiverEmail + " : " + remark);
        transactionRepository.save(sendTxn);

        // receiver transaction
        Transaction receiveTxn = new Transaction();
        receiveTxn.setWallet(receiverWallet);
        receiveTxn.setAmount(amount);
        receiveTxn.setTxnType("RECEIVE");
        receiveTxn.setBalanceAfterTxn(receiverWallet.getBalance());
        receiveTxn.setCreatedAt(LocalDateTime.now());
        receiveTxn.setRemark("Received from " + sender.getEmail());
        transactionRepository.save(receiveTxn);
        
        notificationService.notify(sender,
                "Money Sent",
                "You sent ₹" + amount + " to " + receiver.getEmail());

        notificationService.notify(receiver,
                "Money Received",
                "You received ₹" + amount + " from " + sender.getEmail());
    }

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

        // ⭐ CHECK CARD BALANCE
        if (card.getAvailableBalance() < amount)
            throw new RuntimeException("Insufficient bank balance");

        // ⭐ DEDUCT FROM CARD
        card.setAvailableBalance(card.getAvailableBalance() - amount);
        paymentMethodRepository.save(card);

        // ⭐ ADD TO WALLET
        Wallet wallet = getMyWallet();
        wallet.setBalance(wallet.getBalance() + amount);
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);

        // ⭐ TRANSACTION
        Transaction txn = new Transaction();
        txn.setWallet(wallet);
        txn.setAmount(amount);
        txn.setTxnType("CARD_TO_WALLET");
        txn.setBalanceAfterTxn(wallet.getBalance());
        txn.setCreatedAt(LocalDateTime.now());
        txn.setRemark("Added via card ending " +
                card.getCardNumber().substring(card.getCardNumber().length()-4));

        transactionRepository.save(txn);
    }
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

    @Override
    @Transactional
    public void payUsingCard(Long cardId,
                             String receiverEmail,
                             Double amount,
                             String remark) {

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

        // 🔹 Deduct from card
        card.setAvailableBalance(card.getAvailableBalance() - amount);
        paymentMethodRepository.save(card);

        // 🔹 Credit receiver wallet
        receiverWallet.setBalance(receiverWallet.getBalance() + amount);
        walletRepository.save(receiverWallet);

        // ✅ CREATE SENDER TRANSACTION (IMPORTANT FIX)
        Wallet senderWallet = walletRepository.findByUser(sender)
                .orElseThrow(() -> new RuntimeException("Sender wallet missing"));

        Transaction senderTxn = new Transaction();
        senderTxn.setWallet(senderWallet);
        senderTxn.setAmount(-amount);
        senderTxn.setTxnType("CARD_INVOICE_PAYMENT");
        senderTxn.setBalanceAfterTxn(senderWallet.getBalance()); // wallet balance unchanged
        senderTxn.setCreatedAt(LocalDateTime.now());
        senderTxn.setRemark("Invoice paid via card to " + receiver.getEmail());
        transactionRepository.save(senderTxn);

        // 🔹 Receiver transaction
        Transaction receiverTxn = new Transaction();
        receiverTxn.setWallet(receiverWallet);
        receiverTxn.setAmount(amount);
        receiverTxn.setTxnType("INVOICE_RECEIVED");
        receiverTxn.setBalanceAfterTxn(receiverWallet.getBalance());
        receiverTxn.setCreatedAt(LocalDateTime.now());
        receiverTxn.setRemark("Invoice paid by " + sender.getEmail());
        transactionRepository.save(receiverTxn);

        // 🔹 Notifications
        notificationService.notify(sender,
                "Invoice Paid (Card)",
                "₹" + amount + " paid using card");

        notificationService.notify(receiver,
                "Invoice Payment Received",
                "₹" + amount + " received via card");
    }
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

        // Deduct sender
        senderWallet.setBalance(senderWallet.getBalance() - amount);
        walletRepository.save(senderWallet);

        // Credit receiver
        receiverWallet.setBalance(receiverWallet.getBalance() + amount);
        walletRepository.save(receiverWallet);

        // 🔹 Sender transaction
        Transaction senderTxn = new Transaction();
        senderTxn.setWallet(senderWallet);
        senderTxn.setAmount(-amount);
        senderTxn.setTxnType("INVOICE_PAYMENT");
        senderTxn.setBalanceAfterTxn(senderWallet.getBalance());
        senderTxn.setCreatedAt(LocalDateTime.now());
        senderTxn.setRemark("Paid invoice to " + receiver.getEmail());
        transactionRepository.save(senderTxn);

        // 🔹 Receiver transaction
        Transaction receiverTxn = new Transaction();
        receiverTxn.setWallet(receiverWallet);
        receiverTxn.setAmount(amount);
        receiverTxn.setTxnType("INVOICE_RECEIVED");
        receiverTxn.setBalanceAfterTxn(receiverWallet.getBalance());
        receiverTxn.setCreatedAt(LocalDateTime.now());
        receiverTxn.setRemark("Invoice paid by " + sender.getEmail());
        transactionRepository.save(receiverTxn);

        // 🔹 Notifications
        notificationService.notify(sender,
                "Invoice Paid",
                "You paid ₹" + amount + " to " + receiver.getEmail());

        notificationService.notify(receiver,
                "Invoice Payment Received",
                "You received ₹" + amount + " from " + sender.getEmail());
    }
    
    @Override
    @Transactional
    public void payLoanUsingCard(Long cardId,
                                 Double amount,
                                 String remark) {

        User user = userService.getCurrentUser();

        PaymentMethod card = paymentMethodRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        if (!card.getUser().getUserId().equals(user.getUserId()))
            throw new RuntimeException("Unauthorized card");

        if (card.getAvailableBalance() < amount)
            throw new RuntimeException("Insufficient bank balance");

        // Deduct card
        card.setAvailableBalance(card.getAvailableBalance() - amount);
        paymentMethodRepository.save(card);

        // Transaction record (optional: create loan wallet txn if needed)
    }
    
    @Override
    @Transactional
    public void sendMoneyUsingCard(Long cardId,
            String receiverEmail,
            Double amount,
            String remark) {

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

        // Deduct from card
        card.setAvailableBalance(card.getAvailableBalance() - amount);
        paymentMethodRepository.save(card);

        // Credit receiver wallet
        receiverWallet.setBalance(receiverWallet.getBalance() + amount);
        walletRepository.save(receiverWallet);

        // Receiver transaction
        Transaction receiveTxn = new Transaction();
        receiveTxn.setWallet(receiverWallet);
        receiveTxn.setAmount(amount);
        receiveTxn.setTxnType("CARD_TRANSFER");
        receiveTxn.setBalanceAfterTxn(receiverWallet.getBalance());
        receiveTxn.setCreatedAt(LocalDateTime.now());
        receiveTxn.setRemark("Received via card from " + sender.getEmail());
        transactionRepository.save(receiveTxn);

        // Notifications
        notificationService.notify(sender,
                "Money Sent (Card)",
                "₹" + amount + " sent via card");

        notificationService.notify(receiver,
                "Money Received",
                "₹" + amount + " received from " + sender.getEmail());
    }
    
    @Override
    @Transactional
    public void sendMoneyInternal(User sender,
                                  User receiver,
                                  Double amount,
                                  String remark) {

        if (amount == null || amount <= 0)
            throw new RuntimeException("Invalid amount");

        Wallet senderWallet = walletRepository.findByUser(sender)
                .orElseThrow(() -> new RuntimeException("Sender wallet missing"));

        Wallet receiverWallet = walletRepository.findByUser(receiver)
                .orElseThrow(() -> new RuntimeException("Receiver wallet missing"));

        if (senderWallet.getBalance() < amount)
            throw new RuntimeException("Insufficient balance");

        // 🔻 Debit sender
        senderWallet.setBalance(senderWallet.getBalance() - amount);

        // 🔺 Credit receiver
        receiverWallet.setBalance(receiverWallet.getBalance() + amount);

        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);

        // 🔹 Sender transaction
        Transaction sendTxn = new Transaction();
        sendTxn.setWallet(senderWallet);
        sendTxn.setAmount(-amount);
        sendTxn.setTxnType("REQUEST_PAYMENT");
        sendTxn.setBalanceAfterTxn(senderWallet.getBalance());
        sendTxn.setCreatedAt(LocalDateTime.now());
        sendTxn.setRemark("Paid request to " + receiver.getEmail());
        transactionRepository.save(sendTxn);

        // 🔹 Receiver transaction
        Transaction receiveTxn = new Transaction();
        receiveTxn.setWallet(receiverWallet);
        receiveTxn.setAmount(amount);
        receiveTxn.setTxnType("REQUEST_RECEIVED");
        receiveTxn.setBalanceAfterTxn(receiverWallet.getBalance());
        receiveTxn.setCreatedAt(LocalDateTime.now());
        receiveTxn.setRemark("Request accepted by " + sender.getEmail());
        transactionRepository.save(receiveTxn);
    }
    
    @Override
    public Wallet getWalletByUser(User user) {
        return walletRepository.findByUser(user)
				.orElseThrow(() -> new RuntimeException("Wallet not found"));
	}
}