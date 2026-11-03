package com.bloodbank.model;

import java.time.LocalDate;

public class Donor extends Person {

    private String bloodGroup;
    private LocalDate lastDonationDate;

    public Donor(
            int id,
            String name,
            int age,
            String phone,
            String bloodGroup,
            LocalDate lastDonationDate) {

        super(id, name, age, phone);

        this.bloodGroup = bloodGroup;
        this.lastDonationDate = lastDonationDate;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public LocalDate getLastDonationDate() {
        return lastDonationDate;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public void setLastDonationDate(LocalDate lastDonationDate) {
        this.lastDonationDate = lastDonationDate;
    }
}