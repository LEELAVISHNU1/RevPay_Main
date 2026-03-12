package com.revpay.repository;

import com.revpay.entity.Notification;
import com.revpay.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

	List<Notification> findByUserOrderByCreatedAtDesc(User user);

	List<Notification> findByUserAndIsReadFalse(User user);

	void deleteByUser(User user);
}