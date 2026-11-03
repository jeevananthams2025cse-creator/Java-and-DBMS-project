package com.bloodbank.model;

import java.time.LocalDate;

public abstract class BloodProduct {

    private int productId;
    private String bloodGroup;
    private LocalDate collectionDate;

    public BloodProduct(
            int productId,
            String bloodGroup,
            LocalDate collectionDate) {

        this.productId = productId;
        this.bloodGroup = bloodGroup;
        this.collectionDate = collectionDate;
    }

    public int getProductId() {
        return productId;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public LocalDate getCollectionDate() {
        return collectionDate;
    }

    // Every blood product must provide its own shelf life
    public abstract int shelfLifeDays();
}