package com.jadecaravan.domain.campaign;

import java.math.BigDecimal;
import java.util.UUID;

public record CartCargoAllocation(
        UUID cartId,
        UUID inventoryLotId,
        BigDecimal quantity,
        String notes) {

    public CartCargoAllocation {
        DomainValidation.requireNonNull(cartId, "cartId");
        DomainValidation.requireNonNull(inventoryLotId, "inventoryLotId");
        DomainValidation.requireNonNegative(quantity, "quantity");
    }
}
