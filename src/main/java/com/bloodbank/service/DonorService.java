package com.bloodbank.service;

import com.bloodbank.dao.DonorDAO;
import com.bloodbank.dto.DonorForm;
import com.bloodbank.model.Donor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class DonorService {

    private final DonorDAO donorDAO = new DonorDAO();

    public List<Donor> getDonors(String search, String bloodGroup, int page, int pageSize) {
        return donorDAO.getDonors(search, bloodGroup, page, pageSize);
    }

    public int countDonors(String search, String bloodGroup) {
        return donorDAO.countDonors(search, bloodGroup);
    }

    public List<Donor> getAllDonors() {
        return donorDAO.getAllDonors();
    }

    public Donor getDonorById(int id) {
        return donorDAO.getDonorById(id);
    }

    public boolean registerDonor(DonorForm form) {
        if (form.getLastDonationDate() != null && form.getLastDonationDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Last donation date cannot be in the future.");
        }

        Donor donor = new Donor(
                0,
                form.getName().trim(),
                form.getAge(),
                form.getPhone().trim(),
                form.getBloodGroup().trim().toUpperCase(),
                form.getLastDonationDate()
        );

        donorDAO.addDonor(donor);
        return true;
    }

    public boolean updateDonor(int id, DonorForm form) {
        if (form.getLastDonationDate() != null && form.getLastDonationDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Last donation date cannot be in the future.");
        }

        Donor existing = donorDAO.getDonorById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Donor with ID #" + id + " does not exist.");
        }

        Donor donor = new Donor(
                id,
                form.getName().trim(),
                form.getAge(),
                form.getPhone().trim(),
                form.getBloodGroup().trim().toUpperCase(),
                form.getLastDonationDate()
        );

        return donorDAO.updateDonorWithCascade(donor);
    }

    public boolean deleteDonor(int id) {
        Donor existing = donorDAO.getDonorById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Donor with ID #" + id + " does not exist.");
        }
        return donorDAO.deleteDonorWithCascade(id);
    }

    public DonorDAO.DonorStats getDonorStats(int id) {
        return donorDAO.getDonorStats(id);
    }
}
