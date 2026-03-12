package com.revpay.service.interfaces;

import com.revpay.entity.User;
import com.revpay.entity.Notification;
import java.util.List;

public interface NotificationService {

    void notify(User user, String title, String message);
    
    void markAllAsRead();
    
    List<Notification> getAllNotifications();
    void clearAllNotifications() ;
    
}