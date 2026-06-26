package com.jadecaravan.domain.calculation;

import java.math.BigDecimal;
import java.util.List;

public record TravelValidationResult(
        boolean travelAllowed,
        BigDecimal passengerOccupancy,
        BigDecimal passengerCapacity,
        BigDecimal cargoOccupancy,
        BigDecimal cargoCapacity,
        BigDecimal towingStrength,
        BigDecimal requiredTowingStrength,
        List<CalculationBreakdownItem> breakdown,
        List<CalculationIssue> warnings,
        List<CalculationIssue> blockers,
        String ruleSetVersionId) {

    public TravelValidationResult {
        breakdown = List.copyOf(breakdown);
        warnings = List.copyOf(warnings);
        blockers = List.copyOf(blockers);
    }
}
