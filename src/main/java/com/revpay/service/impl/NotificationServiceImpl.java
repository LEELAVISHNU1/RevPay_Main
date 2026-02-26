package com.revpay.service.impl;

import com.revpay.dto.response.PageResponse;
import com.revpay.entity.Notification;
import com.revpay.entity.User;
import com.revpay.repository.NotificationRepository;
import com.revpay.repository.UserRepository;
import com.revpay.service.interfaces.NotificationService;
import com.revpay.service.interfaces.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

	@Autowired
	private NotificationRepository notificationRepository;
	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepository;

	@Override
	public void notify(User user, String title, String message) {

		Notification n = new Notification();
		n.setUser(user);
		n.setTitle(title);
		n.setMessage(message);
		n.setIsRead(false);
		n.setCreatedAt(LocalDateTime.now());

		notificationRepository.save(n);
	}

	@Override
	public List<Notification> myNotifications() {
		return notificationRepository.findTop5ByUserOrderByCreatedAtDesc(userService.getCurrentUser());
	}

	@Override
	public PageResponse<?> myNotifications(int page, int size) {

		User user = userService.getCurrentUser();

		Pageable pageable = PageRequest.of(page, size);

		Page<Notification> notificationPage = notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable);

		return new PageResponse<>(notificationPage.getContent(), notificationPage.getNumber(),
				notificationPage.getTotalPages(), notificationPage.getTotalElements());
	}

	@Override
	public void markAllAsRead() {

		User currentUser = getCurrentUser();

		List<Notification> notifications = notificationRepository.findByUserAndIsReadFalse(currentUser);

		for (Notification n : notifications) {
			n.setIsRead(true); // ✅ correct setter
		}

		notificationRepository.saveAll(notifications);
	}

	private User getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String email = authentication.getName();

		return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
	}

	@Override
	public List<Notification> getUnreadNotifications() {
		User user = userService.getCurrentUser();
		return notificationRepository.findByUserAndIsReadFalse(user);
	}

	@Override
	public List<Notification> getAllNotifications() {
		User user = userService.getCurrentUser();
		return notificationRepository.findByUserOrderByCreatedAtDesc(user);
	}

	@Override
	public void deleteAllNotifications() {
		User user = userService.getCurrentUser();
		List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);
		notificationRepository.deleteAll(notifications);
	}

	@Override
	@Transactional
	public void clearAllNotifications() {
		User user = userService.getCurrentUser();
		notificationRepository.deleteByUser(user);
	}
}