
package com.bloodbank.dao;

import com.bloodbank.model.BloodUnit;
import com.bloodbank.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class BloodUnitDAO {

    // Add a blood unit to inventory
    public boolean addBloodUnit(BloodUnit unit) {

        String sql = """
                INSERT INTO blood_units
                (donation_id, blood_group, product_type,
                 collection_date, expiry_date, status)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, unit.getDonationId());
            ps.setString(2, unit.getBloodGroup());
            ps.setString(3, unit.getProductType());
            ps.setDate(4, Date.valueOf(unit.getCollectionDate()));
            ps.setDate(5, Date.valueOf(unit.getExpiryDate()));
            ps.setString(6, unit.getStatus());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println(
                    "Failed to add blood unit: " + e.getMessage()
            );
            return false;
        }
    }public int addBloodUnitAndGetId(BloodUnit unit) {

    String sql = """
            INSERT INTO blood_units
            (donation_id, blood_group, product_type,
             collection_date, expiry_date, status)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

    try (
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(
                    sql,
                    java.sql.Statement.RETURN_GENERATED_KEYS
            )
    ) {
        ps.setInt(1, unit.getDonationId());
        ps.setString(2, unit.getBloodGroup());
        ps.setString(3, unit.getProductType());
        ps.setDate(4, Date.valueOf(unit.getCollectionDate()));
        ps.setDate(5, Date.valueOf(unit.getExpiryDate()));
        ps.setString(6, unit.getStatus());

        int rowsInserted = ps.executeUpdate();

        if (rowsInserted == 0) {
            return -1;
        }

        try (var keys = ps.getGeneratedKeys()) {
            if (keys.next()) {
                return keys.getInt(1);
            }
        }

    } catch (SQLException e) {
        System.out.println(
                "Failed to add blood unit: " + e.getMessage()
        );
    }

    return -1;
}

    // Retrieve all blood units
    public List<BloodUnit> getAllBloodUnits() {

        List<BloodUnit> units = new ArrayList<>();

        String sql = "SELECT * FROM blood_units ORDER BY unit_id";

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {

                BloodUnit unit = new BloodUnit(
                        rs.getInt("unit_id"),
                        rs.getInt("donation_id"),
                        rs.getString("blood_group"),
                        rs.getString("product_type"),
                        rs.getDate("collection_date").toLocalDate(),
                        rs.getDate("expiry_date").toLocalDate(),
                        rs.getString("status")
                );

                units.add(unit);
            }

        } catch (SQLException e) {
            System.out.println(
                    "Failed to fetch blood units: " + e.getMessage()
            );
        }

        return units;
    }

public List<BloodUnit> getAvailableUnits() {

    List<BloodUnit> units = new ArrayList<>();

    String sql = """
            SELECT *
            FROM blood_units
            WHERE status = 'AVAILABLE'
              AND expiry_date >= CURRENT_DATE
            ORDER BY expiry_date ASC
            """;

    try (
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
    ) {
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

    } catch (SQLException e) {
        System.out.println("Failed to fetch available units: " + e.getMessage());
    }

    return units;
}public int addBloodUnit(
        BloodUnit unit,
        Connection connection
) throws SQLException {

    String sql = """
            INSERT INTO blood_units
            (donation_id, blood_group, product_type,
             collection_date, expiry_date, status)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

    try (
            PreparedStatement ps = connection.prepareStatement(
                    sql,
                    java.sql.Statement.RETURN_GENERATED_KEYS
            )
    ) {
        ps.setInt(1, unit.getDonationId());
        ps.setString(2, unit.getBloodGroup());
        ps.setString(3, unit.getProductType());
        ps.setDate(4, Date.valueOf(unit.getCollectionDate()));
        ps.setDate(5, Date.valueOf(unit.getExpiryDate()));
        ps.setString(6, unit.getStatus());

        if (ps.executeUpdate() == 0) {
            return -1;
        }

        try (ResultSet keys = ps.getGeneratedKeys()) {
            if (keys.next()) {
                return keys.getInt(1);
            }
        }
    }

    return -1;
}

public List<BloodUnit> getUnitsByBloodGroup(String bloodGroup) {

    List<BloodUnit> units = new ArrayList<>();

    String sql = """
            SELECT *
            FROM blood_units
            WHERE blood_group = ?
            ORDER BY expiry_date ASC
            """;

    try (
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)
    ) {
        ps.setString(1, bloodGroup);

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
        System.out.println("Failed to filter blood units: " + e.getMessage());
    }

    return units;
}

}
