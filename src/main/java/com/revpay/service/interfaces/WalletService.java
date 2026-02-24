package com.revpay.service.interfaces;

import com.revpay.entity.User;
import com.revpay.entity.Wallet;

public interface WalletService {

    void createWallet(User user);

    Wallet getMyWallet();
    
    void addMoney(Double amount);

    void sendMoney(String receiverEmail, Double amount, String remark);

    void addMoneyViaCard(Long cardId, Double amount);

    void creditUser(User user, Double amount, String remark);
    
    void debitUser(User user, Double amount, String remark);
}
