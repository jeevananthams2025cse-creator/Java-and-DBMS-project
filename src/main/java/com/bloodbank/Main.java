
package com.bloodbank;

import com.bloodbank.dao.BloodUnitDAO;
import com.bloodbank.dao.DonationDAO;
import com.bloodbank.dao.DonorDAO;
import com.bloodbank.dao.PatientDAO;
import com.bloodbank.dao.BloodIssueDAO;
import com.bloodbank.model.BloodUnit;
import com.bloodbank.model.Donation;
import com.bloodbank.model.Donor;
import com.bloodbank.model.Patient;
import com.bloodbank.service.DonationService;
import com.bloodbank.service.RequestService;
import java.time.LocalDate;
import com.bloodbank.dao.BloodRequestDAO;
import com.bloodbank.model.BloodRequest;
import com.bloodbank.dao.ReportDAO;
import java.time.LocalDateTime;

import java.util.Scanner;


public class Main {
    private static final DonationDAO donationDAO = new DonationDAO();
private static final BloodUnitDAO bloodUnitDAO = new BloodUnitDAO();
    private static final Scanner scanner = new Scanner(System.in);
private static final DonationService donationService =new DonationService();
    private static final DonorDAO donorDAO = new DonorDAO();
    private static final BloodRequestDAO bloodRequestDAO =
        new BloodRequestDAO();
    private static final ReportDAO reportDAO = new ReportDAO();
    private static final PatientDAO patientDAO = new PatientDAO();
    private static final BloodIssueDAO bloodIssueDAO = new BloodIssueDAO();
    
