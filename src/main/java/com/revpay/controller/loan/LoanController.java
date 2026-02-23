package com.revpay.controller.loan;

import com.revpay.dto.response.ApiResponse;
import com.revpay.entity.Loan;
import com.revpay.service.interfaces.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    @Autowired
    private LoanService loanService;

    // PERSONAL can apply loan
    @PreAuthorize("hasRole('PERSONAL')")
    @PostMapping("/apply")
    public ApiResponse<?> apply(@RequestBody Map<String,String> body) {
        loanService.applyLoan(
                Double.valueOf(body.get("amount")),
                Integer.parseInt(body.get("months"))
        );
        return new ApiResponse<>(true, "Loan applied", null);
    }

    // ADMIN approves
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/approve/{id}")
    public ApiResponse<?> approve(@PathVariable Long id) {
        loanService.approveLoan(id);
        return new ApiResponse<>(true, "Loan approved", null);
    }

    // PERSONAL repays EMI
    @PreAuthorize("hasRole('PERSONAL')")
    @PostMapping("/repay/{id}")
    public ApiResponse<?> repay(@PathVariable Long id) {
        loanService.repayEmi(id);
        return new ApiResponse<>(true, "EMI paid", null);
    }

    // Any logged in user
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ApiResponse<?> myLoans() {
        List<Loan> loans = loanService.myLoans();
        return new ApiResponse<>(true, "Loans fetched successfully", loans);
    }
}