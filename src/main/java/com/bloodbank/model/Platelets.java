package com.bloodbank.model;

import java.time.LocalDate;

public class Platelets extends BloodProduct {

    public Platelets(
            int productId,
            String bloodGroup,
            LocalDate collectionDate) {

        super(productId, bloodGroup, collectionDate);
    }

    @Override
    public int shelfLifeDays() {
        return 5;
    }
}