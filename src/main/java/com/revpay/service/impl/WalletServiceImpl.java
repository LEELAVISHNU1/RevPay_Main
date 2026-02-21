package com.revpay.service.impl;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.revpay.entity.Transaction;
import com.revpay.entity.User;
import com.revpay.entity.Wallet;
import com.revpay.repository.TransactionRepository;
import com.revpay.repository.UserRepository;
import com.revpay.repository.WalletRepository;
import com.revpay.security.CustomUserDetails;
import com.revpay.service.interfaces.UserService;
import com.revpay.service.interfaces.WalletService;

@Service
public class WalletServiceImpl implements WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserService userService;

    
    @Autowired
    private UserRepository userRepository;

    @Override
    public void createWalletForUser(User user) {

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setBalance(0.0);
        wallet.setWalletStatus("ACTIVE");
        wallet.setUpdatedAt(LocalDateTime.now());

        walletRepository.save(wallet);
    }

    @Override
    public Wallet getCurrentUserWallet() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();

        return walletRepository.findByUser(userDetails.getUser())
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
    }

    @Override
    public Wallet getWalletOfCurrentUser() {

        User user = userService.getCurrentUser();

        return walletRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
    }

    
    @Override
    public void addMoney(Double amount) {

        if (amount == null || amount <= 0)
            throw new RuntimeException("Invalid amount");

        Wallet wallet = getWalletOfCurrentUser();

        double newBalance = wallet.getBalance() + amount;
        wallet.setBalance(newBalance);
        wallet.setUpdatedAt(LocalDateTime.now());

        walletRepository.save(wallet);

        
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

       
        senderWallet.setBalance(senderWallet.getBalance() - amount);
        receiverWallet.setBalance(receiverWallet.getBalance() + amount);

        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);

        
        Transaction sendTxn = new Transaction();
        sendTxn.setWallet(senderWallet);
        sendTxn.setAmount(amount);
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
    }
}