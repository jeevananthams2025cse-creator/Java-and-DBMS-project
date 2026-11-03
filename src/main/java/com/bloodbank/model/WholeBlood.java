package com.bloodbank.model;

import java.time.LocalDate;

public class WholeBlood extends BloodProduct {

    public WholeBlood(
            int productId,
            String bloodGroup,
            LocalDate collectionDate) {

        super(productId, bloodGroup, collectionDate);
    }

    @Override
    public int shelfLifeDays() {
        return 31;
    }
}