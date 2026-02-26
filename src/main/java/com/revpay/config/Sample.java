package com.revpay.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class Sample {

	public static void main(String[] args) {
		System.out.println(new BCryptPasswordEncoder().encode("admin123"));
	}
}
