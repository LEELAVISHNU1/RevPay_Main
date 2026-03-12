package com.revpay;

import com.revpay.entity.PaymentMethod;
import com.revpay.entity.User;
import com.revpay.entity.Wallet;
import com.revpay.repository.PaymentMethodRepository;
import com.revpay.repository.TransactionRepository;
import com.revpay.repository.UserRepository;
import com.revpay.repository.WalletRepository;
import com.revpay.service.impl.WalletServiceImpl;
import com.revpay.service.interfaces.NotificationService;
import com.revpay.service.interfaces.TransactionService;
import com.revpay.service.interfaces.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

	@Mock
	private WalletRepository walletRepository;

	@Mock
	private UserService userService;

	@Mock
	private TransactionService transactionService;

	@Mock
	private TransactionRepository transactionRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private PaymentMethodRepository paymentMethodRepository;

	@Mock
	private NotificationService notificationService;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private WalletServiceImpl walletService;

	@Test
	void testCreateWallet_Success() {
		User user = new User();
		user.setUserId(1L);
		walletService.createWallet(user);

		verify(walletRepository, times(1)).save(any(Wallet.class));
	}

	@Test
	void testGetMyWallet_Success() {
		User user = new User();
		user.setUserId(1L);

		Wallet wallet = new Wallet();
		wallet.setWalletId(1L);
		wallet.setUser(user);
		wallet.setBalance(1000.00);

		when(userService.getCurrentUser()).thenReturn(user);
		when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));

		Wallet result = walletService.getMyWallet();
		assertNotNull(result);
		assertEquals(1L, result.getWalletId());
		assertEquals(1000.00, result.getBalance());
		verify(userService, times(1)).getCurrentUser();
		verify(walletRepository, times(1)).findByUser(user);
	}

	@Test
	void testGetMyWallet_NotFound_ThrowsException() {
		User user = new User();
		user.setUserId(1L);

		when(userService.getCurrentUser()).thenReturn(user);
		when(walletRepository.findByUser(user)).thenReturn(Optional.empty());
		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			walletService.getMyWallet();
		});

		assertEquals("Wallet not found", exception.getMessage());
		verify(userService, times(1)).getCurrentUser();
		verify(walletRepository, times(1)).findByUser(user);
	}

	@Test
	void testAddMoney_Success() {
		User user = new User();
		user.setUserId(1L);

		Wallet wallet = new Wallet();
		wallet.setWalletId(1L);
		wallet.setUser(user);
		wallet.setBalance(1000.00);

		when(userService.getCurrentUser()).thenReturn(user);
		when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));
		when(walletRepository.save(any(Wallet.class))).thenAnswer(inv -> inv.getArgument(0));

		walletService.addMoney(500.00, "Test add money");
		assertEquals(1500.00, wallet.getBalance(), 0.0001);
		verify(walletRepository, times(1)).save(wallet);
	}

	@Test
	void testAddMoney_InvalidAmount_ThrowsException() {
		Double invalidAmount = -100.00;

		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			walletService.addMoney(invalidAmount, "Test");
		});

		assertEquals("Invalid amount", exception.getMessage());
		verify(walletRepository, never()).save(any());
		verify(transactionRepository, never()).save(any());
	}

	@Test
	void testSendMoney_Success() {
		User sender = new User();
		sender.setUserId(1L);
		sender.setEmail("sender@test.com");
		sender.setTransactionPin("encodedPin");

		User receiver = new User();
		receiver.setUserId(2L);
		receiver.setEmail("receiver@test.com");

		Wallet senderWallet = new Wallet();
		senderWallet.setWalletId(1L);
		senderWallet.setUser(sender);
		senderWallet.setBalance(1000.00);

		Wallet receiverWallet = new Wallet();
		receiverWallet.setWalletId(2L);
		receiverWallet.setUser(receiver);
		receiverWallet.setBalance(500.00);

		when(userService.getCurrentUser()).thenReturn(sender);
		when(passwordEncoder.matches("1234", sender.getTransactionPin())).thenReturn(true);
		when(userRepository.findByEmail("receiver@test.com")).thenReturn(Optional.of(receiver));
		when(walletRepository.findByUser(sender)).thenReturn(Optional.of(senderWallet));
		when(walletRepository.findByUser(receiver)).thenReturn(Optional.of(receiverWallet));

		walletService.sendMoney("receiver@test.com", 300.00, "Test payment", "1234");

		assertEquals(700.00, senderWallet.getBalance());
		assertEquals(800.00, receiverWallet.getBalance());
		verify(walletRepository, times(1)).save(senderWallet);
		verify(walletRepository, times(1)).save(receiverWallet);
		verify(transactionRepository, times(2)).save(any());
		verify(notificationService, times(2)).notify(any(), anyString(), anyString());
	}

	@Test
	void testSendMoney_InsufficientBalance_ThrowsException() {

		User sender = new User();
		sender.setUserId(1L);
		sender.setTransactionPin("encodedPin");

		User receiver = new User();
		receiver.setUserId(2L);

		Wallet senderWallet = new Wallet();
		senderWallet.setWalletId(1L);
		senderWallet.setUser(sender);
		senderWallet.setBalance(100.00);

		when(userService.getCurrentUser()).thenReturn(sender);
		when(passwordEncoder.matches("1234", sender.getTransactionPin())).thenReturn(true);
		when(userRepository.findByEmail("receiver@test.com")).thenReturn(Optional.of(receiver));
		when(walletRepository.findByUser(sender)).thenReturn(Optional.of(senderWallet));
		when(walletRepository.findByUser(receiver)).thenReturn(Optional.of(new Wallet()));

		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			walletService.sendMoney("receiver@test.com", 300.00, "Test", "1234");
		});

		assertEquals("Insufficient balance", exception.getMessage());
		verify(walletRepository, never()).save(any());
	}

	@Test
	void testSendMoney_InvalidPin_ThrowsException() {

		User sender = new User();
		sender.setUserId(1L);
		sender.setTransactionPin("encodedPin");

		when(userService.getCurrentUser()).thenReturn(sender);
		when(passwordEncoder.matches("wrongPin", sender.getTransactionPin())).thenReturn(false);

		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			walletService.sendMoney("receiver@test.com", 300.00, "Test", "wrongPin");
		});

		assertEquals("Invalid Transaction PIN", exception.getMessage());
	}

	@Test
	void testSendMoney_SelfTransfer_ThrowsException() {

		User sender = new User();
		sender.setUserId(1L);
		sender.setEmail("sender@test.com");
		sender.setTransactionPin("encodedPin");

		when(userService.getCurrentUser()).thenReturn(sender);
		when(passwordEncoder.matches("1234", sender.getTransactionPin())).thenReturn(true);
		when(userRepository.findByEmail("sender@test.com")).thenReturn(Optional.of(sender));

		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			walletService.sendMoney("sender@test.com", 300.00, "Test", "1234");
		});

		assertEquals("Cannot send money to yourself", exception.getMessage());
	}

	@Test
	void testAddMoneyViaCard_Success() {
		User user = new User();
		user.setUserId(1L);

		PaymentMethod card = new PaymentMethod();
		card.setMethodId(1L);
		card.setUser(user);
		card.setAvailableBalance(1000.00);
		card.setCardNumber("1234567890123456");

		Wallet wallet = new Wallet();
		wallet.setWalletId(1L);
		wallet.setUser(user);
		wallet.setBalance(500.00);

		when(userService.getCurrentUser()).thenReturn(user);
		when(paymentMethodRepository.findById(1L)).thenReturn(Optional.of(card));
		when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));
		walletService.addMoneyViaCard(1L, 300.00);

		assertEquals(700.00, card.getAvailableBalance());
		assertEquals(800.00, wallet.getBalance());
		verify(paymentMethodRepository, times(1)).save(card);
		verify(walletRepository, times(1)).save(wallet);
		verify(transactionRepository, times(1)).save(any());
	}

	@Test
	void testDebitUser_Success() {
		User user = new User();
		user.setUserId(1L);

		Wallet wallet = new Wallet();
		wallet.setWalletId(1L);
		wallet.setUser(user);
		wallet.setBalance(1000.00);

		when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));
		walletService.debitUser(user, 300.00, "Test debit");
		assertEquals(700.00, wallet.getBalance());
		verify(walletRepository, times(1)).save(wallet);
		verify(transactionRepository, times(1)).save(any());
	}

	@Test
	void testCreditUser_Success() {
		User user = new User();
		user.setUserId(1L);

		Wallet wallet = new Wallet();
		wallet.setWalletId(1L);
		wallet.setUser(user);
		wallet.setBalance(1000.00);

		when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));
		walletService.creditUser(user, 500.00, "Test credit");
		assertEquals(1500.00, wallet.getBalance());
		verify(walletRepository, times(1)).save(wallet);
		verify(transactionRepository, times(1)).save(any());
	}
}