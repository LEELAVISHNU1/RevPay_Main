package com.revpay.service.interfaces;

import com.revpay.entity.User;
import com.revpay.entity.Wallet;

public interface WalletService {

	void createWallet(User user);

	Wallet getMyWallet();

	void addMoney(Double amount, String remark);

	void addMoneyViaCard(Long cardId, Double amount);

	void creditUser(User user, Double amount, String remark);

	void debitUser(User user, Double amount, String remark);

	void payUsingCard(Long cardId, String receiverEmail, Double amount, String remark);

	void payToUser(User receiver, Double amount, String remark);

	void payLoanUsingCard(Long cardId, Double amount, String remark);

	void sendMoney(String receiverEmail, Double amount, String remark, String pin);

	void sendMoneyUsingCard(Long cardId, String email, Double amount, String remark);

	void sendMoneyInternal(User sender,
            User receiver,
            Double amount,
            String remark);

	Wallet getWalletByUser(User user);

}
