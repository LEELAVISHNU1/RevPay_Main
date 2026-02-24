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
import com.revpay.service.interfaces.UserService;
import com.revpay.service.interfaces.WalletService;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class WalletServiceImpl implements WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private UserRepository userRepository;


    @Autowired
    private PaymentMethodRepository paymentMethodRepository;
    
    @Autowired
    private NotificationService notificationService;

   
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
    public void addMoney(Double amount) {

        if (amount == null || amount <= 0)
            throw new RuntimeException("Invalid amount");

        Wallet wallet = getMyWallet();

        double newBalance = wallet.getBalance() + amount;
        wallet.setBalance(newBalance);
        wallet.setUpdatedAt(LocalDateTime.now());

        walletRepository.save(wallet);

        // ⭐ create transaction record
        Transaction txn = new Transaction();
        txn.setWallet(wallet);
        txn.setAmount(amount);
        txn.setTxnType("ADD_MONEY");
        txn.setBalanceAfterTxn(newBalance);
        txn.setCreatedAt(LocalDateTime.now());
        txn.setRemark("Money added to wallet");

        transactionRepository.save(txn);
    }
    
    @Override
    @Transactional
    public void sendMoney(String receiverEmail, Double amount, String remark) {

        if (amount == null || amount <= 0)
            throw new RuntimeException("Invalid amount");

        User sender = userService.getCurrentUser();
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
        sendTxn.setAmount(amount);
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
    public void addMoneyViaCard(Long cardId, Double amount) {

        if (amount == null || amount <= 0)
            throw new RuntimeException("Invalid amount");

        User user = userService.getCurrentUser();

        PaymentMethod card = paymentMethodRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        if (!card.getUser().getUserId().equals(user.getUserId()))
            throw new RuntimeException("Card does not belong to user");

        Wallet wallet = getMyWallet();

        double newBalance = wallet.getBalance() + amount;
        wallet.setBalance(newBalance);
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);

        Transaction txn = new Transaction();
        txn.setWallet(wallet);
        txn.setAmount(amount);
        txn.setTxnType("CARD_DEPOSIT");
        txn.setBalanceAfterTxn(newBalance);
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
        txn.setAmount(amount);
        txn.setTxnType("EMI_PAYMENT");
        txn.setBalanceAfterTxn(newBalance);
        txn.setCreatedAt(LocalDateTime.now());
        txn.setRemark(remark);

        transactionRepository.save(txn);
    }


}
