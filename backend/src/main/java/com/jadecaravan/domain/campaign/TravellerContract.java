package com.jadecaravan.domain.campaign;

import java.time.Instant;

public record TravellerContract(
        String contractType,
        long monthlyCostCp,
        boolean active,
        String notes,
        Instant signedAt) {

    public TravellerContract {
        DomainValidation.requireNonBlank(contractType, "contractType");
        DomainValidation.requireNonNegative(monthlyCostCp, "monthlyCostCp");
    }
}
