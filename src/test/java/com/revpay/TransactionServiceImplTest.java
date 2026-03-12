package com.revpay;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.revpay.entity.Transaction;
import com.revpay.entity.User;
import com.revpay.entity.Wallet;
import com.revpay.repository.TransactionRepository;
import com.revpay.repository.WalletRepository;
import com.revpay.service.impl.TransactionServiceImpl;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private WalletRepository walletRepository; 

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Test
    void testCreateTransaction() {

        User user = new User();

        Wallet wallet = new Wallet();
        wallet.setBalance(500.0);

        when(walletRepository.findByUser(user))
                .thenReturn(Optional.of(wallet));   

        transactionService.createTransaction(user, 100.0, "Test");

        verify(transactionRepository, times(1))
                .save(any(Transaction.class));
    }
}