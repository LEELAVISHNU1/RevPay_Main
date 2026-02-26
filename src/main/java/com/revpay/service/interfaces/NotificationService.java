package com.revpay.service.interfaces;

import com.revpay.entity.User;
import com.revpay.dto.response.PageResponse;
import com.revpay.entity.Notification;
import java.util.List;

public interface NotificationService {

    void notify(User user, String title, String message);

    List<Notification> myNotifications();
    
    PageResponse<?> myNotifications(int page, int size);
    
    void markAllAsRead();
    
    List<Notification> getUnreadNotifications();
    
    List<Notification> getAllNotifications();
    void deleteAllNotifications();
    
    void clearAllNotifications() ;
    
}