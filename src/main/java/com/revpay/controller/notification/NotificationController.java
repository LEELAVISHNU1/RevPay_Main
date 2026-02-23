package com.revpay.controller.notification;

import com.revpay.dto.response.ApiResponse;
import com.revpay.entity.Notification;
import com.revpay.service.interfaces.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Notification>>> myNotifications() {

        List<Notification> notifications = notificationService.myNotifications();

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Notifications fetched successfully",
                        notifications
                )
        );
    }
}