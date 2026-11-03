
package com.bloodbank.service;

import com.bloodbank.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CompatibilityService {

    public List<String> getCompatibleBloodGroups(String recipientGroup) {

        List<String> groups = new ArrayList<>();

        String sql = """
                SELECT donor_group
                FROM compatibility_rules
                WHERE recipient_group = ?
                ORDER BY donor_group
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setString(1, recipientGroup);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    groups.add(rs.getString("donor_group"));
                }
            }

        } catch (SQLException e) {
            System.out.println(
                    "Failed to check compatibility: " + e.getMessage()
            );
        }

        return groups;
    }
}
