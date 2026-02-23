package com.revpay.controller.loan;

import com.revpay.dto.response.ApiResponse;
import com.revpay.entity.Loan;
import com.revpay.service.interfaces.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    @Autowired
    private LoanService loanService;

    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<Void>> apply(@RequestBody Map<String, String> body) {

        loanService.applyLoan(
                Double.valueOf(body.get("amount")),
                Integer.parseInt(body.get("months"))
        );

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Loan applied successfully", null)
        );
    }

    @PostMapping("/approve/{id}")
    public ResponseEntity<ApiResponse<Void>> approve(@PathVariable Long id) {

        loanService.approveLoan(id);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Loan approved & credited", null)
        );
    }

    @PostMapping("/repay/{id}")
    public ResponseEntity<ApiResponse<Void>> repay(@PathVariable Long id) {

        loanService.repayEmi(id);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "EMI paid successfully", null)
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Loan>>> myLoans() {

        List<Loan> loans = loanService.myLoans();

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Loans fetched successfully", loans)
        );
    }
}