package com.jadecaravan.domain.campaign;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public record InventoryLot(
        UUID id,
        UUID caravanId,
        String cargoTypeId,
        BigDecimal quantity,
        BigDecimal unitCapacity,
        long unitValueCp,
        UUID cartId,
        UUID originSettlementId,
        BigDecimal remainingProvisions,
        BigDecimal perishableDecayProgress,
        Map<String, String> metadata) {

    public InventoryLot {
        DomainValidation.requireNonNull(id, "id");
        DomainValidation.requireNonNull(caravanId, "caravanId");
        DomainValidation.requireNonBlank(cargoTypeId, "cargoTypeId");
        DomainValidation.requireNonNegative(quantity, "quantity");
        DomainValidation.requireNonNegative(unitCapacity, "unitCapacity");
        DomainValidation.requireNonNegative(unitValueCp, "unitValueCp");
        if (remainingProvisions != null) {
            DomainValidation.requireNonNegative(remainingProvisions, "remainingProvisions");
        }
        if (perishableDecayProgress != null) {
            DomainValidation.requireNonNegative(perishableDecayProgress, "perishableDecayProgress");
        }
        metadata = DomainCollections.immutableCopy(metadata);
    }

    public InventoryLot withQuantity(BigDecimal newQuantity) {
        return new InventoryLot(
                id,
                caravanId,
                cargoTypeId,
                newQuantity,
                unitCapacity,
                unitValueCp,
                cartId,
                originSettlementId,
                remainingProvisions,
                perishableDecayProgress,
                metadata);
    }

    public InventoryLot withRemainingProvisions(BigDecimal newRemainingProvisions) {
        return new InventoryLot(
                id,
                caravanId,
                cargoTypeId,
                quantity,
                unitCapacity,
                unitValueCp,
                cartId,
                originSettlementId,
                newRemainingProvisions,
                perishableDecayProgress,
                metadata);
    }

    public InventoryLot withPerishableDecayProgress(BigDecimal newPerishableDecayProgress) {
        return new InventoryLot(
                id,
                caravanId,
                cargoTypeId,
                quantity,
                unitCapacity,
                unitValueCp,
                cartId,
                originSettlementId,
                remainingProvisions,
                newPerishableDecayProgress,
                metadata);
    }
}
