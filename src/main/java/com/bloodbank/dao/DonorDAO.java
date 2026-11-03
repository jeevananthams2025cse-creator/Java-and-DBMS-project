package com.bloodbank.dao;

import com.bloodbank.model.Donor;
import com.bloodbank.util.DatabaseConnection;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Date;
import java.sql.SQLException;

public class DonorDAO {

    public record DonorStats(int donationCount, int unitCount, int availableUnitCount) {}

    public void addDonor(Donor donor) {

        String sql = """
                INSERT INTO donors
                (name, age, blood_group, phone, last_donation_date)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {

            ps.setString(1, donor.getName());
            ps.setInt(2, donor.getAge());
            ps.setString(3, donor.getBloodGroup());
            ps.setString(4, donor.getPhone());

            if (donor.getLastDonationDate() != null) {
                ps.setDate(
                        5,
                        Date.valueOf(donor.getLastDonationDate())
                );
            } else {
                ps.setNull(5, java.sql.Types.DATE);
            }

            ps.executeUpdate();

            System.out.println("Donor added successfully!");

        } catch (Exception e) {

            System.out.println("Failed to add donor.");
            e.printStackTrace();
        }
    }

    public List<Donor> getAllDonors() {

        List<Donor> donors = new ArrayList<>();

        String sql = "SELECT * FROM donors";

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql);
                var rs = ps.executeQuery()
        ) {

            while (rs.next()) {

                int id = rs.getInt("donor_id");
                String name = rs.getString("name");
                int age = rs.getInt("age");
                String bloodGroup = rs.getString("blood_group");
                String phone = rs.getString("phone");

                Date lastDonationSql =
                        rs.getDate("last_donation_date");

                java.time.LocalDate lastDonationDate =
                        lastDonationSql != null
                                ? lastDonationSql.toLocalDate()
                                : null;

                Donor donor = new Donor(
                        id,
                        name,
                        age,
                        phone,
                        bloodGroup,
                        lastDonationDate
                );

                donors.add(donor);
            }

        } catch (Exception e) {

            System.out.println("Failed to fetch donors.");
            e.printStackTrace();
        }

        return donors;
    }

    public Donor getDonorById(int donorId) {

        String sql = "SELECT * FROM donors WHERE donor_id = ?";

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {

            ps.setInt(1, donorId);

            try (var rs = ps.executeQuery()) {

                if (rs.next()) {

                    int id = rs.getInt("donor_id");
                    String name = rs.getString("name");
                    int age = rs.getInt("age");
                    String bloodGroup = rs.getString("blood_group");
                    String phone = rs.getString("phone");

                    Date lastDonationSql =
                            rs.getDate("last_donation_date");

                    java.time.LocalDate lastDonationDate =
                            lastDonationSql != null
                                    ? lastDonationSql.toLocalDate()
                                    : null;

                    return new Donor(
                            id,
                            name,
                            age,
                            phone,
                            bloodGroup,
                            lastDonationDate
                    );
                }
            }

        } catch (Exception e) {

            System.out.println("Failed to find donor.");
            e.printStackTrace();
        }

        return null;
    }

    public void updateDonor(Donor donor) {

        String sql = """
                UPDATE donors
                SET name = ?,
                    age = ?,
                    blood_group = ?,
                    phone = ?,
                    last_donation_date = ?
                WHERE donor_id = ?
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setString(1, donor.getName());
            ps.setInt(2, donor.getAge());
            ps.setString(3, donor.getBloodGroup());
            ps.setString(4, donor.getPhone());

            if (donor.getLastDonationDate() != null) {
                ps.setDate(5, Date.valueOf(donor.getLastDonationDate()));
            } else {
                ps.setNull(5, java.sql.Types.DATE);
            }

            ps.setInt(6, donor.getId());

            int rowsUpdated = ps.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Donor updated successfully!");
            } else {
                System.out.println("Donor not found.");
            }

        } catch (Exception e) {
            System.out.println("Failed to update donor.");
            e.printStackTrace();
        }
    }

    public void updateDonor(Donor donor, Connection connection) throws SQLException {
        String sql = """
                UPDATE donors
                SET name = ?,
                    age = ?,
                    blood_group = ?,
                    phone = ?,
                    last_donation_date = ?
                WHERE donor_id = ?
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, donor.getName());
            ps.setInt(2, donor.getAge());
            ps.setString(3, donor.getBloodGroup());
            ps.setString(4, donor.getPhone());

            if (donor.getLastDonationDate() != null) {
                ps.setDate(5, Date.valueOf(donor.getLastDonationDate()));
            } else {
                ps.setNull(5, java.sql.Types.DATE);
            }

            ps.setInt(6, donor.getId());
            ps.executeUpdate();
        }
    }

    public boolean updateDonorWithCascade(Donor donor) {
        String updateDonorSql = """
                UPDATE donors
                SET name = ?,
                    age = ?,
                    blood_group = ?,
                    phone = ?,
                    last_donation_date = ?
                WHERE donor_id = ?
                """;

        String cascadeUnitsSql = """
                UPDATE blood_units bu
                JOIN donations d ON bu.donation_id = d.donation_id
                SET bu.blood_group = ?
                WHERE d.donor_id = ? AND bu.status = 'AVAILABLE'
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                // 1. Update the donor record
                try (PreparedStatement ps = connection.prepareStatement(updateDonorSql)) {
                    ps.setString(1, donor.getName());
                    ps.setInt(2, donor.getAge());
                    ps.setString(3, donor.getBloodGroup());
                    ps.setString(4, donor.getPhone());

                    if (donor.getLastDonationDate() != null) {
                        ps.setDate(5, Date.valueOf(donor.getLastDonationDate()));
                    } else {
                        ps.setNull(5, java.sql.Types.DATE);
                    }

                    ps.setInt(6, donor.getId());

                    int rows = ps.executeUpdate();
                    if (rows == 0) {
                        throw new SQLException("Donor #" + donor.getId() + " not found.");
                    }
                }

                // 2. Cascade updated blood group to any non-issued available units linked to this donor
                try (PreparedStatement ps = connection.prepareStatement(cascadeUnitsSql)) {
                    ps.setString(1, donor.getBloodGroup());
                    ps.setInt(2, donor.getId());
                    ps.executeUpdate();
                }

                connection.commit();
                return true;

            } catch (SQLException e) {
                connection.rollback();
                System.out.println("Failed to update donor with cascade: " + e.getMessage());
                return false;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
            return false;
        }
    }

    public void deleteDonor(int donorId) {
        deleteDonorWithCascade(donorId);
    }

    public boolean deleteDonorWithCascade(int donorId) {
        String deleteAvailableUnitsSql = """
                DELETE bu FROM blood_units bu
                JOIN donations d ON bu.donation_id = d.donation_id
                WHERE d.donor_id = ? AND bu.status = 'AVAILABLE'
                """;

        String unlinkIssuedUnitsSql = """
                UPDATE blood_units bu
                JOIN donations d ON bu.donation_id = d.donation_id
                SET bu.donation_id = NULL
                WHERE d.donor_id = ?
                """;

        String deleteDonationsSql = "DELETE FROM donations WHERE donor_id = ?";
        String deleteDonorSql = "DELETE FROM donors WHERE donor_id = ?";

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                // 1. Delete available un-issued units
                try (PreparedStatement ps = connection.prepareStatement(deleteAvailableUnitsSql)) {
                    ps.setInt(1, donorId);
                    ps.executeUpdate();
                }

                // 2. Unlink any issued units so historic issue logs remain valid
                try (PreparedStatement ps = connection.prepareStatement(unlinkIssuedUnitsSql)) {
                    ps.setInt(1, donorId);
                    ps.executeUpdate();
                }

                // 3. Delete donations
                try (PreparedStatement ps = connection.prepareStatement(deleteDonationsSql)) {
                    ps.setInt(1, donorId);
                    ps.executeUpdate();
                }

                // 4. Delete donor
                int rows;
                try (PreparedStatement ps = connection.prepareStatement(deleteDonorSql)) {
                    ps.setInt(1, donorId);
                    rows = ps.executeUpdate();
                }

                if (rows == 0) {
                    throw new SQLException("Donor #" + donorId + " not found.");
                }

                connection.commit();
                System.out.println("Donor #" + donorId + " and linked data deleted successfully.");
                return true;

            } catch (SQLException e) {
                connection.rollback();
                System.out.println("Failed to delete donor with cascade: " + e.getMessage());
                return false;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
            return false;
        }
    }

    public DonorStats getDonorStats(int donorId) {
        String sql = """
                SELECT
                    (SELECT COUNT(*) FROM donations WHERE donor_id = ?) AS donation_count,
                    (SELECT COUNT(*) FROM blood_units bu JOIN donations d ON bu.donation_id = d.donation_id WHERE d.donor_id = ?) AS unit_count,
                    (SELECT COUNT(*) FROM blood_units bu JOIN donations d ON bu.donation_id = d.donation_id WHERE d.donor_id = ? AND bu.status = 'AVAILABLE') AS available_unit_count
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setInt(1, donorId);
            ps.setInt(2, donorId);
            ps.setInt(3, donorId);

            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new DonorStats(
                            rs.getInt("donation_count"),
                            rs.getInt("unit_count"),
                            rs.getInt("available_unit_count")
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to fetch donor stats: " + e.getMessage());
        }

        return new DonorStats(0, 0, 0);
    }

    public List<Donor> getDonors(String search, String bloodGroup, int page, int pageSize) {
        List<Donor> donors = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM donors WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (search != null && !search.isBlank()) {
            sql.append(" AND (LOWER(name) LIKE ? OR phone LIKE ? OR CAST(donor_id AS CHAR) LIKE ?)");
            String searchParam = "%" + search.trim().toLowerCase().replace("#d-", "").replace("d-", "") + "%";
            params.add(searchParam);
            params.add(searchParam);
            params.add(searchParam);
        }

        if (bloodGroup != null && !bloodGroup.isBlank() && !bloodGroup.equalsIgnoreCase("ALL")) {
            sql.append(" AND blood_group = ?");
            params.add(bloodGroup.trim().toUpperCase());
        }

        sql.append(" ORDER BY donor_id DESC LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add(Math.max(0, (page - 1) * pageSize));

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql.toString())
        ) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date lastDonationSql = rs.getDate("last_donation_date");
                    java.time.LocalDate lastDonationDate =
                            lastDonationSql != null ? lastDonationSql.toLocalDate() : null;

                    donors.add(new Donor(
                            rs.getInt("donor_id"),
                            rs.getString("name"),
                            rs.getInt("age"),
                            rs.getString("phone"),
                            rs.getString("blood_group"),
                            lastDonationDate
                    ));
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to fetch filtered donors: " + e.getMessage());
        }

        return donors;
    }

    public int countDonors(String search, String bloodGroup) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM donors WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (search != null && !search.isBlank()) {
            sql.append(" AND (LOWER(name) LIKE ? OR phone LIKE ? OR CAST(donor_id AS CHAR) LIKE ?)");
            String searchParam = "%" + search.trim().toLowerCase().replace("#d-", "").replace("d-", "") + "%";
            params.add(searchParam);
            params.add(searchParam);
            params.add(searchParam);
        }

        if (bloodGroup != null && !bloodGroup.isBlank() && !bloodGroup.equalsIgnoreCase("ALL")) {
            sql.append(" AND blood_group = ?");
            params.add(bloodGroup.trim().toUpperCase());
        }

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql.toString())
        ) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to count donors: " + e.getMessage());
        }

        return 0;
    }
}
