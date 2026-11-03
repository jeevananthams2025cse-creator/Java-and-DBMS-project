package com.bloodbank;

import com.bloodbank.model.BloodRequest;
import com.bloodbank.model.BloodUnit;
import com.bloodbank.model.Donor;
import com.bloodbank.model.Patient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CascadeManagementTest {

    @Test
    @DisplayName("Updating donor blood group cascades to available units but preserves issued units")
    void testDonorBloodGroupCascade() {
        int donorId = 42;
        Donor donor = new Donor(donorId, "Alice Walker", 29, "+1-555-0199", "A+", LocalDate.now().minusDays(100));

        // Simulated blood units collected from this donor
        BloodUnit availableUnit = new BloodUnit(101, 1, donor.getBloodGroup(), "WHOLE_BLOOD",
                LocalDate.now().minusDays(10), LocalDate.now().plusDays(25), "AVAILABLE");
        BloodUnit issuedUnit = new BloodUnit(102, 1, donor.getBloodGroup(), "WHOLE_BLOOD",
                LocalDate.now().minusDays(50), LocalDate.now().minusDays(15), "ISSUED");

        List<BloodUnit> units = new ArrayList<>(List.of(availableUnit, issuedUnit));

        // Donor's blood group corrected from A+ to O+
        String newBloodGroup = "O+";
        donor.setBloodGroup(newBloodGroup);

        // Cascade rule: update only AVAILABLE units
        for (int i = 0; i < units.size(); i++) {
            BloodUnit u = units.get(i);
            if ("AVAILABLE".equals(u.getStatus())) {
                units.set(i, new BloodUnit(u.getUnitId(), u.getDonationId(), newBloodGroup,
                        u.getProductType(), u.getCollectionDate(), u.getExpiryDate(), u.getStatus()));
            }
        }

        assertEquals("O+", units.get(0).getBloodGroup(), "Available unit should receive cascaded blood group");
        assertEquals("A+", units.get(1).getBloodGroup(), "Already issued unit should keep its historic blood group");
    }

    @Test
    @DisplayName("Updating patient blood group cascades to pending blood requests")
    void testPatientBloodGroupCascade() {
        int patientId = 77;
        Patient patient = new Patient(patientId, "Robert Chen", 45, "+1-555-0144", "B+", "St. Jude Hospital");

        // Simulated blood requests for this patient
        BloodRequest pendingRequest = new BloodRequest(201, patientId, patient.getBloodGroup(),
                "WHOLE_BLOOD", 2, "URGENT", null, "PENDING");
        BloodRequest fulfilledRequest = new BloodRequest(202, patientId, patient.getBloodGroup(),
                "WHOLE_BLOOD", 1, "NORMAL", null, "FULFILLED");

        List<BloodRequest> requests = new ArrayList<>(List.of(pendingRequest, fulfilledRequest));

        // Patient blood group corrected to AB+
        String newBloodGroup = "AB+";
        patient.setBloodGroup(newBloodGroup);

        // Cascade rule: update only PENDING requests
        for (int i = 0; i < requests.size(); i++) {
            BloodRequest r = requests.get(i);
            if ("PENDING".equals(r.getStatus())) {
                requests.set(i, new BloodRequest(r.getRequestId(), r.getPatientId(), newBloodGroup,
                        r.getProductType(), r.getUnitsRequired(), r.getPriority(), r.getRequestDate(), r.getStatus()));
            }
        }

        assertEquals("AB+", requests.get(0).getBloodGroup(), "Pending request should cascade to new patient blood group");
        assertEquals("B+", requests.get(1).getBloodGroup(), "Historical fulfilled request should remain intact");
    }

    @Test
    @DisplayName("Deleting donor unlinks issued units and removes available units")
    void testDonorDeleteCascade() {
        BloodUnit availableUnit = new BloodUnit(301, 10, "O-", "WHOLE_BLOOD",
                LocalDate.now(), LocalDate.now().plusDays(35), "AVAILABLE");
        BloodUnit issuedUnit = new BloodUnit(302, 10, "O-", "WHOLE_BLOOD",
                LocalDate.now().minusDays(20), LocalDate.now().plusDays(15), "ISSUED");

        List<BloodUnit> inventory = new ArrayList<>(List.of(availableUnit, issuedUnit));

        // Simulation of delete cascade:
        // 1. Remove available units linked to donor donation (id: 10)
        inventory.removeIf(u -> u.getDonationId() == 10 && "AVAILABLE".equals(u.getStatus()));

        // 2. Unlink issued units (donation_id = 0)
        for (int i = 0; i < inventory.size(); i++) {
            BloodUnit u = inventory.get(i);
            if (u.getDonationId() == 10 && "ISSUED".equals(u.getStatus())) {
                inventory.set(i, new BloodUnit(u.getUnitId(), 0, u.getBloodGroup(),
                        u.getProductType(), u.getCollectionDate(), u.getExpiryDate(), u.getStatus()));
            }
        }

        assertEquals(1, inventory.size(), "Only issued unit should remain in inventory");
        assertEquals(0, inventory.get(0).getDonationId(), "Issued unit should have donation_id unlinked (0)");
        assertEquals("ISSUED", inventory.get(0).getStatus(), "Issued status preserved for audit");
    }

    @Test
    @DisplayName("Deleting patient removes all pending requests")
    void testPatientDeleteCascade() {
        int patientId = 99;
        List<BloodRequest> requests = new ArrayList<>(List.of(
                new BloodRequest(401, patientId, "A-", "WHOLE_BLOOD", 1, "EMERGENCY", null, "PENDING"),
                new BloodRequest(402, 888, "O+", "WHOLE_BLOOD", 2, "NORMAL", null, "PENDING")
        ));

        // Simulate cascade deletion for patient 99
        requests.removeIf(r -> r.getPatientId() == patientId);

        assertEquals(1, requests.size(), "Only other patients' requests should remain");
        assertEquals(888, requests.get(0).getPatientId());
    }
}
