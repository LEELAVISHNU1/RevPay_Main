/* =========================================================
   CUSTOM MODAL SYSTEM
========================================================= */

const modalHTML = `
<div id="customModal" class="fixed inset-0 bg-gray-600 bg-opacity-50 hidden" style="z-index:1000;">
    <div class="relative top-24 mx-auto p-6 w-96 shadow-xl rounded-xl bg-white">
        <div class="text-center">
            <div id="modalIcon" class="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-yellow-100"></div>
            <h3 id="modalTitle" class="text-lg font-semibold text-gray-900 mt-3">Alert</h3>
            <p id="modalMessage" class="text-sm text-gray-500 mt-2"></p>
            <div class="mt-6">
                <button id="modalConfirmBtn" class="px-4 py-2 bg-red-600 text-white rounded-md hidden">Confirm</button>
                <button id="modalCancelBtn" class="px-4 py-2 bg-gray-500 text-white rounded-md ml-2 hidden">Cancel</button>
                <button id="modalOkBtn" class="px-4 py-2 bg-indigo-600 text-white rounded-md">OK</button>
            </div>
        </div>
    </div>
</div>
`;

document.body.insertAdjacentHTML("beforeend", modalHTML);

const modal = document.getElementById("customModal");
const modalTitle = document.getElementById("modalTitle");
const modalMessage = document.getElementById("modalMessage");
const modalIcon = document.getElementById("modalIcon");
const modalOkBtn = document.getElementById("modalOkBtn");
const modalConfirmBtn = document.getElementById("modalConfirmBtn");
const modalCancelBtn = document.getElementById("modalCancelBtn");

function showAlert(message, title = "Alert", type = "warning") {
    return new Promise((resolve) => {
        modalTitle.textContent = title;
        modalMessage.textContent = message;

        modalIcon.className = "mx-auto flex items-center justify-center h-12 w-12 rounded-full";

        if (type === "error") modalIcon.classList.add("bg-red-100");
        else if (type === "success") modalIcon.classList.add("bg-green-100");
        else modalIcon.classList.add("bg-yellow-100");

        modalOkBtn.classList.remove("hidden");
        modalConfirmBtn.classList.add("hidden");
        modalCancelBtn.classList.add("hidden");

        modal.classList.remove("hidden");

        modalOkBtn.onclick = () => {
            modal.classList.add("hidden");
            resolve(true);
        };
    });
}

function showConfirm(message, title = "Confirm") {
    return new Promise((resolve) => {
        modalTitle.textContent = title;
        modalMessage.textContent = message;

        modalIcon.className = "mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-yellow-100";

        modalOkBtn.classList.add("hidden");
        modalConfirmBtn.classList.remove("hidden");
        modalCancelBtn.classList.remove("hidden");

        modal.classList.remove("hidden");

        modalConfirmBtn.onclick = () => {
            modal.classList.add("hidden");
            resolve(true);
        };

        modalCancelBtn.onclick = () => {
            modal.classList.add("hidden");
            resolve(false);
        };
    });
}

/* =========================================================
   CARD VALIDATION ( REPEATED DIGITS)
========================================================= */

function isValidLuhn(cardNumber) {
    let sum = 0;
    let alternate = false;

    for (let i = cardNumber.length - 1;i >= 0;i--) {
        let n = parseInt(cardNumber[i], 10);
        if (alternate) {
            n *= 2;
            if (n > 9) n -= 9;
        }
        sum += n;
        alternate = !alternate;
    }

    return sum % 10 === 0;
}

function isRepeatedDigits(cardNumber) {
    return new Set(cardNumber).size === 1;
}

/* =========================================================
   DOM READY
========================================================= */

