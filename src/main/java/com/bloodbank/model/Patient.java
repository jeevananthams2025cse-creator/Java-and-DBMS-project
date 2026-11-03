package com.bloodbank.model;

public class Patient extends Person {

    private String bloodGroup;
    private String hospital;

    public Patient(
            int id,
            String name,
            int age,
            String phone,
            String bloodGroup,
            String hospital) {

        super(id, name, age, phone);

        this.bloodGroup = bloodGroup;
        this.hospital = hospital;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public String getHospital() {
        return hospital;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public void setHospital(String hospital) {
        this.hospital = hospital;
    }
}