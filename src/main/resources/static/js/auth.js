/* =========================================================
   PASSWORD TOGGLE
========================================================= */

function togglePassword(inputId, iconId) {
    const input = document.getElementById(inputId);
    const icon = document.getElementById(iconId);

    if (!input || !icon) return;

    if (input.type === "password") {
        input.type = "text";
        icon.classList.replace("fa-eye", "fa-eye-slash");
    } else {
        input.type = "password";
        icon.classList.replace("fa-eye-slash", "fa-eye");
    }
}

/* =========================================================
   DOM READY
========================================================= */

document.addEventListener("DOMContentLoaded", function () {

    const registerForm = document.getElementById("registerForm");

    if (!registerForm) return;

    const inputs = registerForm.querySelectorAll("input, select");

    // Remove error while typing
    inputs.forEach(input => {
        input.addEventListener("input", () => {
            clearError(input);
        });
    });

    registerForm.addEventListener("submit", function (e) {

        let isValid = true;

        const fullName = registerForm.querySelector('input[name="fullName"]');
        const email = registerForm.querySelector('input[name="email"]');
        const phone = registerForm.querySelector('input[name="phone"]');
        const password = registerForm.querySelector('input[name="password"]');
        const pin = registerForm.querySelector('input[name="transactionPin"]');
        const favoriteColor = registerForm.querySelector('input[name="favoriteColor"]');
        const role = registerForm.querySelector('select[name="role"]');

        // FULL NAME
        if (!fullName.value.trim()) {
            showError(fullName, "Full Name is required");
            isValid = false;
        }

        // EMAIL
        if (!email.value.trim()) {
            showError(email, "Email is required");
            isValid = false;
        } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value)) {
            showError(email, "Invalid email format");
            isValid = false;
        }

        // PHONE
        if (!phone.value.trim()) {
            showError(phone, "Phone number is required");
            isValid = false;
        } else if (!/^\d{10}$/.test(phone.value)) {
            showError(phone, "Phone must be 10 digits");
            isValid = false;
        }

        // PASSWORD
        if (!password.value.trim()) {
            showError(password, "Password is required");
            isValid = false;
        } else if (password.value.length < 6) {
            showError(password, "Password must be at least 6 characters");
            isValid = false;
        }

        // PIN
        if (!pin.value.trim()) {
            showError(pin, "Transaction PIN is required");
            isValid = false;
        } else if (!/^\d{4}$/.test(pin.value)) {
            showError(pin, "PIN must be 4 digits");
            isValid = false;
        }

        // FAVORITE COLOR
        if (!favoriteColor.value.trim()) {
            showError(favoriteColor, "Favorite color is required");
            isValid = false;
        }

        // ROLE
        if (!role.value) {
            showError(role, "Please select account type");
            isValid = false;
        }

        if (!isValid) {
            e.preventDefault();
        }

    });

});

/* =========================================================
   ERROR HANDLING FUNCTIONS
========================================================= */

function showError(input, message) {

    input.classList.add("is-invalid");

    let errorDiv = input.closest(".form-group").querySelector(".error-message");

    if (errorDiv) {
        errorDiv.innerText = message;
        errorDiv.style.color = "red";
        errorDiv.style.fontSize = "13px";
        errorDiv.style.marginTop = "5px";
    }
}

function clearError(input) {

    input.classList.remove("is-invalid");

    let errorDiv = input.closest(".form-group").querySelector(".error-message");

    if (errorDiv) {
        errorDiv.innerText = "";
    }
}