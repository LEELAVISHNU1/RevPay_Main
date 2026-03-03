package com.revpay.service.impl;

import com.revpay.dto.response.PageResponse;
import com.revpay.entity.Notification;
import com.revpay.entity.User;
import com.revpay.repository.NotificationRepository;
import com.revpay.repository.UserRepository;
import com.revpay.service.interfaces.NotificationService;
import com.revpay.service.interfaces.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

/**
 * Implementation of NotificationService.
 *
 * Purpose:
 * - Create notifications
 * - Fetch notifications (latest, paginated, unread, all)
 * - Mark notifications as read
 * - Delete / clear notifications
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    // Logger to track notification activities
    private static final Logger logger =
            LogManager.getLogger(NotificationServiceImpl.class);

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    // ================= SEND NOTIFICATION =================

    /**
     * Creates and saves a new notification for a user.
     *
     * Used when:
     * - Loan approved
     * - EMI paid
     * - Invoice created
     * - Money sent/received
     */
    @Override
    public void notify(User user, String title, String message) {

        logger.info("Creating notification for user: {}", user.getEmail());

        Notification n = new Notification();
        n.setUser(user);
        n.setTitle(title);
        n.setMessage(message);
        n.setIsRead(false); // New notification is unread
        n.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(n);

        logger.info("Notification saved successfully for user: {}", user.getEmail());
    }

    // ================= MY LAST 5 =================

    /**
     * Returns last 5 notifications for current user.
     * Used in dashboard preview section.
     */
    @Override
    public List<Notification> myNotifications() {

        User user = userService.getCurrentUser();

        logger.info("Fetching top 5 notifications for user: {}", user.getEmail());

        return notificationRepository
                .findTop5ByUserOrderByCreatedAtDesc(user);
    }

    // ================= PAGINATED =================

    /**
     * Returns paginated notifications for current user.
     *
     * Used for:
     * - Full notifications page with pagination
     */
//    @Override
//    public PageResponse<?> myNotifications(int page, int size) {
//
//        User user = userService.getCurrentUser();
//
//        logger.info("Fetching paginated notifications for user: {} | page={} size={}",
//                user.getEmail(), page, size);
//
//        Pageable pageable = PageRequest.of(page, size);
//
//        Page<Notification> notificationPage =
//                notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable);
//
//        return new PageResponse<>(
//                notificationPage.getContent(),
//                notificationPage.getNumber(),
//                notificationPage.getTotalPages(),
//                notificationPage.getTotalElements()
//        );
//    }

    // ================= MARK ALL AS READ =================

    /**
     * Marks all unread notifications as read.
     *
     * Used when:
     * - User opens notifications page.
     */
    @Override
    public void markAllAsRead() {

        User currentUser = getCurrentUser();

        logger.info("Marking all notifications as read for user: {}",
                currentUser.getEmail());

        List<Notification> notifications =
                notificationRepository.findByUserAndIsReadFalse(currentUser);

        for (Notification n : notifications) {
            n.setIsRead(true);
        }

        notificationRepository.saveAll(notifications);

        logger.info("{} notifications marked as read for user: {}",
                notifications.size(), currentUser.getEmail());
    }

    // ================= GET UNREAD =================

    /**
     * Returns only unread notifications.
     */
//    @Override
//    public List<Notification> getUnreadNotifications() {
//
//        User user = userService.getCurrentUser();
//
//        logger.info("Fetching unread notifications for user: {}",
//                user.getEmail());
//
//        return notificationRepository.findByUserAndIsReadFalse(user);
//    }

    // ================= GET ALL =================

    /**
     * Returns all notifications for current user
     * ordered by latest first.
     */
    @Override
    public List<Notification> getAllNotifications() {

        User user = userService.getCurrentUser();

        logger.info("Fetching all notifications for user: {}",
                user.getEmail());

        return notificationRepository
                .findByUserOrderByCreatedAtDesc(user);
    }

    // ================= DELETE ALL =================

    /**
     * Deletes all notifications one by one.
     *
     * (Less efficient than deleteByUser)
     */
//    @Override
//    public void deleteAllNotifications() {
//
//        User user = userService.getCurrentUser();
//
//        logger.warn("Deleting all notifications for user: {}",
//                user.getEmail());
//
//        List<Notification> notifications =
//                notificationRepository.findByUserOrderByCreatedAtDesc(user);
//
//        notificationRepository.deleteAll(notifications);
//
//        logger.info("Deleted {} notifications for user: {}",
//                notifications.size(), user.getEmail());
//    }

    // ================= CLEAR ALL =================

    /**
     * Clears all notifications using repository method.
     *
     * More efficient bulk delete.
     */
    @Override
    @Transactional
    public void clearAllNotifications() {

        User user = userService.getCurrentUser();

        logger.warn("Clearing all notifications using deleteByUser for user: {}",
                user.getEmail());

        notificationRepository.deleteByUser(user);

        logger.info("All notifications cleared for user: {}",
                user.getEmail());
    }

    // ================= GET CURRENT USER =================

    /**
     * Retrieves currently authenticated user from Spring Security context.
     *
     * Used internally when UserService is not called.
     */
    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        logger.debug("Fetching current user from security context: {}", email);

        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("User not found with email: {}", email);
                    return new RuntimeException("User not found");
                });
    }
}