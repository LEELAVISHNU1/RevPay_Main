package com.revpay.dto.request;

import jakarta.validation.constraints.*;

public class RegisterRequest {

	@NotBlank(message = "Full name is required")
	private String fullName;

	@NotBlank(message = "Email is required")
	@Email(message = "Enter valid email")
	private String email;

	@NotBlank(message = "Phone is required")
	@Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
	private String phone;

	@NotBlank(message = "Password is required")
	@Size(min = 6, message = "Password must be at least 6 characters")
	private String password;

	@NotBlank(message = "Role is required")
	private String role;

	@NotBlank(message = "Transaction PIN is required")
	@Pattern(regexp = "\\d{4}", message = "PIN must be exactly 4 digits")
	private String transactionPin;

	public String getTransactionPin() {
		return transactionPin;
	}

	public void setTransactionPin(String transactionPin) {
		this.transactionPin = transactionPin;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	private String favoriteColor;

	public String getFavoriteColor() {
		return favoriteColor;
	}

	public void setFavoriteColor(String favoriteColor) {
		this.favoriteColor = favoriteColor;
	}

}
