package com.jadecaravan.domain.calculation;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record EffectiveCart(
        UUID cartId,
        String cartName,
        int maxHitPoints,
        int hardness,
        int propulsionRequirement,
        int maxLargeTowedCreatures,
        int maxMediumTowedCreatures,
        BigDecimal consumption,
        BigDecimal passengerCapacity,
        BigDecimal cargoCapacity,
        List<CalculationBreakdownItem> breakdown,
        List<CalculationIssue> warnings,
        List<CalculationIssue> blockers,
        String ruleSetVersionId) {

    public EffectiveCart {
        Objects.requireNonNull(cartId, "cartId must not be null");
        Objects.requireNonNull(cartName, "cartName must not be null");
        Objects.requireNonNull(consumption, "consumption must not be null");
        Objects.requireNonNull(passengerCapacity, "passengerCapacity must not be null");
        Objects.requireNonNull(cargoCapacity, "cargoCapacity must not be null");
        Objects.requireNonNull(breakdown, "breakdown must not be null");
        Objects.requireNonNull(warnings, "warnings must not be null");
        Objects.requireNonNull(blockers, "blockers must not be null");
        Objects.requireNonNull(ruleSetVersionId, "ruleSetVersionId must not be null");
        breakdown = List.copyOf(breakdown);
        warnings = List.copyOf(warnings);
        blockers = List.copyOf(blockers);
    }
}
