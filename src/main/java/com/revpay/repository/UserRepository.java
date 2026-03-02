package com.revpay.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.revpay.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);

	Optional<User> findByPhone(String phone);

	boolean existsByPhone(String phone);

	boolean existsByEmail(String email);

}
