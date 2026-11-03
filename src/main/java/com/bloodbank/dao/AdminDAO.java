package com.bloodbank.dao;

import com.bloodbank.model.Admin;
import com.bloodbank.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class AdminDAO {

    public Admin findByEmail(String email) {
        String sql = "SELECT id, name, email, password_hash, created_at FROM admins WHERE email = ?";

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Timestamp ts = rs.getTimestamp("created_at");
                    LocalDateTime createdAt = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();

                    return new Admin(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("password_hash"),
                            createdAt
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to find admin by email: " + e.getMessage());
        }

        return null;
    }

    public int countAdmins() {
        String sql = "SELECT COUNT(*) FROM admins";

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Failed to count admins: " + e.getMessage());
        }

        return 0;
    }

    public boolean createAdmin(Admin admin) {
        String sql = """
                INSERT INTO admins (name, email, password_hash, created_at)
                VALUES (?, ?, ?, ?)
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setString(1, admin.getName());
            ps.setString(2, admin.getEmail());
            ps.setString(3, admin.getPasswordHash());
            ps.setTimestamp(4, Timestamp.valueOf(
                    admin.getCreatedAt() != null ? admin.getCreatedAt() : LocalDateTime.now()
            ));

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Failed to create admin: " + e.getMessage());
            return false;
        }
    }
}
