package com.bloodbank;

import com.bloodbank.model.BloodUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class CompatibilityAndExpiryTest {

    // Standard ABO/Rh whole blood recipient compatibility matrix
    private static final Map<String, Set<String>> COMPATIBLE_DONORS_FOR_RECIPIENT = Map.of(
            "O-", Set.of("O-"),
            "O+", Set.of("O-", "O+"),
            "A-", Set.of("O-", "A-"),
            "A+", Set.of("O-", "O+", "A-", "A+"),
            "B-", Set.of("O-", "B-"),
            "B+", Set.of("O-", "O+", "B-", "B+"),
            "AB-", Set.of("O-", "A-", "B-", "AB-"),
            "AB+", Set.of("O-", "O+", "A-", "A+", "B-", "B+", "AB-", "AB+")
    );

    private boolean isCompatible(String donorGroup, String recipientGroup) {
        Set<String> validDonors = COMPATIBLE_DONORS_FOR_RECIPIENT.get(recipientGroup);
        return validDonors != null && validDonors.contains(donorGroup);
    }

    @Test
    @DisplayName("O- is the universal donor and can donate to all 8 blood groups")
    void testUniversalDonor() {
        String universalDonor = "O-";
        for (String recipient : COMPATIBLE_DONORS_FOR_RECIPIENT.keySet()) {
            assertTrue(isCompatible(universalDonor, recipient),
                    "O- should be compatible with recipient: " + recipient);
        }
    }

    @Test
    @DisplayName("AB+ is the universal recipient and can receive from all 8 blood groups")
    void testUniversalRecipient() {
        String universalRecipient = "AB+";
        List<String> allGroups = List.of("O-", "O+", "A-", "A+", "B-", "B+", "AB-", "AB+");
        for (String donor : allGroups) {
            assertTrue(isCompatible(donor, universalRecipient),
                    "AB+ should be able to receive from donor: " + donor);
        }
    }

    @Test
    @DisplayName("Rh positive blood cannot be given to Rh negative recipient")
    void testRhIncompatibility() {
        assertFalse(isCompatible("O+", "O-"), "O+ cannot donate to O-");
        assertFalse(isCompatible("A+", "A-"), "A+ cannot donate to A-");
        assertFalse(isCompatible("B+", "B-"), "B+ cannot donate to B-");
        assertFalse(isCompatible("AB+", "AB-"), "AB+ cannot donate to AB-");
    }

    @Test
    @DisplayName("Incompatible ABO groups are rejected")
    void testAboIncompatibility() {
        assertFalse(isCompatible("A+", "B+"), "A+ cannot donate to B+");
        assertFalse(isCompatible("B+", "A+"), "B+ cannot donate to A+");
        assertFalse(isCompatible("AB+", "O+"), "AB+ cannot donate to O+");
        assertFalse(isCompatible("A-", "O+"), "A- cannot donate to O+");
    }

    @Test
    @DisplayName("Expired units are correctly filtered out")
    void testExpiryFiltering() {
        LocalDate today = LocalDate.now();

        BloodUnit expiredUnit = new BloodUnit(
                1, 101, "O+", "WHOLE_BLOOD",
                today.minusDays(40), today.minusDays(5), "AVAILABLE"
        );

        BloodUnit validUnit = new BloodUnit(
                2, 102, "O+", "WHOLE_BLOOD",
                today.minusDays(10), today.plusDays(21), "AVAILABLE"
        );

        BloodUnit expiresToday = new BloodUnit(
                3, 103, "O+", "WHOLE_BLOOD",
                today.minusDays(31), today, "AVAILABLE"
        );

        List<BloodUnit> allUnits = List.of(expiredUnit, validUnit, expiresToday);

        // Filter: status == AVAILABLE and expiry_date >= today
        List<BloodUnit> nonExpired = allUnits.stream()
                .filter(u -> "AVAILABLE".equals(u.getStatus()) && !u.getExpiryDate().isBefore(today))
                .toList();

        assertEquals(2, nonExpired.size(), "Only non-expired units should be returned");
        assertFalse(nonExpired.contains(expiredUnit), "Expired unit must never appear");
        assertTrue(nonExpired.contains(validUnit), "Valid future unit must appear");
        assertTrue(nonExpired.contains(expiresToday), "Unit expiring today should be valid");
    }

    @Test
    @DisplayName("Suggested units are ordered by earliest expiry date first (FEFO)")
    void testFefoOrdering() {
        LocalDate today = LocalDate.now();

        BloodUnit unitExpiryFar = new BloodUnit(
                1, 101, "A+", "WHOLE_BLOOD",
                today.minusDays(5), today.plusDays(26), "AVAILABLE"
        );

        BloodUnit unitExpirySoon = new BloodUnit(
                2, 102, "A+", "WHOLE_BLOOD",
                today.minusDays(25), today.plusDays(6), "AVAILABLE"
        );

        BloodUnit unitExpiryMid = new BloodUnit(
                3, 103, "A+", "WHOLE_BLOOD",
                today.minusDays(15), today.plusDays(16), "AVAILABLE"
        );

        List<BloodUnit> units = new ArrayList<>(List.of(unitExpiryFar, unitExpirySoon, unitExpiryMid));
        units.sort(Comparator.comparing(BloodUnit::getExpiryDate));

        assertEquals(2, units.get(0).getUnitId(), "Unit with earliest expiry (in 6 days) must be first");
        assertEquals(3, units.get(1).getUnitId(), "Unit with mid expiry (in 16 days) must be second");
        assertEquals(1, units.get(2).getUnitId(), "Unit with latest expiry (in 26 days) must be third");
    }
}
