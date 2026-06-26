package com.jadecaravan.domain.campaign;

public record CheckModifier(
        String source,
        int value,
        boolean applied,
        String notes) {

    public CheckModifier {
        DomainValidation.requireNonBlank(source, "source");
    }
}
