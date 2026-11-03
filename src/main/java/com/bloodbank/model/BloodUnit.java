package com.bloodbank.model;

import java.time.LocalDate;

public class BloodUnit {

    private int unitId;
    private int donationId;
    private String bloodGroup;
    private String productType;
    private LocalDate collectionDate;
    private LocalDate expiryDate;
    private String status;

    public BloodUnit(
            int unitId,
            int donationId,
            String bloodGroup,
            String productType,
            LocalDate collectionDate,
            LocalDate expiryDate,
            String status) {

        this.unitId = unitId;
        this.donationId = donationId;
        this.bloodGroup = bloodGroup;
        this.productType = productType;
        this.collectionDate = collectionDate;
        this.expiryDate = expiryDate;
        this.status = status;
    }

    public int getUnitId() {
        return unitId;
    }

    public int getDonationId() {
        return donationId;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public String getProductType() {
        return productType;
    }

    public LocalDate getCollectionDate() {
        return collectionDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public String getStatus() {
        return status;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}