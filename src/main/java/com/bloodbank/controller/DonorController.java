package com.bloodbank.controller;

import com.bloodbank.dao.DonorDAO;
import com.bloodbank.dto.DonorForm;
import com.bloodbank.model.Donor;
import com.bloodbank.service.DashboardService;
import com.bloodbank.service.DonorService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/donors")
public class DonorController {

    private final DonorService donorService;
    private static final int PAGE_SIZE = 10;

    public DonorController(DonorService donorService) {
        this.donorService = donorService;
    }

    @GetMapping
    public String listDonors(
            @RequestParam(value = "search", required = false, defaultValue = "") String search,
            @RequestParam(value = "bloodGroup", required = false, defaultValue = "ALL") String bloodGroup,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            Model model
    ) {
        if (page < 1) page = 1;

        int totalCount = donorService.countDonors(search, bloodGroup);
        int totalPages = Math.max(1, (int) Math.ceil((double) totalCount / PAGE_SIZE));
        if (page > totalPages) page = totalPages;

        List<Donor> donors = donorService.getDonors(search, bloodGroup, page, PAGE_SIZE);

        model.addAttribute("donors", donors);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("search", search);
        model.addAttribute("selectedBloodGroup", bloodGroup);
        model.addAttribute("bloodGroups", DashboardService.BLOOD_GROUPS);
        model.addAttribute("activeModule", "donors");

        return "donors/list";
    }

    @GetMapping("/new")
    public String newDonorForm(Model model) {
        if (!model.containsAttribute("donorForm")) {
            model.addAttribute("donorForm", new DonorForm());
        }
        model.addAttribute("bloodGroups", DashboardService.BLOOD_GROUPS);
        model.addAttribute("activeModule", "donors");
        return "donors/new";
    }

    @PostMapping
    public String registerDonor(
            @Valid @ModelAttribute("donorForm") DonorForm donorForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("bloodGroups", DashboardService.BLOOD_GROUPS);
            model.addAttribute("activeModule", "donors");
            return "donors/new";
        }

        try {
            donorService.registerDonor(donorForm);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Donor '" + donorForm.getName() + "' (" + donorForm.getBloodGroup() + ") registered successfully."
            );
            return "redirect:/donors";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("bloodGroups", DashboardService.BLOOD_GROUPS);
            model.addAttribute("activeModule", "donors");
            return "donors/new";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to register donor: " + e.getMessage());
            model.addAttribute("bloodGroups", DashboardService.BLOOD_GROUPS);
            model.addAttribute("activeModule", "donors");
            return "donors/new";
        }
    }

    @GetMapping("/manage")
    public String manageDonorLookup(
            @RequestParam("donorId") String rawId,
            @RequestParam(value = "action", defaultValue = "edit") String action,
            RedirectAttributes redirectAttributes
    ) {
        if (rawId == null || rawId.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please provide a valid Donor ID.");
            return "redirect:/dashboard";
        }

        try {
            int donorId = Integer.parseInt(rawId.replaceAll("[^0-9]", ""));
            Donor donor = donorService.getDonorById(donorId);
            if (donor == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Donor #D-" + donorId + " does not exist in registry.");
                return "redirect:/dashboard";
            }

            if ("delete".equalsIgnoreCase(action)) {
                return "redirect:/donors?search=" + donorId;
            }
            return "redirect:/donors/" + donorId + "/edit";
        } catch (NumberFormatException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid Donor ID format: " + rawId);
            return "redirect:/dashboard";
        }
    }

    @GetMapping("/{id}/edit")
    public String editDonorForm(
            @PathVariable("id") int id,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Donor donor = donorService.getDonorById(id);
        if (donor == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Donor #D-" + id + " not found.");
            return "redirect:/donors";
        }

        if (!model.containsAttribute("donorForm")) {
            DonorForm form = new DonorForm();
            form.setName(donor.getName());
            form.setAge(donor.getAge());
            form.setBloodGroup(donor.getBloodGroup());
            form.setPhone(donor.getPhone());
            form.setLastDonationDate(donor.getLastDonationDate());
            model.addAttribute("donorForm", form);
        }

        DonorDAO.DonorStats stats = donorService.getDonorStats(id);
        model.addAttribute("donorId", id);
        model.addAttribute("donor", donor);
        model.addAttribute("stats", stats);
        model.addAttribute("bloodGroups", DashboardService.BLOOD_GROUPS);
        model.addAttribute("activeModule", "donors");
        return "donors/edit";
    }

    @PostMapping("/{id}/edit")
    public String updateDonor(
            @PathVariable("id") int id,
            @Valid @ModelAttribute("donorForm") DonorForm donorForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            Donor donor = donorService.getDonorById(id);
            DonorDAO.DonorStats stats = donorService.getDonorStats(id);
            model.addAttribute("donorId", id);
            model.addAttribute("donor", donor);
            model.addAttribute("stats", stats);
            model.addAttribute("bloodGroups", DashboardService.BLOOD_GROUPS);
            model.addAttribute("activeModule", "donors");
            return "donors/edit";
        }

        try {
            boolean success = donorService.updateDonor(id, donorForm);
            if (success) {
                redirectAttributes.addFlashAttribute(
                        "successMessage",
                        "Donor #D-" + id + " ('" + donorForm.getName() + "') updated successfully. Changes cascaded to all unissued available units in inventory."
                );
                return "redirect:/donors";
            } else {
                model.addAttribute("errorMessage", "Failed to update donor record.");
                Donor donor = donorService.getDonorById(id);
                DonorDAO.DonorStats stats = donorService.getDonorStats(id);
                model.addAttribute("donorId", id);
                model.addAttribute("donor", donor);
                model.addAttribute("stats", stats);
                model.addAttribute("bloodGroups", DashboardService.BLOOD_GROUPS);
                model.addAttribute("activeModule", "donors");
                return "donors/edit";
            }
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            Donor donor = donorService.getDonorById(id);
            DonorDAO.DonorStats stats = donorService.getDonorStats(id);
            model.addAttribute("donorId", id);
            model.addAttribute("donor", donor);
            model.addAttribute("stats", stats);
            model.addAttribute("bloodGroups", DashboardService.BLOOD_GROUPS);
            model.addAttribute("activeModule", "donors");
            return "donors/edit";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error updating donor: " + e.getMessage());
            Donor donor = donorService.getDonorById(id);
            DonorDAO.DonorStats stats = donorService.getDonorStats(id);
            model.addAttribute("donorId", id);
            model.addAttribute("donor", donor);
            model.addAttribute("stats", stats);
            model.addAttribute("bloodGroups", DashboardService.BLOOD_GROUPS);
            model.addAttribute("activeModule", "donors");
            return "donors/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteDonor(
            @PathVariable("id") int id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Donor donor = donorService.getDonorById(id);
            String name = (donor != null) ? donor.getName() : "#D-" + id;
            boolean success = donorService.deleteDonor(id);
            if (success) {
                redirectAttributes.addFlashAttribute(
                        "successMessage",
                        "Donor '" + name + "' (#D-" + id + ") and associated unissued units/donations deleted successfully."
                );
            } else {
                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "Failed to delete donor #D-" + id + ". Record may not exist."
                );
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Error deleting donor #D-" + id + ": " + e.getMessage()
            );
        }
        return "redirect:/donors";
    }
}
