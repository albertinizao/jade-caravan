package com.jadecaravan.domain.calculation;

import java.util.Map;
import java.util.Objects;

public record CalculationIssue(
        BusinessRuleCode code,
        String message,
        String subject,
        Map<String, String> details,
        String source) {

    public CalculationIssue {
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(message, "message must not be null");
        details = details == null ? Map.of() : Map.copyOf(details);
        source = source == null ? "business-rules" : source;
    }
}
