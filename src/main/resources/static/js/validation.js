document.addEventListener('DOMContentLoaded', () => {

    // === 1. WALLET ACTIONS ===
    const addMoneyForm = document.getElementById('addMoneyForm');
    if (addMoneyForm) {
        addMoneyForm.addEventListener('submit', (e) => {
            const amount = parseFloat(addMoneyForm.querySelector('input[name="amount"]').value);
            if (isNaN(amount) || amount <= 0) {
                e.preventDefault();
                alert("Please enter a valid amount greater than 0.");
            }
        });
    }

    // === 2. SEND MONEY ===
    const sendMoneyForm = document.getElementById('sendMoneyForm');
    if (sendMoneyForm) {
        sendMoneyForm.addEventListener('submit', (e) => {
            const amount = parseFloat(sendMoneyForm.querySelector('input[name="amount"]').value);
            const email = sendMoneyForm.querySelector('input[name="email"]').value;
            if (isNaN(amount) || amount <= 0) {
                e.preventDefault();
                alert("Please enter a valid amount.");
                return;
            }
            if (!confirm(`Confirm: Send ₹${amount.toFixed(2)} to ${email}?`)) e.preventDefault();
        });
    }

    // === 3. REQUEST MONEY ===
    const requestMoneyForm = document.getElementById('requestMoneyForm');
    if (requestMoneyForm) {
        requestMoneyForm.addEventListener('submit', (e) => {
            const amount = parseFloat(requestMoneyForm.querySelector('input[name="amount"]').value);
            if (isNaN(amount) || amount <= 0) {
                e.preventDefault();
                alert("Please enter a valid amount.");
            }
        });
    }

    // === 4. ADD NEW CARD ===
    const addCardForm = document.getElementById('addCardForm');
    if (addCardForm) {
        addCardForm.addEventListener('submit', (e) => {
            const cardNum = addCardForm.querySelector('input[name="cardNumber"]').value.replace(/\s+/g, '');
            if (!/^\d{16}$/.test(cardNum)) {
                e.preventDefault();
                alert("Please enter a 16-digit card number.");
            }
        });
    }

    // === 5. CARD MANAGEMENT ===
    document.querySelectorAll('.deposit-form').forEach(form => {
        form.addEventListener('submit', (e) => {
            const amount = parseFloat(form.querySelector('input[name="amount"]').value);
            if (amount <= 0) { e.preventDefault(); alert("Enter valid amount."); }
        });
    });

    document.querySelectorAll('.delete-card-form').forEach(form => {
        form.addEventListener('submit', (e) => {
            if (!confirm("Remove this card?")) e.preventDefault();
        });
    });

    // === 6. REQUESTS ACTIONS ===
    document.querySelectorAll('.accept-form').forEach(form => {
        form.addEventListener('submit', (e) => {
            if (!confirm("Pay this request now?")) e.preventDefault();
        });
    });

    // === 7. INVOICE PAYMENTS ===
    document.querySelectorAll('.invoice-pay-wallet').forEach(form => {
        form.addEventListener('submit', (e) => {
            if (!confirm("Pay invoice via Wallet?")) e.preventDefault();
        });
    });

    // === 8. SAFE AUTO-HIDE ALERTS ===
    const alerts = document.querySelectorAll('.alert-dismissible');
    alerts.forEach(alert => {
        setTimeout(() => {
            alert.style.transition = "all 0.6s ease";
            alert.style.opacity = "0";
            setTimeout(() => alert.style.display = "none", 600);
        }, 5000);
    });
	
	// === 9. RESET PASSWORD VALIDATION ===
	    const resetPasswordForm = document.getElementById('resetPasswordForm');
	    if (resetPasswordForm) {
	        resetPasswordForm.addEventListener('submit', (e) => {
	            const password = document.getElementById('newPassword').value;
	            const confirm = document.getElementById('confirmPassword').value;

	            if (password !== confirm) {
	                e.preventDefault();
	                alert("Passwords do not match. Please try again.");
	                document.getElementById('confirmPassword').focus();
	            }
	        });
	    }
		
		// === 10. LOAN APPLICATION VALIDATION ===
		    const loanForm = document.getElementById('loanApplyForm');
		    if (loanForm) {
		        loanForm.addEventListener('submit', (e) => {
		            const amount = parseFloat(loanForm.querySelector('input[name="amount"]').value);
		            const months = parseInt(loanForm.querySelector('input[name="months"]').value);

		            if (amount < 1000) {
		                e.preventDefault();
		                alert("Minimum loan amount is ₹1,000.");
		                return;
		            }

		            if (months < 3 || months > 60) {
		                e.preventDefault();
		                alert("Tenure must be between 3 and 60 months.");
		                return;
		            }

		            const confirmLoan = confirm(`Confirm Application: You are applying for a loan of ₹${amount.toLocaleString()} for ${months} months. Proceed?`);
		            if (!confirmLoan) e.preventDefault();
		        });
		    }
			
			// === 11. ADMIN LOAN ACTIONS ===
			    document.querySelectorAll('.admin-approve-form').forEach(form => {
			        form.addEventListener('submit', (e) => {
			            if (!confirm("CRITICAL: Are you sure you want to APPROVE this loan application? Funds will be scheduled for disbursement.")) {
			                e.preventDefault();
			            }
			        });
			    });

			    document.querySelectorAll('.admin-reject-form').forEach(form => {
			        form.addEventListener('submit', (e) => {
			            if (!confirm("Are you sure you want to REJECT this loan application? This cannot be undone.")) {
			                e.preventDefault();
			            }
			        });
			    });
				
				// === 12. CREATE INVOICE VALIDATION ===
				    const createInvoiceForm = document.getElementById('createInvoiceForm');
				    if (createInvoiceForm) {
				        createInvoiceForm.addEventListener('submit', (e) => {
				            const amount = parseFloat(createInvoiceForm.querySelector('input[name="amount"]').value);
				            const dueDateInput = createInvoiceForm.querySelector('input[name="dueDate"]');
				            const dueDate = new Date(dueDateInput.value);
				            const today = new Date();
				            today.setHours(0, 0, 0, 0);

				            if (amount <= 0) {
				                e.preventDefault();
				                alert("Please enter an amount greater than 0.");
				                return;
				            }

				            if (dueDate < today) {
				                e.preventDefault();
				                alert("Due date cannot be in the past.");
				                return;
				            }
				            
				            const confirmInvoice = confirm(`Confirm: Generate invoice for ₹${amount.toLocaleString()}?`);
				            if (!confirmInvoice) e.preventDefault();
				        });
				    }
});