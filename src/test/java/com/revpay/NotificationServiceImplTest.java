package com.revpay;



import com.revpay.entity.Notification;
import com.revpay.entity.User;
import com.revpay.repository.NotificationRepository;
import com.revpay.service.impl.NotificationServiceImpl;
import com.revpay.service.interfaces.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private User user;

    @BeforeEach
    void setup() {
        user = new User();
        user.setUserId(1L);
        user.setEmail("test@gmail.com");
    }

    @Test
    void testNotify() {
        notificationService.notify(user, "Title", "Message");

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

//    @Test
//    void testGetUnreadNotifications() {
//        when(userService.getCurrentUser()).thenReturn(user);
//        when(notificationRepository.findByUserAndIsReadFalse(user))
//                .thenReturn(List.of(new Notification()));
//
//        List<Notification> result = notificationService.getUnreadNotifications();
//
//        assertEquals(1, result.size());
//        verify(notificationRepository).findByUserAndIsReadFalse(user);
//    }

    @Test
    void testClearAllNotifications() {
        when(userService.getCurrentUser()).thenReturn(user);

        notificationService.clearAllNotifications();

        verify(notificationRepository).deleteByUser(user);
    }
}
