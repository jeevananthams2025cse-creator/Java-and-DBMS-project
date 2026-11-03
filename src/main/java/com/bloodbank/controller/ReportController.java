package com.bloodbank.controller;

import com.bloodbank.dao.ReportDAO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ReportDAO reportDAO = new ReportDAO();

    @GetMapping("/inventory")
    public String inventoryReport(Model model) {
        List<ReportDAO.InventoryReportRow> rows = reportDAO.getInventoryReportData();

        int totalAvailable = 0;
        int totalIssued = 0;
        int totalExpired = 0;
        int grandTotal = 0;

        for (ReportDAO.InventoryReportRow r : rows) {
            totalAvailable += r.availableUnits();
            totalIssued += r.issuedUnits();
            totalExpired += r.expiredUnits();
            grandTotal += r.totalUnits();
        }

        model.addAttribute("reportRows", rows);
        model.addAttribute("totalAvailable", totalAvailable);
        model.addAttribute("totalIssued", totalIssued);
        model.addAttribute("totalExpired", totalExpired);
        model.addAttribute("grandTotal", grandTotal);
        model.addAttribute("activeModule", "reports-inventory");

        return "reports/inventory";
    }

    @GetMapping("/inventory/csv")
    public void exportInventoryCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"inventory_report_" + LocalDate.now() + ".csv\"");

        List<ReportDAO.InventoryReportRow> rows = reportDAO.getInventoryReportData();

        try (PrintWriter writer = response.getWriter()) {
            writer.println("Blood Group,Available Units,Issued Units,Expired Units,Total Units");
            for (ReportDAO.InventoryReportRow r : rows) {
                writer.printf("%s,%d,%d,%d,%d%n",
                        r.bloodGroup(),
                        r.availableUnits(),
                        r.issuedUnits(),
                        r.expiredUnits(),
                        r.totalUnits()
                );
            }
        }
    }

    @GetMapping("/emergency")
    public String emergencyReport(Model model) {
        List<ReportDAO.EmergencyReportRow> rows = reportDAO.getEmergencyRequestReportData();

        int totalPending = rows.size();
        int emergencyCount = 0;
        int sufficientCount = 0;

        for (ReportDAO.EmergencyReportRow r : rows) {
            if ("EMERGENCY".equalsIgnoreCase(r.priority())) {
                emergencyCount++;
            }
            if (r.isSufficient()) {
                sufficientCount++;
            }
        }

        model.addAttribute("reportRows", rows);
        model.addAttribute("totalPending", totalPending);
        model.addAttribute("emergencyCount", emergencyCount);
        model.addAttribute("sufficientCount", sufficientCount);
        model.addAttribute("activeModule", "reports-emergency");

        return "reports/emergency";
    }

    @GetMapping("/emergency/csv")
    public void exportEmergencyCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"emergency_request_report_" + LocalDate.now() + ".csv\"");

        List<ReportDAO.EmergencyReportRow> rows = reportDAO.getEmergencyRequestReportData();

        try (PrintWriter writer = response.getWriter()) {
            writer.println("Request ID,Patient Name,Blood Group,Units Required,Priority,Status,Compatible Available Units,Stock Check");
            for (ReportDAO.EmergencyReportRow r : rows) {
                writer.printf("%d,\"%s\",%s,%d,%s,%s,%d,%s%n",
                        r.requestId(),
                        r.patientName().replace("\"", "\"\""),
                        r.bloodGroup(),
                        r.unitsRequired(),
                        r.priority(),
                        r.status(),
                        r.availableUnits(),
                        r.isSufficient() ? "SUFFICIENT" : "DEFICIT"
                );
            }
        }
    }
}
