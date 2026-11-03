package com.bloodbank.service;

import com.bloodbank.dao.BloodUnitDAO;
import com.bloodbank.model.BloodUnit;
import com.bloodbank.util.DatabaseConnection;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class InventoryService {

    private final BloodUnitDAO bloodUnitDAO = new BloodUnitDAO();

    public record BloodGroupStockDetail(
            String bloodGroup,
            int availableUnits,
            int expiringSoonUnits,
            int expiredUnits,
            int issuedUnits,
            int totalUnits
    ) {}

    public record AvailableUnitView(
            int unitId,
            int donationId,
            String bloodGroup,
            String productType,
            LocalDate collectionDate,
            LocalDate expiryDate,
            long daysLeft,
            boolean expiringSoon,
            String status
    ) {}

    public List<BloodUnit> findCompatibleWholeBloodUnits(String recipientGroup) {
        List<BloodUnit> units = new ArrayList<>();

        String sql = """
                SELECT bu.unit_id,
                       bu.donation_id,
                       bu.blood_group,
                       bu.product_type,
                       bu.collection_date,
                       bu.expiry_date,
                       bu.status
                FROM blood_units bu
                JOIN compatibility_rules cr
                  ON bu.blood_group = cr.donor_group
                WHERE cr.recipient_group = ?
                  AND bu.product_type = 'WHOLE_BLOOD'
                  AND bu.status = 'AVAILABLE'
                  AND bu.expiry_date >= CURRENT_DATE
                ORDER BY bu.expiry_date ASC, bu.unit_id ASC
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setString(1, recipientGroup);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    units.add(new BloodUnit(
                            rs.getInt("unit_id"),
                            rs.getInt("donation_id"),
                            rs.getString("blood_group"),
                            rs.getString("product_type"),
                            rs.getDate("collection_date").toLocalDate(),
                            rs.getDate("expiry_date").toLocalDate(),
                            rs.getString("status")
                    ));
                }
            }

        } catch (SQLException e) {
            System.out.println(
                    "Failed to search compatible inventory: "
                            + e.getMessage()
            );
        }

        return units;
    }

    public List<BloodGroupStockDetail> getInventoryStockBreakdown() {
        Map<String, BloodGroupStockDetail> map = new LinkedHashMap<>();
        for (String bg : DashboardService.BLOOD_GROUPS) {
            map.put(bg, new BloodGroupStockDetail(bg, 0, 0, 0, 0, 0));
        }

        List<BloodUnit> allUnits = bloodUnitDAO.getAllBloodUnits();
        LocalDate today = LocalDate.now();

        Map<String, Integer> availableMap = new LinkedHashMap<>();
        Map<String, Integer> expiringSoonMap = new LinkedHashMap<>();
        Map<String, Integer> expiredMap = new LinkedHashMap<>();
        Map<String, Integer> issuedMap = new LinkedHashMap<>();
        Map<String, Integer> totalMap = new LinkedHashMap<>();

        for (String bg : DashboardService.BLOOD_GROUPS) {
            availableMap.put(bg, 0);
            expiringSoonMap.put(bg, 0);
            expiredMap.put(bg, 0);
            issuedMap.put(bg, 0);
            totalMap.put(bg, 0);
        }

        for (BloodUnit u : allUnits) {
            String bg = u.getBloodGroup();
            if (bg == null) continue;

            totalMap.put(bg, totalMap.getOrDefault(bg, 0) + 1);

            if ("ISSUED".equalsIgnoreCase(u.getStatus())) {
                issuedMap.put(bg, issuedMap.getOrDefault(bg, 0) + 1);
            } else if ("AVAILABLE".equalsIgnoreCase(u.getStatus())) {
                if (u.getExpiryDate().isBefore(today)) {
                    expiredMap.put(bg, expiredMap.getOrDefault(bg, 0) + 1);
                } else {
                    availableMap.put(bg, availableMap.getOrDefault(bg, 0) + 1);
                    long days = ChronoUnit.DAYS.between(today, u.getExpiryDate());
                    if (days <= 7) {
                        expiringSoonMap.put(bg, expiringSoonMap.getOrDefault(bg, 0) + 1);
                    }
                }
            }
        }

        List<BloodGroupStockDetail> result = new ArrayList<>();
        for (String bg : DashboardService.BLOOD_GROUPS) {
            result.add(new BloodGroupStockDetail(
                    bg,
                    availableMap.getOrDefault(bg, 0),
                    expiringSoonMap.getOrDefault(bg, 0),
                    expiredMap.getOrDefault(bg, 0),
                    issuedMap.getOrDefault(bg, 0),
                    totalMap.getOrDefault(bg, 0)
            ));
        }

        return result;
    }

    public List<AvailableUnitView> getAvailableUnitsList() {
        List<BloodUnit> units = bloodUnitDAO.getAvailableUnits();
        List<AvailableUnitView> views = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (BloodUnit u : units) {
            long daysLeft = ChronoUnit.DAYS.between(today, u.getExpiryDate());
            boolean expiringSoon = daysLeft <= 7;

            views.add(new AvailableUnitView(
                    u.getUnitId(),
                    u.getDonationId(),
                    u.getBloodGroup(),
                    u.getProductType(),
                    u.getCollectionDate(),
                    u.getExpiryDate(),
                    daysLeft,
                    expiringSoon,
                    u.getStatus()
            ));
        }

        return views;
    }
}
