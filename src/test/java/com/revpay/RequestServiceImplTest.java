package com.revpay;

import com.revpay.entity.MoneyRequest;
import com.revpay.entity.User;
import com.revpay.repository.MoneyRequestRepository;
import com.revpay.repository.UserRepository;
import com.revpay.service.impl.RequestServiceImpl;
import com.revpay.service.interfaces.NotificationService;
import com.revpay.service.interfaces.UserService;
import com.revpay.service.interfaces.WalletService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletService walletService;

    @Mock
    private MoneyRequestRepository requestRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private RequestServiceImpl requestService;

    @Test
    void testCreateRequest_Success() {
        // Given
        User sender = new User();
        sender.setUserId(1L);
        
        User receiver = new User();
        receiver.setUserId(2L);
        receiver.setEmail("receiver@test.com");
        
        when(userService.getCurrentUser()).thenReturn(sender);
        when(userRepository.findByEmail("receiver@test.com")).thenReturn(Optional.of(receiver));

        // When
        requestService.createRequest("receiver@test.com", 500.00, "Test note");

        // Then
        verify(requestRepository, times(1)).save(any(MoneyRequest.class));
        verify(notificationService, times(1)).notify(receiver, "Money Request Received", "You have a new money request of ₹500.0");
    }

    @Test
    void testCreateRequest_UserNotFound_ThrowsException() {
        // Given
        User sender = new User();
        sender.setUserId(1L);
        
        when(userService.getCurrentUser()).thenReturn(sender);
        when(userRepository.findByEmail("invalid@test.com")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            requestService.createRequest("invalid@test.com", 500.00, "Test");
        });
        
        assertEquals("User not found", exception.getMessage());
        verify(requestRepository, never()).save(any());
    }

    @Test
    void testCreateRequest_SelfRequest_ThrowsException() {
        // Given
        User sender = new User();
        sender.setUserId(1L);
        sender.setEmail("self@test.com");
        
        when(userService.getCurrentUser()).thenReturn(sender);
        when(userRepository.findByEmail("self@test.com")).thenReturn(Optional.of(sender));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            requestService.createRequest("self@test.com", 500.00, "Test");
        });
        
        assertEquals("You cannot request money from yourself", exception.getMessage());
        verify(requestRepository, never()).save(any());
    }

    @Test
    void testCreateRequest_InvalidAmount_ThrowsException() {
        // Given
        User sender = new User();
        sender.setUserId(1L);
        
        User receiver = new User();
        receiver.setUserId(2L);
        
        when(userService.getCurrentUser()).thenReturn(sender);
        when(userRepository.findByEmail("receiver@test.com")).thenReturn(Optional.of(receiver));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            requestService.createRequest("receiver@test.com", -100.00, "Test");
        });
        
        assertEquals("Invalid amount", exception.getMessage());
        verify(requestRepository, never()).save(any());
    }

    @Test
    void testAcceptRequest_Success() {
        // Given
        User currentUser = new User();
        currentUser.setUserId(2L); // receiver
        
        User sender = new User();
        sender.setUserId(1L);
        
        MoneyRequest request = new MoneyRequest();
        request.setRequestId(1L);
        request.setSender(sender);
        request.setReceiver(currentUser);
        request.setAmount(500.00);
        request.setStatus("PENDING");
        
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(userService.getCurrentUser()).thenReturn(currentUser);
        doNothing().when(walletService).sendMoneyInternal(currentUser, sender, 500.00, "Money request accepted");

        // When
        requestService.acceptRequest(1L);

        // Then
        assertEquals("ACCEPTED", request.getStatus());
        verify(requestRepository, times(1)).save(request);
        verify(walletService, times(1)).sendMoneyInternal(currentUser, sender, 500.00, "Money request accepted");
        verify(notificationService, times(1)).notify(sender, "Request Accepted", "₹500.0 has been received");
    }

    @Test
    void testAcceptRequest_NotFound_ThrowsException() {
        // Given
        when(requestRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            requestService.acceptRequest(99L);
        });
        
        assertEquals("Request not found", exception.getMessage());
        verify(walletService, never()).sendMoneyInternal(any(), any(), anyDouble(), anyString());
    }

    @Test
    void testAcceptRequest_AlreadyProcessed_ThrowsException() {
        // Given
        MoneyRequest request = new MoneyRequest();
        request.setRequestId(1L);
        request.setStatus("ACCEPTED");
        
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            requestService.acceptRequest(1L);
        });
        
        assertEquals("Request already processed", exception.getMessage());
        verify(walletService, never()).sendMoneyInternal(any(), any(), anyDouble(), anyString());
    }

    @Test
    void testAcceptRequest_Unauthorized_ThrowsException() {
        // Given
        User currentUser = new User();
        currentUser.setUserId(1L); // different user
        
        User receiver = new User();
        receiver.setUserId(2L);
        
        MoneyRequest request = new MoneyRequest();
        request.setRequestId(1L);
        request.setReceiver(receiver);
        request.setStatus("PENDING");
        
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(userService.getCurrentUser()).thenReturn(currentUser);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            requestService.acceptRequest(1L);
        });
        
        assertEquals("Unauthorized action", exception.getMessage());
        verify(walletService, never()).sendMoneyInternal(any(), any(), anyDouble(), anyString());
    }

    @Test
    void testRejectRequest_Success() {
        // Given
        User currentUser = new User();
        currentUser.setUserId(2L); // receiver
        
        User sender = new User();
        sender.setUserId(1L);
        
        MoneyRequest request = new MoneyRequest();
        request.setRequestId(1L);
        request.setSender(sender);
        request.setReceiver(currentUser);
        request.setAmount(500.00);
        request.setStatus("PENDING");
        
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(userService.getCurrentUser()).thenReturn(currentUser);

        // When
        requestService.rejectRequest(1L);

        // Then
        assertEquals("REJECTED", request.getStatus());
        verify(requestRepository, times(1)).save(request);
        verify(notificationService, times(1)).notify(sender, "Request Rejected", "Your money request was rejected");
    }
}