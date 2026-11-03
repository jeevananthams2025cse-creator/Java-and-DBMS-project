package com.bloodbank.service;

import com.bloodbank.dao.PatientDAO;
import com.bloodbank.dto.PatientForm;
import com.bloodbank.model.Patient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientService {

    private final PatientDAO patientDAO = new PatientDAO();

    public List<Patient> getPatients(String search, int page, int pageSize) {
        return patientDAO.getPatients(search, page, pageSize);
    }

    public int countPatients(String search) {
        return patientDAO.countPatients(search);
    }

    public List<Patient> getAllPatients() {
        return patientDAO.getAllPatients();
    }

    public Patient getPatientById(int id) {
        return patientDAO.getPatientById(id);
    }

    public boolean registerPatient(PatientForm form) {
        Patient patient = new Patient(
                0,
                form.getName().trim(),
                form.getAge(),
                form.getPhone().trim(),
                form.getBloodGroup().trim().toUpperCase(),
                form.getHospital().trim()
        );

        return patientDAO.addPatient(patient);
    }

    public boolean updatePatient(int id, PatientForm form) {
        Patient existing = patientDAO.getPatientById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Patient with ID #" + id + " does not exist.");
        }

        Patient patient = new Patient(
                id,
                form.getName().trim(),
                form.getAge(),
                form.getPhone().trim(),
                form.getBloodGroup().trim().toUpperCase(),
                form.getHospital().trim()
        );

        return patientDAO.updatePatientWithCascade(patient);
    }

    public boolean deletePatient(int id) {
        Patient existing = patientDAO.getPatientById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Patient with ID #" + id + " does not exist.");
        }
        return patientDAO.deletePatientWithCascade(id);
    }

    public PatientDAO.PatientStats getPatientStats(int id) {
        return patientDAO.getPatientStats(id);
    }
}
