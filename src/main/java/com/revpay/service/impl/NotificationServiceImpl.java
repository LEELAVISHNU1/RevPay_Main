package com.revpay.service.impl;

import com.revpay.entity.Notification;
import com.revpay.entity.User;
import com.revpay.repository.NotificationRepository;
import com.revpay.repository.UserRepository;
import com.revpay.service.interfaces.NotificationService;
import com.revpay.service.interfaces.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

	private static final Logger logger = LogManager.getLogger(NotificationServiceImpl.class);

	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepository;

	// Creates and saves a notification for the given user
	@Override
	public void notify(User user, String title, String message) {

		logger.info("Creating notification for user: {}", user.getEmail());

		Notification n = new Notification();
		n.setUser(user);
		n.setTitle(title);
		n.setMessage(message);
		n.setIsRead(false);
		n.setCreatedAt(LocalDateTime.now());

		notificationRepository.save(n);

		logger.info("Notification saved successfully for user: {}", user.getEmail());
	}

	// Marks all unread notifications as read for the current user
	@Override
	public void markAllAsRead() {

		User currentUser = getCurrentUser();

		logger.info("Marking all notifications as read for user: {}", currentUser.getEmail());

		List<Notification> notifications = notificationRepository.findByUserAndIsReadFalse(currentUser);

		for (Notification n : notifications) {
			n.setIsRead(true);
		}

		notificationRepository.saveAll(notifications);

		logger.info("{} notifications marked as read for user: {}", notifications.size(), currentUser.getEmail());
	}

	// Returns all notifications of the current user ordered by latest
	@Override
	public List<Notification> getAllNotifications() {

		User user = userService.getCurrentUser();

		logger.info("Fetching all notifications for user: {}", user.getEmail());

		return notificationRepository.findByUserOrderByCreatedAtDesc(user);
	}

	// Deletes all notifications belonging to the current user
	@Override
	@Transactional
	public void clearAllNotifications() {

		User user = userService.getCurrentUser();

		logger.warn("Clearing all notifications using deleteByUser for user: {}", user.getEmail());

		notificationRepository.deleteByUser(user);

		logger.info("All notifications cleared for user: {}", user.getEmail());
	}

	// Retrieves the currently authenticated user from security context
	private User getCurrentUser() {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		String email = authentication.getName();

		logger.debug("Fetching current user from security context: {}", email);

		return userRepository.findByEmail(email).orElseThrow(() -> {
			logger.error("User not found with email: {}", email);
			return new RuntimeException("User not found");
		});
	}
}