package com.bloodbank.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PatientForm {

    @NotBlank(message = "Patient full name is required.")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters.")
    private String name;

    @NotNull(message = "Age is required.")
    @Min(value = 1, message = "Patient age must be at least 1.")
    @Max(value = 120, message = "Patient age cannot exceed 120.")
    private Integer age;

    @NotBlank(message = "Blood group is required.")
    @Pattern(regexp = "^(A|B|AB|O)[+-]$", message = "Please select a valid blood group (A+, A-, B+, B-, AB+, AB-, O+, O-).")
    private String bloodGroup;

    @NotBlank(message = "Hospital name is required.")
    @Size(min = 2, max = 100, message = "Hospital name must be between 2 and 100 characters.")
    private String hospital;

    @NotBlank(message = "Contact phone number is required.")
    @Pattern(regexp = "^[0-9+()\\-\\s]{7,20}$", message = "Please enter a valid contact phone number (at least 7 digits).")
    private String phone;

    public PatientForm() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getHospital() {
        return hospital;
    }

    public void setHospital(String hospital) {
        this.hospital = hospital;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
