package com.revpay;

import com.revpay.config.SecurityConfig;
import com.revpay.dto.request.RegisterRequest;
import com.revpay.entity.Role;
import com.revpay.entity.User;
import com.revpay.repository.RoleRepository;
import com.revpay.repository.UserRepository;
import com.revpay.service.impl.AuthServiceImpl;
import com.revpay.service.interfaces.WalletService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private RoleRepository roleRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private WalletService walletService;

	@Mock
	private SecurityConfig securityConfig;

	@Mock
	private AuthenticationManager authenticationManager;

	private AuthServiceImpl authService;

	@BeforeEach
	void setUp() {
		authService = new AuthServiceImpl(securityConfig);

		injectField(authService, "userRepository", userRepository);
		injectField(authService, "roleRepository", roleRepository);
		injectField(authService, "passwordEncoder", passwordEncoder);
		injectField(authService, "walletService", walletService);
		injectField(authService, "authenticationManager", authenticationManager);
	}

	private void injectField(Object target, String fieldName, Object mock) {
		try {
			var field = target.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(target, mock);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	void testRegister_EmailAlreadyExists_ThrowsException() {

		RegisterRequest request = new RegisterRequest();
		request.setEmail("existing@example.com");

		when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(new User()));

		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			authService.register(request);
		});

		assertEquals("Email already registered", exception.getMessage());

		verify(userRepository, never()).save(any());
		verify(walletService, never()).createWallet(any());
	}

	@Test
	void testRegister_PhoneAlreadyExists_ThrowsException() {

		RegisterRequest request = new RegisterRequest();
		request.setEmail("kumar@example.com");
		request.setPhone("1234567890");
		request.setRole("USER");

		when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

		when(roleRepository.findByRoleName("USER")).thenReturn(Optional.of(new Role()));

		when(userRepository.existsByPhone(request.getPhone())).thenReturn(true);

		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			authService.register(request);
		});

		assertEquals("Phone number already registered", exception.getMessage());
	}

	@Test
	void testRegister_RoleNotFound_ThrowsException() {

		RegisterRequest request = new RegisterRequest();
		request.setEmail("kajal@example.com");
		request.setRole("INVALID");

		when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

		when(roleRepository.findByRoleName("INVALID")).thenReturn(Optional.empty());

		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			authService.register(request);
		});

		assertEquals("Role not found", exception.getMessage());
	}

	@Test
	void testRegister_Success() {

		RegisterRequest request = new RegisterRequest();
		request.setFullName("Lakshman");
		request.setEmail("lakshman@example.com");
		request.setPhone("1234567890");
		request.setPassword("password123");
		request.setTransactionPin("1234");
		request.setFavoriteColor("Blue");
		request.setRole("USER");

		Role role = new Role();
		role.setRoleName("USER");

		when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

		when(roleRepository.findByRoleName("USER")).thenReturn(Optional.of(role));

		when(userRepository.existsByPhone(request.getPhone())).thenReturn(false);

		when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

		when(passwordEncoder.encode("1234")).thenReturn("encodedPin");

		authService.register(request);

		verify(userRepository, times(1)).save(any(User.class));
		verify(walletService, times(1)).createWallet(any(User.class));
	}
}