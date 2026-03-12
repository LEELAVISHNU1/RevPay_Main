package com.revpay.controller.view;

import com.revpay.entity.Loan;
import com.revpay.service.interfaces.LoanService;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.*;
import org.springframework.core.io.*;

@Controller
public class LoanViewController {

    @Autowired
    private LoanService loanService;

    // Opens the loan application page
    @GetMapping("/loan/apply")
    public String applyPage() {
        return "apply-loan";
    }

    // Submits a loan request after validating the uploaded document
    @PostMapping("/loan/apply")
    public String apply(@RequestParam Double amount,
                        @RequestParam Integer months,
                        @RequestParam("document") MultipartFile document,
                        RedirectAttributes ra) {

        try {
            if (document.isEmpty())
                throw new RuntimeException("Please upload required document");

            if (document.getSize() > 5 * 1024 * 1024)
                throw new RuntimeException("File size must be less than 5MB");

            loanService.applyLoan(amount, months, document);

            ra.addFlashAttribute("success", "Loan application submitted successfully");

        } catch (Exception e) {

            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/dashboard";
    }

    // Shows all loan applications for admin review
    @GetMapping("/loan/admin")
    public String adminLoans(Model model) {
        model.addAttribute("loans", loanService.allLoans());
        return "approve-loans";
    }

    // Approves a loan based on the given loan ID
    @PostMapping("/loan/approve/{id}")
    public String approve(@PathVariable Long id, RedirectAttributes ra) {

        try {
            loanService.approveLoan(id);
            ra.addFlashAttribute("success", "Loan approved successfully");

        } catch (Exception e) {

            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/loan/admin";
    }

    // Repays EMI using card if provided, otherwise uses wallet
    @PostMapping("/loan/repay/{id}")
    public String repay(@PathVariable Long id,
                        @RequestParam(required = false) Long cardId,
                        RedirectAttributes ra) {

        try {
            if (cardId != null && !cardId.toString().isEmpty())
                loanService.repayEmiUsingCard(id, cardId);
            else
                loanService.repayEmi(id);

            ra.addFlashAttribute("success", "EMI Paid Successfully");

        } catch (Exception e) {

            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/dashboard";
    }

    // Rejects the loan application and redirects to admin page
    @PostMapping("/loan/reject/{id}")
    public String reject(@PathVariable Long id) {
        loanService.rejectLoan(id);
        return "redirect:/loan/admin?rejected";
    }

    // Loads and displays the uploaded loan document in the browser
    @GetMapping("/loan/document/{id}")
    public ResponseEntity<Resource> viewDocument(@PathVariable Long id) throws IOException {

        Loan loan = loanService.allLoans().stream()
                .filter(l -> l.getLoanId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        Path path = Paths.get("uploads/loans", loan.getDocumentPath());

        Resource resource = new UrlResource(path.toUri());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + loan.getDocumentName() + "\"")
                .contentType(MediaType.parseMediaType(loan.getDocumentType()))
                .body(resource);
    }
}