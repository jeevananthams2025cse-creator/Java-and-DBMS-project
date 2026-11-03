package com.bloodbank.service;

import com.bloodbank.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RequestService {

    public record FulfilmentResult(boolean success, String message, int unitsFulfilled) {}

    public boolean fulfilRequest(int requestId) {
        return processFulfilment(requestId).success();
    }

    public FulfilmentResult processFulfilment(int requestId) {

        String requestSql = """
                SELECT patient_id, blood_group, product_type,
                       units_required, status
                FROM blood_requests
                WHERE request_id = ?
                FOR UPDATE
                """;

        String unitsSql = """
                SELECT bu.unit_id
                FROM blood_units bu
                JOIN compatibility_rules cr
                  ON bu.blood_group = cr.donor_group
                WHERE cr.recipient_group = ?
                  AND bu.product_type = 'WHOLE_BLOOD'
                  AND bu.status = 'AVAILABLE'
                  AND bu.expiry_date >= CURRENT_DATE
                ORDER BY bu.expiry_date ASC, bu.unit_id ASC
                LIMIT ?
                FOR UPDATE
                """;

        String issueSql = """
                INSERT INTO blood_issues (request_id, unit_id)
                VALUES (?, ?)
                """;

        String updateUnitSql = """
                UPDATE blood_units
                SET status = 'ISSUED'
                WHERE unit_id = ?
                  AND status = 'AVAILABLE'
                  AND expiry_date >= CURRENT_DATE
                """;

        String updateRequestSql = """
                UPDATE blood_requests
                SET status = 'FULFILLED'
                WHERE request_id = ?
                  AND status = 'PENDING'
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                int unitsRequired;
                String bloodGroup;
                String productType;
                String status;

                // 1. Lock and read the request
                try (PreparedStatement ps = connection.prepareStatement(requestSql)) {

                    ps.setInt(1, requestId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Request ID #" + requestId + " not found.");
                        }

                        bloodGroup = rs.getString("blood_group");
                        productType = rs.getString("product_type");
                        unitsRequired = rs.getInt("units_required");
                        status = rs.getString("status");
                    }
                }

                if (!"PENDING".equalsIgnoreCase(status)) {
                    throw new SQLException("Only PENDING requests can be fulfilled. Current status is " + status + ".");
                }

                if (!"WHOLE_BLOOD".equalsIgnoreCase(productType)) {
                    throw new SQLException("This workflow supports WHOLE_BLOOD only.");
                }

                if (unitsRequired <= 0) {
                    throw new SQLException("Required unit count must be positive.");
                }

                // 2. Find and lock enough compatible, non-expired units
                List<Integer> unitIds = new ArrayList<>();

                try (PreparedStatement ps = connection.prepareStatement(unitsSql)) {

                    ps.setString(1, bloodGroup);
                    ps.setInt(2, unitsRequired);

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            unitIds.add(rs.getInt("unit_id"));
                        }
                    }
                }

                if (unitIds.size() < unitsRequired) {
                    throw new SQLException(
                            "Insufficient compatible units in stock. Required: "
                                    + unitsRequired
                                    + ", found: " + unitIds.size() + "."
                    );
                }

                // 3. Mark each unit issued and record its issue
                try (
                        PreparedStatement updateUnit = connection.prepareStatement(updateUnitSql);
                        PreparedStatement insertIssue = connection.prepareStatement(issueSql)
                ) {
                    for (int unitId : unitIds) {

                        updateUnit.setInt(1, unitId);

                        if (updateUnit.executeUpdate() != 1) {
                            throw new SQLException(
                                    "Unit " + unitId + " is no longer available or was concurrently allocated."
                            );
                        }

                        insertIssue.setInt(1, requestId);
                        insertIssue.setInt(2, unitId);
                        insertIssue.executeUpdate();
                    }
                }

                // 4. Mark the request fulfilled
                try (PreparedStatement ps = connection.prepareStatement(updateRequestSql)) {

                    ps.setInt(1, requestId);

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("Request status could not be updated.");
                    }
                }

                // 5. Commit all changes together in a single transaction
                connection.commit();

                String successMsg = "Request #" + requestId + " fulfilled successfully with " + unitIds.size() + " compatible unit(s).";
                System.out.println(successMsg);

                return new FulfilmentResult(true, successMsg, unitIds.size());

            } catch (Exception e) {

                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    e.addSuppressed(rollbackEx);
                }

                String failMsg = "Request fulfilment failed: " + e.getMessage();
                System.out.println(failMsg);
                System.out.println("All changes were rolled back.");

                return new FulfilmentResult(false, e.getMessage(), 0);

            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    System.out.println("Could not restore auto-commit: " + e.getMessage());
                }
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return new FulfilmentResult(false, "Database connection error: " + e.getMessage(), 0);
        }
    }
}