    public static void main(String[] args) {

        boolean running = true;

        while (running) {
            System.out.println("\n==================================");
            System.out.println("   BLOOD BANK MANAGEMENT SYSTEM");
            System.out.println("==================================");
            System.out.println("1. Register Donor");
            System.out.println("2. View All Donors");
            System.out.println("3. Register Patient");
            System.out.println("4. View All Patients");
            System.out.println("5. Record Donation");
System.out.println("6. View Donation History");
System.out.println("7. View Blood Inventory");
System.out.println("8. View Available Blood Units");
System.out.println("9. Register Blood Request");
System.out.println("10. View All Blood Requests");
System.out.println("11. Fulfil Blood Request");
System.out.println("12. View Blood Issue History");
System.out.println("13. View Inventory Report");
System.out.println("14. View Emergency Request Report");
            System.out.println("0. Exit");
            System.out.println("==================================");

            int choice = readInt("Enter your choice: ");

            switch (choice) {
                case 1 -> registerDonor();
                case 2 -> viewDonors();
                case 3 -> registerPatient();
                case 4 -> viewPatients();
                case 5 -> recordDonation();
case 6 -> viewDonations();
case 7 -> viewInventory();
case 8 -> viewAvailableUnits();
case 9 -> registerBloodRequest();
case 10 -> viewBloodRequests();
case 11 -> fulfilBloodRequest();
case 12 -> viewBloodIssueHistory();
case 13 -> reportDAO.displayInventoryReport();
case 14 -> reportDAO.displayEmergencyRequestReport();
                case 0 -> {
                    running = false;
                    System.out.println("Thank you for using the system!");
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }

        scanner.close();
    }

    private static void registerDonor() {

        System.out.println("\n--- Register Donor ---");

        System.out.print("Name: ");
        String name = scanner.nextLine().trim();

        int age = readInt("Age: ");

        System.out.print("Phone: ");
        String phone = scanner.nextLine().trim();

        System.out.print("Blood group (e.g. A+): ");
        String bloodGroup = scanner.nextLine().trim().toUpperCase();

        System.out.print("Last donation date (YYYY-MM-DD), or leave blank: ");
        String dateInput = scanner.nextLine().trim();

        LocalDate lastDonationDate = null;

        try {
            if (!dateInput.isEmpty()) {
                lastDonationDate = LocalDate.parse(dateInput);
            }

            Donor donor = new Donor(
                    0, name, age, phone, bloodGroup, lastDonationDate
            );

            donorDAO.addDonor(donor);

        } catch (java.time.format.DateTimeParseException e) {
            System.out.println("Invalid date. Use YYYY-MM-DD.");
        }
    }

    private static void viewDonors() {

        System.out.println("\n--- Registered Donors ---");

        for (Donor donor : donorDAO.getAllDonors()) {
            System.out.println(
                    donor.getId() + " | "
                    + donor.getName() + " | Age: "
                    + donor.getAge() + " | "
                    + donor.getBloodGroup() + " | Phone: "
                    + donor.getPhone() + " | Last donation: "
                    + donor.getLastDonationDate()
            );
        }
    }
    private static final RequestService requestService =
        new RequestService();

private static void fulfilBloodRequest() {

    System.out.println("\n--- Fulfil Blood Request ---");

    int requestId = readInt("Enter pending request ID: ");

    boolean success = requestService.fulfilRequest(requestId);

    if (success) {
        System.out.println("Blood request processed successfully.");
    } else {
        System.out.println(
                "Blood request could not be fulfilled. "
                + "Check the message above."
        );
    }
}
private static void viewBloodIssueHistory() {
    System.out.println("\n--- Blood Issue History ---");

    int requestId = readInt("Enter blood request ID: ");

    bloodIssueDAO.displayIssuesForRequest(requestId);
}

    private static void registerPatient() {

        System.out.println("\n--- Register Patient ---");

        System.out.print("Name: ");
        String name = scanner.nextLine().trim();

        int age = readInt("Age: ");

        System.out.print("Phone: ");
        String phone = scanner.nextLine().trim();

        System.out.print("Blood group (e.g. A+): ");
        String bloodGroup = scanner.nextLine().trim().toUpperCase();

        System.out.print("Hospital: ");
        String hospital = scanner.nextLine().trim();

        Patient patient = new Patient(
                0, name, age, phone, bloodGroup, hospital
        );

        if (patientDAO.addPatient(patient)) {
            System.out.println("Patient registered successfully!");
        } else {
            System.out.println("Patient registration failed.");
        }
    }

    private static void viewPatients() {

        System.out.println("\n--- Registered Patients ---");

        for (Patient patient : patientDAO.getAllPatients()) {
            System.out.println(
                    patient.getId() + " | "
                    + patient.getName() + " | Age: "
                    + patient.getAge() + " | "
                    + patient.getBloodGroup() + " | Hospital: "
                    + patient.getHospital() + " | Phone: "
                    + patient.getPhone()
            );
        }
    }
    private static void registerBloodRequest() {

    System.out.println("\n--- Register Blood Request ---");

    int patientId = readInt("Enter existing patient ID: ");

    if (patientDAO.getPatientById(patientId) == null) {
        System.out.println("Patient ID not found. Register the patient first.");
        return;
    }

    System.out.print("Required blood group (e.g. A+): ");
    String bloodGroup = scanner.nextLine().trim().toUpperCase();

    System.out.print("Product type (WHOLE_BLOOD): ");
    String productType = scanner.nextLine().trim().toUpperCase();

    if (!productType.equals("WHOLE_BLOOD")) {
        System.out.println("Currently, only WHOLE_BLOOD is supported.");
        return;
    }

    int unitsRequired = readInt("Number of units required: ");

    if (unitsRequired <= 0) {
        System.out.println("Units required must be positive.");
        return;
    }

    System.out.print("Priority (NORMAL / URGENT / EMERGENCY): ");
    String priority = scanner.nextLine().trim().toUpperCase();

    if (!priority.equals("NORMAL")
            && !priority.equals("URGENT")
            && !priority.equals("EMERGENCY")) {
        System.out.println("Invalid priority.");
        return;
    }

    BloodRequest request = new BloodRequest(
            0,
            patientId,
            bloodGroup,
            productType,
            unitsRequired,
            priority,
            LocalDateTime.now(),
            "PENDING"
    );

    if (bloodRequestDAO.addRequest(request)) {
        System.out.println("Blood request registered successfully!");
    } else {
        System.out.println("Failed to register blood request.");
    }
}

private static void viewBloodRequests() {

    System.out.println("\n--- Blood Request History ---");

    for (BloodRequest request : bloodRequestDAO.getAllRequests()) {
        System.out.println(
                "Request ID: " + request.getRequestId()
                + " | Patient ID: " + request.getPatientId()
                + " | Blood Group: " + request.getBloodGroup()
                + " | Product: " + request.getProductType()
                + " | Units: " + request.getUnitsRequired()
                + " | Priority: " + request.getPriority()
                + " | Status: " + request.getStatus()
        );
    }
}

private static void recordDonation() {

    System.out.println("\n--- Record Donation ---");

    int donorId = readInt("Enter existing donor ID: ");

    if (donorDAO.getDonorById(donorId) == null) {
        System.out.println("Donor ID not found. Register the donor first.");
        return;
    }

    System.out.print("Donation date (YYYY-MM-DD): ");
    String dateInput = scanner.nextLine().trim();

    LocalDate donationDate;

    try {
        donationDate = LocalDate.parse(dateInput);

        if (donationDate.isAfter(LocalDate.now())) {
            System.out.println("Donation date cannot be in the future.");
            return;
        }

    } catch (java.time.format.DateTimeParseException e) {
        System.out.println("Invalid date. Use YYYY-MM-DD.");
        return;
    }

    System.out.print("Product type (WHOLE_BLOOD): ");
    String productType = scanner.nextLine().trim().toUpperCase();

    if (!productType.equals("WHOLE_BLOOD")) {
        System.out.println("Only WHOLE_BLOOD is currently supported.");
        return;
    }

    int quantity = readInt("Enter quantity (1): ");

    if (quantity != 1) {
        System.out.println("Quantity must be 1 for this workflow.");
        return;
    }

    Donation donation = new Donation(
            0,
            donorId,
            donationDate,
            productType,
            quantity
    );

    donationService.recordDonation(donation);
}

private static void viewDonations() {

    System.out.println("\n--- Donation History ---");

    for (Donation donation : donationDAO.getAllDonations()) {
        System.out.println(
                "Donation ID: " + donation.getDonationId()
                + " | Donor ID: " + donation.getDonorId()
                + " | Date: " + donation.getDonationDate()
                + " | Product: " + donation.getProductType()
                + " | Quantity: " + donation.getQuantity()
        );
    }
}

private static void viewInventory() {

    System.out.println("\n--- All Blood Units ---");

    for (BloodUnit unit : bloodUnitDAO.getAllBloodUnits()) {
        System.out.println(
                "Unit ID: " + unit.getUnitId()
                + " | Group: " + unit.getBloodGroup()
                + " | Product: " + unit.getProductType()
                + " | Expiry: " + unit.getExpiryDate()
                + " | Status: " + unit.getStatus()
        );
    }
}

private static void viewAvailableUnits() {

    System.out.println("\n--- Available, Non-Expired Units ---");

    for (BloodUnit unit : bloodUnitDAO.getAvailableUnits()) {
        System.out.println(
                "Unit ID: " + unit.getUnitId()
                + " | Group: " + unit.getBloodGroup()
                + " | Product: " + unit.getProductType()
                + " | Expiry: " + unit.getExpiryDate()
        );
    }
}


    private static int readInt(String prompt) {

        while (true) {
            System.out.print(prompt);

            try {
                if (!scanner.hasNextLine()) {
                    System.out.println("\nNo input available. Exiting.");
                    return 0;
                }

                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    System.out.println("Please enter a valid whole number.");
                    continue;
                }

                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number.");
            } catch (IllegalStateException | java.util.NoSuchElementException e) {
                System.out.println("\nInput stream is unavailable. Exiting.");
                return 0;
            }
        }
    }
}
