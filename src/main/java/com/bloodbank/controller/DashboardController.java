package com.bloodbank.controller;

import com.bloodbank.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        DashboardService.DashboardKPIs kpis = dashboardService.getDashboardData();
        model.addAttribute("kpis", kpis);
        model.addAttribute("activeModule", "dashboard");
        return "dashboard";
    }
}
