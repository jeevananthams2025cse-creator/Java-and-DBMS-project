package com.bloodbank.dao;

import com.bloodbank.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO {

    public record InventoryReportRow(
            String bloodGroup,
            int availableUnits,
            int issuedUnits,
            int expiredUnits,
            int totalUnits
    ) {}

    public record EmergencyReportRow(
            int requestId,
            String patientName,
            String bloodGroup,
            int unitsRequired,
            String priority,
            String status,
            int availableUnits,
            boolean isSufficient
    ) {}

    public void displayInventoryReport() {

        String sql = """
            SELECT
                blood_group,
                SUM(CASE
                    WHEN status = 'AVAILABLE'
                         AND expiry_date >= CURRENT_DATE
                    THEN 1 ELSE 0
                END) AS available_units,

                SUM(CASE
                    WHEN status = 'ISSUED'
                    THEN 1 ELSE 0
                END) AS issued_units,

                SUM(CASE
                    WHEN expiry_date < CURRENT_DATE
                         AND status = 'AVAILABLE'
                    THEN 1 ELSE 0
                END) AS expired_units

            FROM blood_units
            GROUP BY blood_group
            ORDER BY blood_group
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            System.out.println("\n--- BLOOD INVENTORY REPORT ---");
            System.out.printf(
                    "%-15s %-15s %-15s %-15s%n",
                    "Blood Group", "Available",
                    "Issued", "Expired"
            );

            System.out.println(
                    "------------------------------------------------------------"
            );

            boolean found = false;

            while (result.next()) {
                found = true;

                System.out.printf(
                        "%-15s %-15d %-15d %-15d%n",
                        result.getString("blood_group"),
                        result.getInt("available_units"),
                        result.getInt("issued_units"),
                        result.getInt("expired_units")
                );
            }

            if (!found) {
                System.out.println("No blood inventory records found.");
            }

        } catch (SQLException e) {
            System.out.println(
                    "Could not generate inventory report: "
                            + e.getMessage()
            );
        }
    }

    public void displayEmergencyRequestReport() {

        String sql = """
            SELECT
                br.request_id,
                p.name AS patient_name,
                br.blood_group,
                br.units_required,
                br.priority,
                br.status,
                (
                    SELECT COUNT(*)
                    FROM blood_units bu
                    JOIN compatibility_rules cr
                        ON cr.donor_group = bu.blood_group
                    WHERE cr.recipient_group = br.blood_group
                      AND bu.product_type = br.product_type
                      AND bu.status = 'AVAILABLE'
                      AND bu.expiry_date >= CURRENT_DATE
                ) AS available_units
            FROM blood_requests br
            JOIN patients p
                ON p.patient_id = br.patient_id
            WHERE br.status = 'PENDING'
            ORDER BY
                CASE br.priority
                    WHEN 'EMERGENCY' THEN 1
                    WHEN 'URGENT' THEN 2
                    ELSE 3
                END,
                br.request_date ASC
            """;

        try (var connection = DatabaseConnection.getConnection();
             var statement = connection.prepareStatement(sql);
             var result = statement.executeQuery()) {

            System.out.println("\n--- PENDING BLOOD REQUEST REPORT ---");
            System.out.printf(
                "%-8s %-20s %-12s %-8s %-12s %-12s %-10s%n",
                "ID", "Patient", "Blood Group",
                "Required", "Priority", "Status", "Available"
            );

            System.out.println(
                "--------------------------------------------------------------------------------"
            );

            boolean found = false;

            while (result.next()) {
                found = true;

                int required = result.getInt("units_required");
                int available = result.getInt("available_units");

                System.out.printf(
                    "%-8d %-20s %-12s %-8d %-12s %-12s %-10d%n",
                    result.getInt("request_id"),
                    result.getString("patient_name"),
                    result.getString("blood_group"),
                    required,
                    result.getString("priority"),
                    result.getString("status"),
                    available
                );

                if (available >= required) {
                    System.out.println("         Stock check: Enough compatible units available.");
                } else {
                    System.out.println("         Stock check: Insufficient compatible units.");
                }
            }

            if (!found) {
                System.out.println("No pending blood requests found.");
            }

        } catch (SQLException e) {
            System.out.println(
                "Could not generate request report: " + e.getMessage()
            );
        }
    }

    public List<InventoryReportRow> getInventoryReportData() {
        List<InventoryReportRow> list = new ArrayList<>();
        String sql = """
            SELECT
                blood_group,
                SUM(CASE
                    WHEN status = 'AVAILABLE'
                         AND expiry_date >= CURRENT_DATE
                    THEN 1 ELSE 0
                END) AS available_units,

                SUM(CASE
                    WHEN status = 'ISSUED'
                    THEN 1 ELSE 0
                END) AS issued_units,

                SUM(CASE
                    WHEN expiry_date < CURRENT_DATE
                         AND status = 'AVAILABLE'
                    THEN 1 ELSE 0
                END) AS expired_units,

                COUNT(*) AS total_units

            FROM blood_units
            GROUP BY blood_group
            ORDER BY blood_group
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                list.add(new InventoryReportRow(
                        rs.getString("blood_group"),
                        rs.getInt("available_units"),
                        rs.getInt("issued_units"),
                        rs.getInt("expired_units"),
                        rs.getInt("total_units")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Failed to get inventory report data: " + e.getMessage());
        }

        return list;
    }

    public List<EmergencyReportRow> getEmergencyRequestReportData() {
        List<EmergencyReportRow> list = new ArrayList<>();
        String sql = """
            SELECT
                br.request_id,
                p.name AS patient_name,
                br.blood_group,
                br.units_required,
                br.priority,
                br.status,
                (
                    SELECT COUNT(*)
                    FROM blood_units bu
                    JOIN compatibility_rules cr
                        ON cr.donor_group = bu.blood_group
                    WHERE cr.recipient_group = br.blood_group
                      AND bu.product_type = br.product_type
                      AND bu.status = 'AVAILABLE'
                      AND bu.expiry_date >= CURRENT_DATE
                ) AS available_units
            FROM blood_requests br
            JOIN patients p
                ON p.patient_id = br.patient_id
            WHERE br.status = 'PENDING'
            ORDER BY
                CASE br.priority
                    WHEN 'EMERGENCY' THEN 1
                    WHEN 'URGENT' THEN 2
                    ELSE 3
                END,
                br.request_date ASC
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                int required = rs.getInt("units_required");
                int available = rs.getInt("available_units");

                list.add(new EmergencyReportRow(
                        rs.getInt("request_id"),
                        rs.getString("patient_name"),
                        rs.getString("blood_group"),
                        required,
                        rs.getString("priority"),
                        rs.getString("status"),
                        available,
                        available >= required
                ));
            }
        } catch (SQLException e) {
            System.out.println("Failed to get emergency request report data: " + e.getMessage());
        }

        return list;
    }
}