document.addEventListener("DOMContentLoaded", () => {

    const registerForm = document.getElementById("registerForm");

    if (registerForm) {
        registerForm.addEventListener("submit", function(e) {
            e.preventDefault();

            let isValid = true;

            const fields = registerForm.querySelectorAll("input, select");

            fields.forEach(field => {
                const error = field.nextElementSibling;
                if (error) {
                    error.textContent = "";
                    error.classList.add("hidden");
                }
            });

            function showError(input, message) {
                const error = input.nextElementSibling;
                error.textContent = message;
                error.classList.remove("hidden");
                input.classList.add("border-red-500");
                isValid = false;
            }

            const fullName = registerForm.querySelector('[name="fullName"]');
            const email = registerForm.querySelector('[name="email"]');
            const phone = registerForm.querySelector('[name="phone"]');
            const password = registerForm.querySelector('[name="password"]');
            const pin = registerForm.querySelector('[name="transactionPin"]');
            const favoriteColor = registerForm.querySelector('[name="favoriteColor"]');
            const role = registerForm.querySelector('[name="role"]');

            if (!fullName.value.trim())
                showError(fullName, "Full name is required");

            if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value))
                showError(email, "Enter valid email address");

            if (!/^\d{10}$/.test(phone.value))
                showError(phone, "Phone must be 10 digits");

            if (password.value.length < 6)
                showError(password, "Password must be at least 6 characters");

            if (!/^\d{4}$/.test(pin.value))
                showError(pin, "PIN must be exactly 4 digits");

            if (!favoriteColor.value.trim())
                showError(favoriteColor, "Favorite color is required");

            if (!role.value)
                showError(role, "Please select account type");

            if (isValid)
                registerForm.submit();
        });
    }

});

/* ================= ADD CARD ================= */

const addCardForm = document.getElementById("addCardForm");
if (addCardForm) {
    addCardForm.addEventListener("submit", async (e) => {
        e.preventDefault();
        const cardNum = addCardForm.querySelector('input[name="cardNumber"]').value.replace(/\s+/g, "");

        if (!/^\d{16}$/.test(cardNum)) {
            await showAlert("Card number must be 16 digits.", "Invalid Card", "error");
            return;
        }

        if (isRepeatedDigits(cardNum)) {
            await showAlert("Card cannot contain identical digits.", "Invalid Card", "error");
            return;
        }

        /* if (!isValidLuhn(cardNum)) {
             await showAlert("Invalid card number.", "Invalid Card", "error");
             return;
         }*/

        addCardForm.submit();
    });
}

/* ================= SEND MONEY ================= */

const sendMoneyForm = document.getElementById("sendMoneyForm");
if (sendMoneyForm) {
    sendMoneyForm.addEventListener("submit", async (e) => {
        e.preventDefault();
        const amount = parseFloat(sendMoneyForm.querySelector('input[name="amount"]').value);
        const email = sendMoneyForm.querySelector('input[name="email"]').value;

        if (isNaN(amount) || amount <= 0) {
            await showAlert("Enter valid amount.", "Invalid Amount", "error");
            return;
        }

        const confirmed = await showConfirm(`Send ₹${amount.toFixed(2)} to ${email}?`, "Confirm Transaction");
        if (confirmed) sendMoneyForm.submit();
    });
}

/* ================= LOAN VALIDATION ================= */

const loanForm = document.getElementById("loanApplyForm");
if (loanForm) {
    loanForm.addEventListener("submit", async (e) => {
        e.preventDefault();

        const amount = parseFloat(loanForm.querySelector('input[name="amount"]').value);
        const months = parseInt(loanForm.querySelector('input[name="months"]').value);

        if (amount < 1000) {
            await showAlert("Minimum loan amount is ₹1,000.", "Invalid Amount", "error");
            return;
        }

        if (months < 3 || months > 60) {
            await showAlert("Tenure must be between 3 and 60 months.", "Invalid Tenure", "error");
            return;
        }

        const confirmed = await showConfirm("Proceed with loan application?", "Confirm Loan");
        if (confirmed) loanForm.submit();
    });
}

/* ================= DELETE CARD ================= */

document.querySelectorAll(".delete-card-form").forEach(form => {
    form.addEventListener("submit", async (e) => {
        e.preventDefault();
        const confirmed = await showConfirm("Remove this card?", "Confirm Removal");
        if (confirmed) form.submit();
    });
});

/* ================= AUTO HIDE ALERTS ================= */

document.querySelectorAll(".alert").forEach(alert => {
    setTimeout(() => {
        alert.style.opacity = "0";
        setTimeout(() => alert.remove(), 500);
    }, 5000);
});



/* =========================================================
   PASSWORD TOGGLE
========================================================= */

function togglePassword(inputId, iconId) {
    const input = document.getElementById(inputId);
    const icon = document.getElementById(iconId);

    if (input.type === "password") {
        input.type = "text";
        icon.classList.replace("fa-eye", "fa-eye-slash");
    } else {
        input.type = "password";
        icon.classList.replace("fa-eye-slash", "fa-eye");
    }
}