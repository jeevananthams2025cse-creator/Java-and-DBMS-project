package com.bloodbank.controller;

import com.bloodbank.dao.BloodIssueDAO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/issues")
public class IssueController {

    private final BloodIssueDAO bloodIssueDAO = new BloodIssueDAO();
    private static final int PAGE_SIZE = 10;

    @GetMapping
    public String listIssues(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            Model model
    ) {
        if (page < 1) page = 1;

        int totalCount = bloodIssueDAO.countIssues();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalCount / PAGE_SIZE));
        if (page > totalPages) page = totalPages;

        List<BloodIssueDAO.IssueRecord> issues = bloodIssueDAO.getAllIssues(page, PAGE_SIZE);

        model.addAttribute("issues", issues);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("activeModule", "issues");

        return "issues/list";
    }
}
