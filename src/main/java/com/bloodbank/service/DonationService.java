package com.bloodbank.service;

import com.bloodbank.dao.BloodUnitDAO;
import com.bloodbank.dao.DonationDAO;
import com.bloodbank.dao.DonorDAO;
import com.bloodbank.model.BloodUnit;
import com.bloodbank.model.Donation;
import com.bloodbank.model.Donor;
import com.bloodbank.util.DatabaseConnection;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class DonationService {

    private final DonationDAO donationDAO = new DonationDAO();
    private final BloodUnitDAO bloodUnitDAO = new BloodUnitDAO();
    private final DonorDAO donorDAO = new DonorDAO();

    public DonationService() {
    }

    public boolean recordDonation(Donation donation) {

        Donor donor = this.donorDAO.getDonorById(donation.getDonorId());

        if (donor == null) {
            System.out.println("Donor ID not found.");
            throw new IllegalArgumentException("Donor ID " + donation.getDonorId() + " not found.");
        }

        if (!"WHOLE_BLOOD".equalsIgnoreCase(donation.getProductType())) {
            System.out.println("Only WHOLE_BLOOD is currently supported.");
            throw new IllegalArgumentException("Only WHOLE_BLOOD is currently supported.");
        }

        if (donation.getQuantity() != 1) {
            System.out.println("Quantity must be 1 for this workflow.");
            throw new IllegalArgumentException("Quantity must be 1 for this workflow.");
        }

        if (donation.getDonationDate().isAfter(LocalDate.now())) {
            System.out.println("Donation date cannot be in the future.");
            throw new IllegalArgumentException("Donation date cannot be in the future.");
        }

        // Enforce 90-day minimum gap check between donations
        if (donor.getLastDonationDate() != null) {
            long daysSinceLastDonation = ChronoUnit.DAYS.between(donor.getLastDonationDate(), donation.getDonationDate());
            if (daysSinceLastDonation < 90L) {
                String errorMsg = "Donor is not eligible. Minimum 90 days required between donations. Last donation was " 
                        + daysSinceLastDonation + " day(s) ago on " + donor.getLastDonationDate() + ".";
                System.out.println(errorMsg);
                throw new IllegalStateException(errorMsg);
            }
        }

        try (Connection connection = DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                // 1. Insert donation using the shared connection.
                int donationId = this.donationDAO.addDonation(donation, connection);

                if (donationId == -1) {
                    throw new SQLException("Could not save donation.");
                }

                // 2. Prepare linked inventory unit (whole blood shelf life: 31 days).
                LocalDate collectionDate = donation.getDonationDate();
                LocalDate expiryDate = collectionDate.plusDays(31L);

                BloodUnit unit = new BloodUnit(
                        0,
                        donationId,
                        donor.getBloodGroup(),
                        donation.getProductType(),
                        collectionDate,
                        expiryDate,
                        "AVAILABLE"
                );

                // 3. Insert inventory unit using the same connection.
                int unitId = this.bloodUnitDAO.addBloodUnit(unit, connection);

                if (unitId == -1) {
                    throw new SQLException("Could not create blood unit.");
                }

                // 4. Update donor's last donation date using the SAME transaction connection!
                donor.setLastDonationDate(collectionDate);
                this.donorDAO.updateDonor(donor, connection);

                // All operations succeeded. Commit transaction atomically.
                connection.commit();

                System.out.println("Donation and inventory saved successfully!");
                System.out.println("Donation ID: " + donationId);
                System.out.println("Blood Unit ID: " + unitId);

                return true;

            } catch (SQLException | RuntimeException e) {

                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }

                System.out.println("Transaction failed; changes rolled back: " + e.getMessage());

                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new RuntimeException("Transaction failed: " + e.getMessage(), e);

            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    System.out.println("Could not restore auto-commit: " + e.getMessage());
                }
            }

        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    public List<DonationDAO.DonationRecord> getDonations(LocalDate fromDate, LocalDate toDate, String bloodGroup, int page, int pageSize) {
        return this.donationDAO.getDonationRecords(fromDate, toDate, bloodGroup, page, pageSize);
    }

    public int countDonations(LocalDate fromDate, LocalDate toDate, String bloodGroup) {
        return this.donationDAO.countDonationRecords(fromDate, toDate, bloodGroup);
    }
}
