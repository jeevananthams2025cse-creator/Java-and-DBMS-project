package com.bloodbank.dao;

import com.bloodbank.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BloodIssueDAO {

    public record IssueRecord(
            int issueId,
            int requestId,
            String patientName,
            String hospital,
            int unitId,
            String bloodGroup,
            String productType,
            LocalDateTime issueDate
    ) {}

    // Record one blood unit issued for a request
    public boolean recordIssue(int requestId, int unitId) {

        String sql = """
                INSERT INTO blood_issues (request_id, unit_id)
                VALUES (?, ?)
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, requestId);
            ps.setInt(2, unitId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println(
                    "Failed to record blood issue: " + e.getMessage()
            );
            return false;
        }
    }

    // Display issued units for a particular request (for console)
    public void displayIssuesForRequest(int requestId) {

        String sql = """
                SELECT bi.issue_id,
                       bi.request_id,
                       bu.unit_id,
                       bu.blood_group,
                       bu.product_type,
                       bi.issue_date
                FROM blood_issues bi
                JOIN blood_units bu ON bi.unit_id = bu.unit_id
                WHERE bi.request_id = ?
                ORDER BY bi.issue_id
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, requestId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println(
                            "Issue ID: " + rs.getInt("issue_id")
                            + " | Request ID: " + rs.getInt("request_id")
                            + " | Unit ID: " + rs.getInt("unit_id")
                            + " | Blood Group: " + rs.getString("blood_group")
                            + " | Product: " + rs.getString("product_type")
                            + " | Issued At: " + rs.getTimestamp("issue_date")
                    );
                }
            }

        } catch (SQLException e) {
            System.out.println(
                    "Failed to retrieve issue records: " + e.getMessage()
            );
        }
    }

    public List<IssueRecord> getAllIssues(int page, int pageSize) {
        List<IssueRecord> issues = new ArrayList<>();
        String sql = """
                SELECT bi.issue_id, bi.request_id, p.name AS patient_name, p.hospital,
                       bu.unit_id, bu.blood_group, bu.product_type, bi.issue_date
                FROM blood_issues bi
                JOIN blood_requests br ON bi.request_id = br.request_id
                JOIN patients p ON br.patient_id = p.patient_id
                JOIN blood_units bu ON bi.unit_id = bu.unit_id
                ORDER BY bi.issue_date DESC, bi.issue_id DESC
                LIMIT ? OFFSET ?
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, pageSize);
            ps.setInt(2, Math.max(0, (page - 1) * pageSize));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("issue_date");
                    issues.add(new IssueRecord(
                            rs.getInt("issue_id"),
                            rs.getInt("request_id"),
                            rs.getString("patient_name"),
                            rs.getString("hospital"),
                            rs.getInt("unit_id"),
                            rs.getString("blood_group"),
                            rs.getString("product_type"),
                            ts != null ? ts.toLocalDateTime() : LocalDateTime.now()
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to fetch all issues: " + e.getMessage());
        }

        return issues;
    }

    public int countIssues() {
        String sql = "SELECT COUNT(*) FROM blood_issues";

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Failed to count issues: " + e.getMessage());
        }

        return 0;
    }

    public List<IssueRecord> getIssuesForRequest(int requestId) {
        List<IssueRecord> issues = new ArrayList<>();
        String sql = """
                SELECT bi.issue_id, bi.request_id, p.name AS patient_name, p.hospital,
                       bu.unit_id, bu.blood_group, bu.product_type, bi.issue_date
                FROM blood_issues bi
                JOIN blood_requests br ON bi.request_id = br.request_id
                JOIN patients p ON br.patient_id = p.patient_id
                JOIN blood_units bu ON bi.unit_id = bu.unit_id
                WHERE bi.request_id = ?
                ORDER BY bi.issue_id ASC
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, requestId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("issue_date");
                    issues.add(new IssueRecord(
                            rs.getInt("issue_id"),
                            rs.getInt("request_id"),
                            rs.getString("patient_name"),
                            rs.getString("hospital"),
                            rs.getInt("unit_id"),
                            rs.getString("blood_group"),
                            rs.getString("product_type"),
                            ts != null ? ts.toLocalDateTime() : LocalDateTime.now()
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to fetch issues for request: " + e.getMessage());
        }

        return issues;
    }
}
