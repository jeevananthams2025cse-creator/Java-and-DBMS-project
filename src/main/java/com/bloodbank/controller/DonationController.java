package com.bloodbank.controller;

import com.bloodbank.dao.DonationDAO;
import com.bloodbank.dto.DonationForm;
import com.bloodbank.model.Donation;
import com.bloodbank.service.DashboardService;
import com.bloodbank.service.DonationService;
import com.bloodbank.service.DonorService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/donations")
public class DonationController {

    private final DonationService donationService;
    private final DonorService donorService;
    private static final int PAGE_SIZE = 10;

    public DonationController(DonationService donationService, DonorService donorService) {
        this.donationService = donationService;
        this.donorService = donorService;
    }

    @GetMapping
    public String listDonations(
            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(value = "bloodGroup", required = false, defaultValue = "ALL") String bloodGroup,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            Model model
    ) {
        if (page < 1) page = 1;

        int totalCount = donationService.countDonations(fromDate, toDate, bloodGroup);
        int totalPages = Math.max(1, (int) Math.ceil((double) totalCount / PAGE_SIZE));
        if (page > totalPages) page = totalPages;

        List<DonationDAO.DonationRecord> donations = donationService.getDonations(fromDate, toDate, bloodGroup, page, PAGE_SIZE);

        model.addAttribute("donations", donations);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("selectedBloodGroup", bloodGroup);
        model.addAttribute("bloodGroups", DashboardService.BLOOD_GROUPS);
        model.addAttribute("activeModule", "donations");

        return "donations/list";
    }

    @GetMapping("/new")
    public String newDonationForm(
            @RequestParam(value = "donorId", required = false) Integer donorId,
            Model model
    ) {
        if (!model.containsAttribute("donationForm")) {
            DonationForm form = new DonationForm();
            if (donorId != null) {
                form.setDonorId(donorId);
            }
            model.addAttribute("donationForm", form);
        }

        model.addAttribute("donors", donorService.getAllDonors());
        model.addAttribute("activeModule", "donations");
        return "donations/new";
    }

    @PostMapping
    public String recordDonation(
            @Valid @ModelAttribute("donationForm") DonationForm donationForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("donors", donorService.getAllDonors());
            model.addAttribute("activeModule", "donations");
            return "donations/new";
        }

        try {
            Donation donation = new Donation(
                    0,
                    donationForm.getDonorId(),
                    donationForm.getDonationDate(),
                    donationForm.getProductType(),
                    donationForm.getQuantity()
            );

            donationService.recordDonation(donation);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Blood donation recorded successfully! Unit added to inventory (eligible shelf life: 31 days)."
            );
            return "redirect:/donations";

        } catch (IllegalStateException e) {
            // 90-day gap eligibility violation
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("donors", donorService.getAllDonors());
            model.addAttribute("activeModule", "donations");
            return "donations/new";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to record donation: " + e.getMessage());
            model.addAttribute("donors", donorService.getAllDonors());
            model.addAttribute("activeModule", "donations");
            return "donations/new";
        }
    }
}
