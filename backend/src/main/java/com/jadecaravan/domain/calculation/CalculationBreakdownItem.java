package com.jadecaravan.domain.calculation;

import java.math.BigDecimal;
import java.util.Objects;

public record CalculationBreakdownItem(
        String concept,
        BigDecimal value,
        String source,
        String notes) {

    public CalculationBreakdownItem {
        Objects.requireNonNull(concept, "concept must not be null");
        Objects.requireNonNull(value, "value must not be null");
        Objects.requireNonNull(source, "source must not be null");
    }
}
