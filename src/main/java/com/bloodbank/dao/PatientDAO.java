package com.bloodbank.dao;

import com.bloodbank.model.Patient;
import com.bloodbank.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PatientDAO {

    public record PatientStats(int totalRequests, int pendingRequests, int fulfilledRequests) {}

    public boolean addPatient(Patient patient) {

        String sql = """
                INSERT INTO patients
                (name, age, blood_group, hospital, phone)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setString(1, patient.getName());
            ps.setInt(2, patient.getAge());
            ps.setString(3, patient.getBloodGroup());
            ps.setString(4, patient.getHospital());
            ps.setString(5, patient.getPhone());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Failed to add patient: " + e.getMessage());
            return false;
        }
    }

    public List<Patient> getAllPatients() {

        List<Patient> patients = new ArrayList<>();

        String sql = "SELECT * FROM patients";

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {

                Patient patient = new Patient(
                        rs.getInt("patient_id"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getString("phone"),
                        rs.getString("blood_group"),
                        rs.getString("hospital")
                );

                patients.add(patient);
            }

        } catch (SQLException e) {
            System.out.println("Failed to fetch patients: " + e.getMessage());
        }

        return patients;
    }

    public Patient getPatientById(int patientId) {

        String sql = "SELECT * FROM patients WHERE patient_id = ?";

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, patientId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return new Patient(
                            rs.getInt("patient_id"),
                            rs.getString("name"),
                            rs.getInt("age"),
                            rs.getString("phone"),
                            rs.getString("blood_group"),
                            rs.getString("hospital")
                    );
                }
            }

        } catch (SQLException e) {
            System.out.println("Failed to find patient: " + e.getMessage());
        }

        return null;
    }

    public boolean updatePatient(Patient patient) {
        return updatePatientWithCascade(patient);
    }

    public boolean updatePatientWithCascade(Patient patient) {
        String updatePatientSql = """
                UPDATE patients
                SET name = ?,
                    age = ?,
                    blood_group = ?,
                    hospital = ?,
                    phone = ?
                WHERE patient_id = ?
                """;

        String cascadePendingRequestsSql = """
                UPDATE blood_requests
                SET blood_group = ?
                WHERE patient_id = ? AND status = 'PENDING'
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                // 1. Update patient master record
                try (PreparedStatement ps = connection.prepareStatement(updatePatientSql)) {
                    ps.setString(1, patient.getName());
                    ps.setInt(2, patient.getAge());
                    ps.setString(3, patient.getBloodGroup());
                    ps.setString(4, patient.getHospital());
                    ps.setString(5, patient.getPhone());
                    ps.setInt(6, patient.getId());

                    int rows = ps.executeUpdate();
                    if (rows == 0) {
                        throw new SQLException("Patient #" + patient.getId() + " not found.");
                    }
                }

                // 2. Cascade updated blood group to any pending requests for this patient
                try (PreparedStatement ps = connection.prepareStatement(cascadePendingRequestsSql)) {
                    ps.setString(1, patient.getBloodGroup());
                    ps.setInt(2, patient.getId());
                    ps.executeUpdate();
                }

                connection.commit();
                return true;

            } catch (SQLException e) {
                connection.rollback();
                System.out.println("Failed to update patient with cascade: " + e.getMessage());
                return false;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
            return false;
        }
    }

    public boolean deletePatient(int patientId) {
        return deletePatientWithCascade(patientId);
    }

    public boolean deletePatientWithCascade(int patientId) {
        String checkIssuesSql = """
                SELECT COUNT(*) FROM blood_issues bi
                JOIN blood_requests br ON bi.request_id = br.request_id
                WHERE br.patient_id = ?
                """;

        String deleteRequestsSql = "DELETE FROM blood_requests WHERE patient_id = ?";
        String deletePatientSql = "DELETE FROM patients WHERE patient_id = ?";

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                // 1. Audit check: verify patient has NO issued transfusions on record
                try (PreparedStatement ps = connection.prepareStatement(checkIssuesSql)) {
                    ps.setInt(1, patientId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            throw new SQLException("Cannot delete Patient #" + patientId + ": Patient has " 
                                    + rs.getInt(1) + " clinical transfusion record(s) on file. Medical audit logs are permanent and immutable.");
                        }
                    }
                }

                // 2. Delete blood requests (pending/unfulfilled only, as no issues exist)
                try (PreparedStatement ps = connection.prepareStatement(deleteRequestsSql)) {
                    ps.setInt(1, patientId);
                    ps.executeUpdate();
                }

                // 3. Delete patient record
                int rows;
                try (PreparedStatement ps = connection.prepareStatement(deletePatientSql)) {
                    ps.setInt(1, patientId);
                    rows = ps.executeUpdate();
                }

                if (rows == 0) {
                    throw new SQLException("Patient #" + patientId + " not found.");
                }

                connection.commit();
                System.out.println("Patient #" + patientId + " and unfulfilled requests deleted successfully.");
                return true;

            } catch (SQLException e) {
                connection.rollback();
                System.out.println("Failed to delete patient: " + e.getMessage());
                throw new RuntimeException(e.getMessage(), e);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    public PatientStats getPatientStats(int patientId) {
        String sql = """
                SELECT
                    (SELECT COUNT(*) FROM blood_requests WHERE patient_id = ?) AS total_requests,
                    (SELECT COUNT(*) FROM blood_requests WHERE patient_id = ? AND status = 'PENDING') AS pending_requests,
                    (SELECT COUNT(*) FROM blood_requests WHERE patient_id = ? AND status = 'FULFILLED') AS fulfilled_requests
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, patientId);
            ps.setInt(2, patientId);
            ps.setInt(3, patientId);

            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new PatientStats(
                            rs.getInt("total_requests"),
                            rs.getInt("pending_requests"),
                            rs.getInt("fulfilled_requests")
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to fetch patient stats: " + e.getMessage());
        }

        return new PatientStats(0, 0, 0);
    }

    public List<Patient> getPatients(String search, int page, int pageSize) {
        List<Patient> patients = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM patients WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (search != null && !search.isBlank()) {
            sql.append(" AND (LOWER(name) LIKE ? OR phone LIKE ? OR LOWER(hospital) LIKE ? OR CAST(patient_id AS CHAR) LIKE ?)");
            String searchParam = "%" + search.trim().toLowerCase().replace("#p-", "").replace("p-", "") + "%";
            params.add(searchParam);
            params.add(searchParam);
            params.add(searchParam);
            params.add(searchParam);
        }

        sql.append(" ORDER BY patient_id DESC LIMIT ? OFFSET ?");
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
                    patients.add(new Patient(
                            rs.getInt("patient_id"),
                            rs.getString("name"),
                            rs.getInt("age"),
                            rs.getString("phone"),
                            rs.getString("blood_group"),
                            rs.getString("hospital")
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to fetch filtered patients: " + e.getMessage());
        }

        return patients;
    }

    public int countPatients(String search) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM patients WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (search != null && !search.isBlank()) {
            sql.append(" AND (LOWER(name) LIKE ? OR phone LIKE ? OR LOWER(hospital) LIKE ? OR CAST(patient_id AS CHAR) LIKE ?)");
            String searchParam = "%" + search.trim().toLowerCase().replace("#p-", "").replace("p-", "") + "%";
            params.add(searchParam);
            params.add(searchParam);
            params.add(searchParam);
            params.add(searchParam);
        }

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql.toString())
        ) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to count patients: " + e.getMessage());
        }

        return 0;
    }
}
