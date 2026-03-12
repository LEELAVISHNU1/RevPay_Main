package com.revpay.controller.view;

import com.revpay.entity.Notification;
import com.revpay.service.interfaces.NotificationService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class NotificationViewController {

    @Autowired
    private NotificationService notificationService;

    // Shows all user notifications and marks unread ones as read
    @GetMapping("/notifications")
    public String viewNotifications(Model model) {

        List<Notification> notifications =
                notificationService.getAllNotifications();

        notificationService.markAllAsRead();

        model.addAttribute("notifications", notifications);

        return "notifications";
    }

    // Clears all notifications for the current user
    @PostMapping("/notifications/clear")
    public String clearNotifications() {

        notificationService.clearAllNotifications();

        return "redirect:/notifications";
    }
}