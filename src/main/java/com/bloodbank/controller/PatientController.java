package com.bloodbank.controller;

import com.bloodbank.dao.PatientDAO;
import com.bloodbank.dto.PatientForm;
import com.bloodbank.model.Patient;
import com.bloodbank.service.DashboardService;
import com.bloodbank.service.PatientService;
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
@RequestMapping("/patients")
public class PatientController {

    private final PatientService patientService;
    private static final int PAGE_SIZE = 10;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    public String listPatients(
            @RequestParam(value = "search", required = false, defaultValue = "") String search,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            Model model
    ) {
        if (page < 1) page = 1;

        int totalCount = patientService.countPatients(search);
        int totalPages = Math.max(1, (int) Math.ceil((double) totalCount / PAGE_SIZE));
        if (page > totalPages) page = totalPages;

        List<Patient> patients = patientService.getPatients(search, page, PAGE_SIZE);

        model.addAttribute("patients", patients);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("search", search);
        model.addAttribute("activeModule", "patients");

        return "patients/list";
    }

    @GetMapping("/new")
    public String newPatientForm(Model model) {
        if (!model.containsAttribute("patientForm")) {
            model.addAttribute("patientForm", new PatientForm());
        }
        model.addAttribute("bloodGroups", DashboardService.BLOOD_GROUPS);
        model.addAttribute("activeModule", "patients");
        return "patients/new";
    }

    @PostMapping
    public String registerPatient(
            @Valid @ModelAttribute("patientForm") PatientForm patientForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("bloodGroups", DashboardService.BLOOD_GROUPS);
            model.addAttribute("activeModule", "patients");
            return "patients/new";
        }

        try {
            boolean success = patientService.registerPatient(patientForm);
            if (success) {
                redirectAttributes.addFlashAttribute(
                        "successMessage",
                        "Patient '" + patientForm.getName() + "' (" + patientForm.getBloodGroup() + ") registered successfully."
                );
                return "redirect:/patients";
            } else {
                model.addAttribute("errorMessage", "Failed to register patient in database.");
                model.addAttribute("bloodGroups", DashboardService.BLOOD_GROUPS);
                model.addAttribute("activeModule", "patients");
                return "patients/new";
            }
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error registering patient: " + e.getMessage());
            model.addAttribute("bloodGroups", DashboardService.BLOOD_GROUPS);
            model.addAttribute("activeModule", "patients");
            return "patients/new";
        }
    }

    @GetMapping("/manage")
    public String managePatientLookup(
            @RequestParam("patientId") String rawId,
            @RequestParam(value = "action", defaultValue = "edit") String action,
            RedirectAttributes redirectAttributes
    ) {
        if (rawId == null || rawId.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please provide a valid Patient ID.");
            return "redirect:/dashboard";
        }

        try {
            int patientId = Integer.parseInt(rawId.replaceAll("[^0-9]", ""));
            Patient patient = patientService.getPatientById(patientId);
            if (patient == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Patient #P-" + patientId + " does not exist in registry.");
                return "redirect:/dashboard";
            }

            if ("delete".equalsIgnoreCase(action)) {
                return "redirect:/patients?search=" + patientId;
            }
            return "redirect:/patients/" + patientId + "/edit";
        } catch (NumberFormatException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid Patient ID format: " + rawId);
            return "redirect:/dashboard";
        }
    }

    @GetMapping("/{id}/edit")
    public String editPatientForm(
            @PathVariable("id") int id,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Patient patient = patientService.getPatientById(id);
        if (patient == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Patient #P-" + id + " not found.");
            return "redirect:/patients";
        }

        if (!model.containsAttribute("patientForm")) {
            PatientForm form = new PatientForm();
            form.setName(patient.getName());
            form.setAge(patient.getAge());
            form.setBloodGroup(patient.getBloodGroup());
            form.setHospital(patient.getHospital());
            form.setPhone(patient.getPhone());
            model.addAttribute("patientForm", form);
        }

        PatientDAO.PatientStats stats = patientService.getPatientStats(id);
        model.addAttribute("patientId", id);
        model.addAttribute("patient", patient);
        model.addAttribute("stats", stats);
        model.addAttribute("bloodGroups", DashboardService.BLOOD_GROUPS);
        model.addAttribute("activeModule", "patients");
        return "patients/edit";
    }

    @PostMapping("/{id}/edit")
    public String updatePatient(
            @PathVariable("id") int id,
            @Valid @ModelAttribute("patientForm") PatientForm patientForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            Patient patient = patientService.getPatientById(id);
            PatientDAO.PatientStats stats = patientService.getPatientStats(id);
            model.addAttribute("patientId", id);
            model.addAttribute("patient", patient);
            model.addAttribute("stats", stats);
            model.addAttribute("bloodGroups", DashboardService.BLOOD_GROUPS);
            model.addAttribute("activeModule", "patients");
            return "patients/edit";
        }

        try {
            boolean success = patientService.updatePatient(id, patientForm);
            if (success) {
                redirectAttributes.addFlashAttribute(
                        "successMessage",
                        "Patient #P-" + id + " ('" + patientForm.getName() + "') updated successfully. Changes cascaded to pending blood requests."
                );
                return "redirect:/patients";
            } else {
                model.addAttribute("errorMessage", "Failed to update patient record.");
                Patient patient = patientService.getPatientById(id);
                PatientDAO.PatientStats stats = patientService.getPatientStats(id);
                model.addAttribute("patientId", id);
                model.addAttribute("patient", patient);
                model.addAttribute("stats", stats);
                model.addAttribute("bloodGroups", DashboardService.BLOOD_GROUPS);
                model.addAttribute("activeModule", "patients");
                return "patients/edit";
            }
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            Patient patient = patientService.getPatientById(id);
            PatientDAO.PatientStats stats = patientService.getPatientStats(id);
            model.addAttribute("patientId", id);
            model.addAttribute("patient", patient);
            model.addAttribute("stats", stats);
            model.addAttribute("bloodGroups", DashboardService.BLOOD_GROUPS);
            model.addAttribute("activeModule", "patients");
            return "patients/edit";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error updating patient: " + e.getMessage());
            Patient patient = patientService.getPatientById(id);
            PatientDAO.PatientStats stats = patientService.getPatientStats(id);
            model.addAttribute("patientId", id);
            model.addAttribute("patient", patient);
            model.addAttribute("stats", stats);
            model.addAttribute("bloodGroups", DashboardService.BLOOD_GROUPS);
            model.addAttribute("activeModule", "patients");
            return "patients/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deletePatient(
            @PathVariable("id") int id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Patient patient = patientService.getPatientById(id);
            String name = (patient != null) ? patient.getName() : "#P-" + id;
            boolean success = patientService.deletePatient(id);
            if (success) {
                redirectAttributes.addFlashAttribute(
                        "successMessage",
                        "Patient '" + name + "' (#P-" + id + ") and associated requests/transfusion logs deleted successfully."
                );
            } else {
                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "Failed to delete patient #P-" + id + ". Record may not exist."
                );
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Error deleting patient #P-" + id + ": " + e.getMessage()
            );
        }
        return "redirect:/patients";
    }
}
