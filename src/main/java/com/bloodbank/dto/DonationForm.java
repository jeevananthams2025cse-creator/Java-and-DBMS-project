package com.bloodbank.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class DonationForm {

    @NotNull(message = "Please select a registered donor.")
    private Integer donorId;

    @NotNull(message = "Donation date is required.")
    @PastOrPresent(message = "Donation date cannot be in the future.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate donationDate = LocalDate.now();

    private String productType = "WHOLE_BLOOD";

    private Integer quantity = 1;

    public DonationForm() {
    }

    public DonationForm(Integer donorId) {
        this.donorId = donorId;
    }

    public Integer getDonorId() {
        return donorId;
    }

    public void setDonorId(Integer donorId) {
        this.donorId = donorId;
    }

    public LocalDate getDonationDate() {
        return donationDate;
    }

    public void setDonationDate(LocalDate donationDate) {
        this.donationDate = donationDate;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
