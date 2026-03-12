package com.revpay.service.impl;

import com.revpay.entity.User;
import com.revpay.repository.UserRepository;
import com.revpay.security.SecurityUtil;
import com.revpay.service.interfaces.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository userRepository;

	// Returns the currently authenticated user from database
	@Override
	public User getCurrentUser() {

		String username = SecurityUtil.getCurrentUsername();

		return userRepository.findByEmail(username).orElseThrow(() -> new RuntimeException("User not found"));
	}
}