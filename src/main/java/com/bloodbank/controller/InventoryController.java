package com.bloodbank.controller;

import com.bloodbank.service.DashboardService;
import com.bloodbank.service.InventoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public String viewInventory(Model model) {
        List<InventoryService.BloodGroupStockDetail> stockDetails = inventoryService.getInventoryStockBreakdown();

        int totalAvailable = 0;
        int totalExpiringSoon = 0;
        int totalExpired = 0;

        for (InventoryService.BloodGroupStockDetail item : stockDetails) {
            totalAvailable += item.availableUnits();
            totalExpiringSoon += item.expiringSoonUnits();
            totalExpired += item.expiredUnits();
        }

        model.addAttribute("stockDetails", stockDetails);
        model.addAttribute("totalAvailable", totalAvailable);
        model.addAttribute("totalExpiringSoon", totalExpiringSoon);
        model.addAttribute("totalExpired", totalExpired);
        model.addAttribute("activeModule", "inventory");

        return "inventory/index";
    }

    @GetMapping("/available")
    public String viewAvailableUnits(
            @RequestParam(value = "bloodGroup", required = false, defaultValue = "ALL") String bloodGroup,
            Model model
    ) {
        List<InventoryService.AvailableUnitView> units = inventoryService.getAvailableUnitsList();

        if (bloodGroup != null && !bloodGroup.isBlank() && !bloodGroup.equalsIgnoreCase("ALL")) {
            units = units.stream()
                    .filter(u -> u.bloodGroup().equalsIgnoreCase(bloodGroup.trim()))
                    .toList();
        }

        model.addAttribute("units", units);
        model.addAttribute("selectedBloodGroup", bloodGroup);
        model.addAttribute("bloodGroups", DashboardService.BLOOD_GROUPS);
        model.addAttribute("activeModule", "available");

        return "inventory/available";
    }
}
