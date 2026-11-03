package com.bloodbank.service;

import com.bloodbank.dao.BloodRequestDAO;
import com.bloodbank.dao.BloodUnitDAO;
import com.bloodbank.dao.DonorDAO;
import com.bloodbank.dao.PatientDAO;
import com.bloodbank.model.BloodRequest;
import com.bloodbank.model.BloodUnit;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final DonorDAO donorDAO = new DonorDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final BloodUnitDAO bloodUnitDAO = new BloodUnitDAO();
    private final BloodRequestDAO bloodRequestDAO = new BloodRequestDAO();

    public static final String[] BLOOD_GROUPS = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};

    public record DashboardKPIs(
            int totalDonors,
            int totalPatients,
            int availableUnits,
            int pendingRequests,
            int emergencyPendingRequests,
            Map<String, Integer> stockByBloodGroup
    ) {}

    public DashboardKPIs getDashboardData() {
        int totalDonors = donorDAO.getAllDonors().size();
        int totalPatients = patientDAO.getAllPatients().size();

        List<BloodUnit> availableUnitsList = bloodUnitDAO.getAvailableUnits();
        int availableUnitsCount = availableUnitsList.size();

        List<BloodRequest> allRequests = bloodRequestDAO.getAllRequests();
        int pendingRequestsCount = 0;
        int emergencyPendingCount = 0;

        for (BloodRequest req : allRequests) {
            if ("PENDING".equalsIgnoreCase(req.getStatus())) {
                pendingRequestsCount++;
                if ("EMERGENCY".equalsIgnoreCase(req.getPriority())) {
                    emergencyPendingCount++;
                }
            }
        }

        Map<String, Integer> stockMap = new LinkedHashMap<>();
        for (String bg : BLOOD_GROUPS) {
            stockMap.put(bg, 0);
        }

        for (BloodUnit u : availableUnitsList) {
            String bg = u.getBloodGroup();
            if (bg != null) {
                stockMap.put(bg, stockMap.getOrDefault(bg, 0) + 1);
            }
        }

        return new DashboardKPIs(
                totalDonors,
                totalPatients,
                availableUnitsCount,
                pendingRequestsCount,
                emergencyPendingCount,
                stockMap
        );
    }
}
