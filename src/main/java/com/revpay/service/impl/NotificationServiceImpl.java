package com.revpay.service.impl;

import com.revpay.dto.response.PageResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.revpay.entity.Notification;
import com.revpay.entity.User;
import com.revpay.repository.NotificationRepository;
import com.revpay.service.interfaces.NotificationService;
import com.revpay.service.interfaces.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired private NotificationRepository notificationRepository;
    @Autowired private UserService userService;

    @Override
    public void notify(User user, String title, String message) {

        Notification n = new Notification();
        n.setUser(user);
        n.setTitle(title);
        n.setMessage(message);
        n.setRead(false);
        n.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(n);
    }

    @Override
    public List<Notification> myNotifications() {
        return notificationRepository.findByUserOrderByCreatedAtDesc(
                userService.getCurrentUser()
        );
    }
    
    @Override
    public PageResponse<?> myNotifications(int page, int size) {

        User user = userService.getCurrentUser();

        Pageable pageable = PageRequest.of(page, size);

        Page<Notification> notificationPage =
                notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable);

        return new PageResponse<>(
                notificationPage.getContent(),
                notificationPage.getNumber(),
                notificationPage.getTotalPages(),
                notificationPage.getTotalElements()
        );
    }
}