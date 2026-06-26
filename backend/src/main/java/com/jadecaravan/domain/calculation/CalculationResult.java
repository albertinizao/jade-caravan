package com.jadecaravan.domain.calculation;

import java.util.List;
import java.util.Objects;

public record CalculationResult<T>(
        T value,
        List<CalculationBreakdownItem> breakdown,
        List<CalculationIssue> warnings,
        List<CalculationIssue> blockers,
        String ruleSetVersionId) {

    public CalculationResult {
        Objects.requireNonNull(breakdown, "breakdown must not be null");
        Objects.requireNonNull(warnings, "warnings must not be null");
        Objects.requireNonNull(blockers, "blockers must not be null");
        Objects.requireNonNull(ruleSetVersionId, "ruleSetVersionId must not be null");
        breakdown = List.copyOf(breakdown);
        warnings = List.copyOf(warnings);
        blockers = List.copyOf(blockers);
    }

    public boolean isSuccessful() {
        return blockers.isEmpty();
    }
}
