package com.revpay.controller.view;

import com.revpay.entity.Loan;
//import com.revpay.repository.LoanRepository;
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
//import java.nio.file.*;
//import java.io.IOException;

@Controller
public class LoanViewController {

    // Service responsible for handling loan-related business logic
    @Autowired
    private LoanService loanService;

    /**
     * Handles GET request to "/loan/apply"
     *
     * Function:
     * - Displays loan application page.
     * - Allows users to enter loan amount, tenure, and upload document.
     * - Returns "apply-loan.html" view.
     */
    @GetMapping("/loan/apply")
    public String applyPage() {
        return "apply-loan";
    }

    /**
     * Handles POST request to "/loan/apply"
     *
     * Function:
     * - Receives loan amount, tenure (months), and supporting document.
     * - Validates:
     *      1. Document must not be empty.
     *      2. File size must be less than 5MB.
     * - Calls service layer to process loan application.
     * - On success → shows success message.
     * - On failure → shows error message.
     * - Redirects to dashboard.
     */
    @PostMapping("/loan/apply")
    public String apply(@RequestParam Double amount,
                        @RequestParam Integer months,
                        @RequestParam("document") MultipartFile document,
                        RedirectAttributes ra) {

        try {
            // Validate uploaded document
            if (document.isEmpty())
                throw new RuntimeException("Please upload required document");

            if (document.getSize() > 5 * 1024 * 1024)
                throw new RuntimeException("File size must be less than 5MB");

            // Submit loan application
            loanService.applyLoan(amount, months, document);

            ra.addFlashAttribute("success", "Loan application submitted successfully");

        } catch (Exception e) {

            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/dashboard";
    }

    /**
     * Handles GET request to "/loan/admin"
     *
     * Function:
     * - Displays all loan applications.
     * - Typically accessed by ADMIN users.
     * - Adds loan list to model.
     * - Returns "approve-loans.html" view.
     */
    @GetMapping("/loan/admin")
    public String adminLoans(Model model) {
        model.addAttribute("loans", loanService.allLoans());
        return "approve-loans";
    }

    /**
     * Handles POST request to "/loan/approve/{id}"
     *
     * Function:
     * - Approves loan based on loan ID.
     * - Only ADMIN should access this.
     * - On success → shows success message.
     * - On failure → shows error message.
     * - Redirects back to admin loan page.
     */
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

    /**
     * Handles POST request to "/loan/repay/{id}"
     *
     * Function:
     * - Repays EMI for given loan ID.
     * - If cardId is provided → repay using card.
     * - Otherwise → repay using wallet balance.
     * - Shows success or error message.
     * - Redirects to dashboard.
     */
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

    /**
     * Handles POST request to "/loan/reject/{id}"
     *
     * Function:
     * - Rejects a loan application based on loan ID.
     * - Typically used by ADMIN.
     * - Redirects back to admin loan page with rejection status.
     */
    @PostMapping("/loan/reject/{id}")
    public String reject(@PathVariable Long id) {
        loanService.rejectLoan(id);
        return "redirect:/loan/admin?rejected";
    }

    /**
     * Handles GET request to "/loan/document/{id}"
     *
     * Function:
     * - Retrieves uploaded loan document by loan ID.
     * - Loads file from "uploads/loans" directory.
     * - Returns document as inline response (viewable in browser).
     * - Sets correct content type and file name.
     */
    @GetMapping("/loan/document/{id}")
    public ResponseEntity<Resource> viewDocument(@PathVariable Long id) throws IOException {

        // Find loan by ID
        Loan loan = loanService.allLoans().stream()
                .filter(l -> l.getLoanId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        // Build file path
        Path path = Paths.get("uploads/loans", loan.getDocumentPath());

        // Load file as resource
        Resource resource = new UrlResource(path.toUri());

        // Return file as inline response
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + loan.getDocumentName() + "\"")
                .contentType(MediaType.parseMediaType(loan.getDocumentType()))
                .body(resource);
    }
}