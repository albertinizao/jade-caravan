package com.jadecaravan.domain.campaign;

import java.math.BigDecimal;
import java.util.UUID;

public record CartPassengerAssignment(
        UUID cartId,
        UUID travellerId,
        BigDecimal occupancyUnits,
        String notes) {

    public CartPassengerAssignment {
        DomainValidation.requireNonNull(cartId, "cartId");
        DomainValidation.requireNonNull(travellerId, "travellerId");
        DomainValidation.requireNonNegative(occupancyUnits, "occupancyUnits");
    }
}
