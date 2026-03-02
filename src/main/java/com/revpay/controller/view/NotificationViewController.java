package com.revpay.controller.view;

//import com.revpay.dto.response.PageResponse;
import com.revpay.entity.Notification;
import com.revpay.service.interfaces.NotificationService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller; // Use @Controller, NOT @RestController
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class NotificationViewController {

    // Service responsible for handling notification-related operations
    @Autowired
    private NotificationService notificationService;

    /**
     * Handles GET request to "/notifications"
     *
     * Function:
     * - Fetches ALL notifications of the currently logged-in user.
     * - Marks all unread notifications as read.
     * - Adds the notifications list to the model.
     * - Returns "notifications.html" view.
     */
    @GetMapping("/notifications")
    public String viewNotifications(Model model) {

        // Retrieve all notifications for the current user
        List<Notification> notifications =
                notificationService.getAllNotifications();

        // Mark all unread notifications as read
        notificationService.markAllAsRead();

        // Add notifications to model for rendering in UI
        model.addAttribute("notifications", notifications);

        return "notifications";
    }

    /**
     * Handles POST request to "/notifications/clear"
     *
     * Function:
     * - Deletes (clears) all notifications of the current user.
     * - Redirects back to notifications page.
     */
    @PostMapping("/notifications/clear")
    public String clearNotifications() {

        // Remove all notifications for the logged-in user
        notificationService.clearAllNotifications();

        return "redirect:/notifications";
    }
}