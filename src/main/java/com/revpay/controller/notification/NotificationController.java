package com.revpay.controller.notification;

import com.revpay.dto.response.ApiResponse;
import com.revpay.entity.Notification;
import com.revpay.service.interfaces.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    
//    @GetMapping
//    public ApiResponse<?> myNotifications() {
//        List<Notification> notifications = notificationService.myNotifications();
//        return new ApiResponse<>(true, "Notifications fetched successfully", notifications);
//    }
    
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ApiResponse<?> myNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        return new ApiResponse<>(
                true,
                "Notifications fetched",
                notificationService.myNotifications(page, size)
        );
    }
    
    @PostMapping("/notifications/clear")
    public String clearAllNotifications() {
        notificationService.deleteAllNotifications();
        return "redirect:/notifications";
    }
}