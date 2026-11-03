package com.bloodbank.controller;


import com.bloodbank.dao.BloodRequestDAO;
import com.bloodbank.dto.BloodRequestForm;
import com.bloodbank.model.BloodRequest;
import com.bloodbank.model.BloodUnit;
import com.bloodbank.model.Patient;
import com.bloodbank.service.DashboardService;
import com.bloodbank.service.InventoryService;
import com.bloodbank.service.PatientService;
import com.bloodbank.service.RequestService;
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

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/requests")
public class RequestController {

    private final BloodRequestDAO bloodRequestDAO = new BloodRequestDAO();
    private final RequestService requestService = new RequestService();
    private final InventoryService inventoryService;
    private final PatientService patientService;
    private static final int PAGE_SIZE = 10;

    public RequestController(InventoryService inventoryService, PatientService patientService) {
        this.inventoryService = inventoryService;
        this.patientService = patientService;
    }

    @GetMapping
    public String listRequests(
            @RequestParam(value = "tab", required = false, defaultValue = "ALL") String tab,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            Model model
    ) {
        if (page < 1) page = 1;

        int totalCount = bloodRequestDAO.countRequestViews(tab);
        int totalPages = Math.max(1, (int) Math.ceil((double) totalCount / PAGE_SIZE));
        if (page > totalPages) page = totalPages;

        List<BloodRequestDAO.BloodRequestView> requests = bloodRequestDAO.getRequestViews(tab, page, PAGE_SIZE);

        model.addAttribute("requests", requests);
        model.addAttribute("activeTab", tab.toUpperCase());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("allCount", bloodRequestDAO.countRequestViews("ALL"));
        model.addAttribute("pendingCount", bloodRequestDAO.countRequestViews("PENDING"));
        model.addAttribute("fulfilledCount", bloodRequestDAO.countRequestViews("FULFILLED"));
        model.addAttribute("emergencyCount", bloodRequestDAO.countRequestViews("EMERGENCY"));
        model.addAttribute("activeModule", "requests");

        return "requests/list";
    }

    @GetMapping("/new")
    public String newRequestForm(
            @RequestParam(value = "patientId", required = false) Integer patientId,
            Model model
    ) {
        if (!model.containsAttribute("requestForm")) {
            BloodRequestForm form = new BloodRequestForm();
            if (patientId != null) {
                form.setPatientId(patientId);
                Patient patient = patientService.getPatientById(patientId);
                if (patient != null) {
                    form.setBloodGroup(patient.getBloodGroup());
                }
            }
            model.addAttribute("requestForm", form);
        }

        model.addAttribute("patients", patientService.getAllPatients());
        model.addAttribute("bloodGroups", DashboardService.BLOOD_GROUPS);
        model.addAttribute("activeModule", "requests");
        return "requests/new";
    }

    @PostMapping
    public String registerRequest(
            @Valid @ModelAttribute("requestForm") BloodRequestForm requestForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("patients", patientService.getAllPatients());
            model.addAttribute("bloodGroups", DashboardService.BLOOD_GROUPS);
            model.addAttribute("activeModule", "requests");
            return "requests/new";
        }

        BloodRequest request = new BloodRequest(
                0,
                requestForm.getPatientId(),
                requestForm.getBloodGroup(),
                requestForm.getProductType(),
                requestForm.getUnitsRequired(),
                requestForm.getPriority(),
                LocalDateTime.now(),
                "PENDING"
        );

        boolean success = bloodRequestDAO.addRequest(request);

        if (success) {
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Blood request registered successfully! Priority: " + requestForm.getPriority()
            );
            return "redirect:/requests";
        } else {
            model.addAttribute("errorMessage", "Failed to register request in database.");
            model.addAttribute("patients", patientService.getAllPatients());
            model.addAttribute("bloodGroups", DashboardService.BLOOD_GROUPS);
            model.addAttribute("activeModule", "requests");
            return "requests/new";
        }
    }

    @GetMapping("/{id}/fulfil")
    public String fulfilPage(@PathVariable("id") int requestId, Model model, RedirectAttributes redirectAttributes) {
        BloodRequest request = bloodRequestDAO.getRequestById(requestId);

        if (request == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Request #" + requestId + " was not found.");
            return "redirect:/requests";
        }

        Patient patient = patientService.getPatientById(request.getPatientId());
        List<BloodUnit> compatibleUnits = inventoryService.findCompatibleWholeBloodUnits(request.getBloodGroup());

        boolean canFulfil = compatibleUnits.size() >= request.getUnitsRequired();

        model.addAttribute("request", request);
        model.addAttribute("patient", patient);
        model.addAttribute("compatibleUnits", compatibleUnits);
        model.addAttribute("canFulfil", canFulfil);
        model.addAttribute("activeModule", "requests");

        return "requests/fulfil";
    }

    @PostMapping("/{id}/fulfil")
    public String processFulfil(@PathVariable("id") int requestId, RedirectAttributes redirectAttributes) {
        RequestService.FulfilmentResult result = requestService.processFulfilment(requestId);

        if (result.success()) {
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Request #" + requestId + " fulfilled successfully! " + result.unitsFulfilled() + " unit(s) marked ISSUED."
            );
            return "redirect:/requests";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Fulfilment failed: " + result.message());
            return "redirect:/requests/" + requestId + "/fulfil";
        }
    }
}
