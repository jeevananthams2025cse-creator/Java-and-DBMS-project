package com.bloodbank.model;

import java.time.LocalDate;

public class Plasma extends BloodProduct {

    public Plasma(
            int productId,
            String bloodGroup,
            LocalDate collectionDate) {

        super(productId, bloodGroup, collectionDate);
    }

    @Override
    public int shelfLifeDays() {
        return 365;
    }
}