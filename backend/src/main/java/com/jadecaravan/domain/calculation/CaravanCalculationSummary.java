package com.jadecaravan.domain.calculation;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public record CaravanCalculationSummary(
        List<EffectiveCart> effectiveCarts,
        BigDecimal passengerCapacity,
        BigDecimal cargoCapacity,
        BigDecimal passengerOccupancy,
        BigDecimal cargoOccupancy,
        BigDecimal towingStrength,
        BigDecimal requiredTowingStrength,
        BigDecimal speedMilesPerDay,
        BigDecimal dailyConsumption,
        BigDecimal mutinyPenalty,
        List<CalculationBreakdownItem> breakdown,
        List<CalculationIssue> warnings,
        List<CalculationIssue> blockers,
        String ruleSetVersionId) {

    public CaravanCalculationSummary {
        Objects.requireNonNull(effectiveCarts, "effectiveCarts must not be null");
        Objects.requireNonNull(passengerCapacity, "passengerCapacity must not be null");
        Objects.requireNonNull(cargoCapacity, "cargoCapacity must not be null");
        Objects.requireNonNull(passengerOccupancy, "passengerOccupancy must not be null");
        Objects.requireNonNull(cargoOccupancy, "cargoOccupancy must not be null");
        Objects.requireNonNull(towingStrength, "towingStrength must not be null");
        Objects.requireNonNull(requiredTowingStrength, "requiredTowingStrength must not be null");
        Objects.requireNonNull(speedMilesPerDay, "speedMilesPerDay must not be null");
        Objects.requireNonNull(dailyConsumption, "dailyConsumption must not be null");
        Objects.requireNonNull(mutinyPenalty, "mutinyPenalty must not be null");
        Objects.requireNonNull(breakdown, "breakdown must not be null");
        Objects.requireNonNull(warnings, "warnings must not be null");
        Objects.requireNonNull(blockers, "blockers must not be null");
        Objects.requireNonNull(ruleSetVersionId, "ruleSetVersionId must not be null");
        effectiveCarts = List.copyOf(effectiveCarts);
        breakdown = List.copyOf(breakdown);
        warnings = List.copyOf(warnings);
        blockers = List.copyOf(blockers);
    }
}
