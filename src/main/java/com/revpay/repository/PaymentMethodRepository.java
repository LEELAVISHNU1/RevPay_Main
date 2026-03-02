package com.revpay.repository;

import com.revpay.entity.PaymentMethod;
import com.revpay.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

	List<PaymentMethod> findByUser(User user);
}
