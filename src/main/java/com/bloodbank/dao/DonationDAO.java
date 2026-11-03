package com.bloodbank.dao;

import com.bloodbank.model.Donation;
import com.bloodbank.util.DatabaseConnection;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DonationDAO {

    public record DonationRecord(
            int donationId,
            int donorId,
            String donorName,
            String bloodGroup,
            LocalDate donationDate,
            String productType,
            int quantity
    ) {}

    // Record a new donation
    public boolean addDonation(Donation donation) {

        String sql = """
                INSERT INTO donations
                (donor_id, donation_date, product_type, quantity)
                VALUES (?, ?, ?, ?)
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, donation.getDonorId());
            ps.setDate(2, Date.valueOf(donation.getDonationDate()));
            ps.setString(3, donation.getProductType());
            ps.setInt(4, donation.getQuantity());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println(
                    "Failed to add donation: " + e.getMessage()
            );
            return false;
        }
    }

    // Record a donation and return its generated ID
    public int addDonationAndGetId(Donation donation) {

        String sql = """
                INSERT INTO donations
                (donor_id, donation_date, product_type, quantity)
                VALUES (?, ?, ?, ?)
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(
                        sql,
                        java.sql.Statement.RETURN_GENERATED_KEYS
                )
        ) {
            ps.setInt(1, donation.getDonorId());
            ps.setDate(2, Date.valueOf(donation.getDonationDate()));
            ps.setString(3, donation.getProductType());
            ps.setInt(4, donation.getQuantity());

            int rowsInserted = ps.executeUpdate();

            if (rowsInserted == 0) {
                return -1;
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.out.println(
                    "Failed to add donation: " + e.getMessage()
            );
        }

        return -1;
    }

    // Retrieve all donations
    public List<Donation> getAllDonations() {

        List<Donation> donations = new ArrayList<>();

        String sql = "SELECT * FROM donations ORDER BY donation_id";

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {

                Donation donation = new Donation(
                        rs.getInt("donation_id"),
                        rs.getInt("donor_id"),
                        rs.getDate("donation_date").toLocalDate(),
                        rs.getString("product_type"),
                        rs.getInt("quantity")
                );

                donations.add(donation);
            }

        } catch (SQLException e) {
            System.out.println(
                    "Failed to fetch donations: " + e.getMessage()
            );
        }

        return donations;
    }

    public List<Donation> getDonationsByDonorId(int donorId) {

        List<Donation> donations = new ArrayList<>();

        String sql = """
                SELECT *
                FROM donations
                WHERE donor_id = ?
                ORDER BY donation_date DESC
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, donorId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    Donation donation = new Donation(
                            rs.getInt("donation_id"),
                            rs.getInt("donor_id"),
                            rs.getDate("donation_date").toLocalDate(),
                            rs.getString("product_type"),
                            rs.getInt("quantity")
                    );

                    donations.add(donation);
                }
            }

        } catch (SQLException e) {
            System.out.println(
                    "Failed to fetch donor donation history: " + e.getMessage()
            );
        }

        return donations;
    }

    public int addDonation(
            Donation donation,
            Connection connection
    ) throws SQLException {

        String sql = """
                INSERT INTO donations
                (donor_id, donation_date, product_type, quantity)
                VALUES (?, ?, ?, ?)
                """;

        try (
                PreparedStatement ps = connection.prepareStatement(
                        sql,
                        Statement.RETURN_GENERATED_KEYS
                )
        ) {
            ps.setInt(1, donation.getDonorId());
            ps.setDate(2, Date.valueOf(donation.getDonationDate()));
            ps.setString(3, donation.getProductType());
            ps.setInt(4, donation.getQuantity());

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

    public List<DonationRecord> getDonationRecords(LocalDate fromDate, LocalDate toDate, String bloodGroup, int page, int pageSize) {
        List<DonationRecord> records = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                SELECT d.donation_id, d.donor_id, dn.name AS donor_name, dn.blood_group,
                       d.donation_date, d.product_type, d.quantity
                FROM donations d
                JOIN donors dn ON d.donor_id = dn.donor_id
                WHERE 1=1
                """);
        List<Object> params = new ArrayList<>();

        if (fromDate != null) {
            sql.append(" AND d.donation_date >= ?");
            params.add(Date.valueOf(fromDate));
        }

        if (toDate != null) {
            sql.append(" AND d.donation_date <= ?");
            params.add(Date.valueOf(toDate));
        }

        if (bloodGroup != null && !bloodGroup.isBlank() && !bloodGroup.equalsIgnoreCase("ALL")) {
            sql.append(" AND dn.blood_group = ?");
            params.add(bloodGroup.trim().toUpperCase());
        }

        sql.append(" ORDER BY d.donation_date DESC, d.donation_id DESC LIMIT ? OFFSET ?");
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
                    records.add(new DonationRecord(
                            rs.getInt("donation_id"),
                            rs.getInt("donor_id"),
                            rs.getString("donor_name"),
                            rs.getString("blood_group"),
                            rs.getDate("donation_date").toLocalDate(),
                            rs.getString("product_type"),
                            rs.getInt("quantity")
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to fetch donation records: " + e.getMessage());
        }

        return records;
    }

    public int countDonationRecords(LocalDate fromDate, LocalDate toDate, String bloodGroup) {
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*)
                FROM donations d
                JOIN donors dn ON d.donor_id = dn.donor_id
                WHERE 1=1
                """);
        List<Object> params = new ArrayList<>();

        if (fromDate != null) {
            sql.append(" AND d.donation_date >= ?");
            params.add(Date.valueOf(fromDate));
        }

        if (toDate != null) {
            sql.append(" AND d.donation_date <= ?");
            params.add(Date.valueOf(toDate));
        }

        if (bloodGroup != null && !bloodGroup.isBlank() && !bloodGroup.equalsIgnoreCase("ALL")) {
            sql.append(" AND dn.blood_group = ?");
            params.add(bloodGroup.trim().toUpperCase());
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
            System.out.println("Failed to count donation records: " + e.getMessage());
        }

        return 0;
    }
}
