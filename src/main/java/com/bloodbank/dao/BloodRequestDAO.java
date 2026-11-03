package com.bloodbank.dao;

import com.bloodbank.model.BloodRequest;
import com.bloodbank.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class BloodRequestDAO {

    public record BloodRequestView(
            int requestId,
            int patientId,
            String patientName,
            String hospital,
            String bloodGroup,
            String productType,
            int unitsRequired,
            String priority,
            java.time.LocalDateTime requestDate,
            String status
    ) {}

    // Register a new blood request
    public boolean addRequest(BloodRequest request) {

        String sql = """
                INSERT INTO blood_requests
                (patient_id, blood_group, product_type,
                 units_required, priority, status)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, request.getPatientId());
            ps.setString(2, request.getBloodGroup());
            ps.setString(3, request.getProductType());
            ps.setInt(4, request.getUnitsRequired());
            ps.setString(5, request.getPriority());
            ps.setString(6, request.getStatus());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println(
                    "Failed to add blood request: " + e.getMessage()
            );
            return false;
        }
    }
    
    // Retrieve all blood requests
    public List<BloodRequest> getAllRequests() {

        List<BloodRequest> requests = new ArrayList<>();

        String sql = """
                SELECT *
                FROM blood_requests
                ORDER BY
                    CASE priority
                        WHEN 'EMERGENCY' THEN 1
                        WHEN 'URGENT' THEN 2
                        ELSE 3
                    END,
                    request_date DESC
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {

                Timestamp timestamp = rs.getTimestamp("request_date");

                BloodRequest request = new BloodRequest(
                        rs.getInt("request_id"),
                        rs.getInt("patient_id"),
                        rs.getString("blood_group"),
                        rs.getString("product_type"),
                        rs.getInt("units_required"),
                        rs.getString("priority"),
                        timestamp.toLocalDateTime(),
                        rs.getString("status")
                );

                requests.add(request);
            }

        } catch (SQLException e) {
            System.out.println(
                    "Failed to fetch blood requests: " + e.getMessage()
            );
        }

        return requests;
    }

    public BloodRequest getRequestById(int requestId) {
        String sql = "SELECT * FROM blood_requests WHERE request_id = ?";

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, requestId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Timestamp timestamp = rs.getTimestamp("request_date");
                    return new BloodRequest(
                            rs.getInt("request_id"),
                            rs.getInt("patient_id"),
                            rs.getString("blood_group"),
                            rs.getString("product_type"),
                            rs.getInt("units_required"),
                            rs.getString("priority"),
                            timestamp.toLocalDateTime(),
                            rs.getString("status")
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to fetch request by ID: " + e.getMessage());
        }

        return null;
    }

    public List<BloodRequestView> getRequestViews(String tab, int page, int pageSize) {
        List<BloodRequestView> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                SELECT br.request_id, br.patient_id, p.name AS patient_name, p.hospital,
                       br.blood_group, br.product_type, br.units_required, br.priority,
                       br.request_date, br.status
                FROM blood_requests br
                JOIN patients p ON br.patient_id = p.patient_id
                WHERE 1=1
                """);
        List<Object> params = new ArrayList<>();

        if ("PENDING".equalsIgnoreCase(tab)) {
            sql.append(" AND br.status = 'PENDING'");
        } else if ("FULFILLED".equalsIgnoreCase(tab)) {
            sql.append(" AND br.status = 'FULFILLED'");
        } else if ("EMERGENCY".equalsIgnoreCase(tab)) {
            sql.append(" AND br.priority = 'EMERGENCY'");
        }

        sql.append("""
                 ORDER BY
                    CASE br.priority
                        WHEN 'EMERGENCY' THEN 1
                        WHEN 'URGENT' THEN 2
                        ELSE 3
                    END,
                    br.request_date DESC
                 LIMIT ? OFFSET ?
                """);
        params.add(pageSize);
        params.add(Math.max(0, (page - 1) * pageSize));

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql.toString())
        ) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("request_date");
                    list.add(new BloodRequestView(
                            rs.getInt("request_id"),
                            rs.getInt("patient_id"),
                            rs.getString("patient_name"),
                            rs.getString("hospital"),
                            rs.getString("blood_group"),
                            rs.getString("product_type"),
                            rs.getInt("units_required"),
                            rs.getString("priority"),
                            ts != null ? ts.toLocalDateTime() : java.time.LocalDateTime.now(),
                            rs.getString("status")
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to fetch request views: " + e.getMessage());
        }

        return list;
    }

    public int countRequestViews(String tab) {
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*)
                FROM blood_requests br
                WHERE 1=1
                """);

        if ("PENDING".equalsIgnoreCase(tab)) {
            sql.append(" AND br.status = 'PENDING'");
        } else if ("FULFILLED".equalsIgnoreCase(tab)) {
            sql.append(" AND br.status = 'FULFILLED'");
        } else if ("EMERGENCY".equalsIgnoreCase(tab)) {
            sql.append(" AND br.priority = 'EMERGENCY'");
        }

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql.toString());
                ResultSet rs = ps.executeQuery()
        ) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Failed to count request views: " + e.getMessage());
        }

        return 0;
    }
}
