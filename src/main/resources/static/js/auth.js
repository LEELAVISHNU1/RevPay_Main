

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



document.addEventListener("DOMContentLoaded", function () {

    const form = document.getElementById("registerForm");
    if (!form) return;

    form.addEventListener("submit", function (e) {
        e.preventDefault();

        let isValid = true;

        const inputs = form.querySelectorAll("input, select");

        // Reset errors
        inputs.forEach(input => {
            input.classList.remove("is-invalid");
            const feedback = input.parentElement.querySelector(".invalid-feedback");
            if (feedback) feedback.textContent = "";
        });

        function setError(input, message) {
            input.classList.add("is-invalid");
            const feedback = input.parentElement.querySelector(".invalid-feedback");
            if (feedback) feedback.textContent = message;
            isValid = false;
        }

        const fullName = form.querySelector('[name="fullName"]');
        const email = form.querySelector('[name="email"]');
        const phone = form.querySelector('[name="phone"]');
        const password = form.querySelector('[name="password"]');
        const pin = form.querySelector('[name="transactionPin"]');
        const favoriteColor = form.querySelector('[name="favoriteColor"]');
        const role = form.querySelector('[name="role"]');

        if (!fullName.value.trim())
            setError(fullName, "Full name is required");

        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value))
            setError(email, "Enter valid email address");

        if (!/^\d{10}$/.test(phone.value.replace(/\D/g, "")))
            setError(phone, "Phone must be 10 digits");

        if (password.value.length < 6)
            setError(password, "Password must be at least 6 characters");

        if (!/^\d{4}$/.test(pin.value))
            setError(pin, "PIN must be exactly 4 digits");

        if (!favoriteColor.value.trim())
            setError(favoriteColor, "Favorite color is required");

        if (!role.value)
            setError(role, "Please select account type");

        if (isValid)
            form.submit();
    });
});


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