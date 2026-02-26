document.addEventListener('DOMContentLoaded', function() {
    // Select all forms that repay loans
    const repayForms = document.querySelectorAll('form[action*="/loan/repay/"]');
    
    repayForms.forEach(form => {
        form.addEventListener('submit', function(e) {
            const confirmed = confirm("Are you sure you want to process this payment?");
            if (!confirmed) {
                e.preventDefault();
            }
        });
    });
});