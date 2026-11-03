package com.bloodbank.model;

import java.time.LocalDate;

public class Donation {

    private int donationId;
    private int donorId;
    private LocalDate donationDate;
    private String productType;
    private int quantity;

    public Donation(
            int donationId,
            int donorId,
            LocalDate donationDate,
            String productType,
            int quantity) {

        this.donationId = donationId;
        this.donorId = donorId;
        this.donationDate = donationDate;
        this.productType = productType;
        this.quantity = quantity;
    }

    public int getDonationId() {
        return donationId;
    }

    public int getDonorId() {
        return donorId;
    }

    public LocalDate getDonationDate() {
        return donationDate;
    }

    public String getProductType() {
        return productType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setDonationDate(LocalDate donationDate) {
        this.donationDate = donationDate;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}