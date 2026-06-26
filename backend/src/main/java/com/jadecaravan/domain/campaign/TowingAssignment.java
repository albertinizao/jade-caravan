package com.jadecaravan.domain.campaign;

import java.util.UUID;

public record TowingAssignment(
        UUID beastId,
        UUID cartId,
        UUID campaignDayId) {

    public TowingAssignment {
        DomainValidation.requireNonNull(beastId, "beastId");
        DomainValidation.requireNonNull(cartId, "cartId");
        DomainValidation.requireNonNull(campaignDayId, "campaignDayId");
    }
}
