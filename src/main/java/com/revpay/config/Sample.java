package com.revpay.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class Sample {

	public static void main(String[] args) {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
	    System.out.println("Password: " + encoder.encode("admin123"));
	    System.out.println("PIN: " + encoder.encode("1234"));
	}
}
