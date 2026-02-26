package com.revpay.controller.view;

import com.revpay.service.interfaces.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RequestViewController {

	@Autowired
	private RequestService requestService;

	// open request page
	@GetMapping("/request")
	public String requestPage() {
		return "request-money";
	}

	// create request
	@PostMapping("/request")
	public String createRequest(@RequestParam String email, @RequestParam Double amount, @RequestParam String note,
			RedirectAttributes ra) {

		try {
			requestService.createRequest(email, amount, note);
			ra.addFlashAttribute("success", "Money request sent successfully");
		} catch (Exception e) {
			ra.addFlashAttribute("error", e.getMessage());
		}

		return "redirect:/dashboard";
	}

	// incoming requests page
	@GetMapping("/requests")
	public String incomingRequests(Model model) {
		model.addAttribute("requests", requestService.getIncomingRequests());
		return "incoming-requests";
	}

	// accept
	@PostMapping("/requests/accept/{id}")
	public String accept(@PathVariable Long id, RedirectAttributes ra) {
		try {
			requestService.acceptRequest(id);
			ra.addFlashAttribute("success", "Request accepted");
		} catch (Exception e) {
			ra.addFlashAttribute("error", e.getMessage());
		}
		return "redirect:/requests";
	}

	// reject
	@PostMapping("/requests/reject/{id}")
	public String reject(@PathVariable Long id, RedirectAttributes ra) {

		try {
			requestService.rejectRequest(id);
			ra.addFlashAttribute("success", "Request rejected");
		} catch (Exception e) {
			ra.addFlashAttribute("error", e.getMessage());
		}

		return "redirect:/requests";
	}
}