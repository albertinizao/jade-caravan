package com.jadecaravan.domain.catalog;

import java.util.Objects;

public record RoleCatalogEntry(
        String key,
        String name,
        String hardLimit,
        String requirement,
        String benefitSummary,
        boolean optionalSubsystem,
        boolean campaignSpecific,
        String source,
        String note) implements CatalogEntry {

    public RoleCatalogEntry {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(hardLimit, "hardLimit must not be null");
        Objects.requireNonNull(requirement, "requirement must not be null");
        Objects.requireNonNull(benefitSummary, "benefitSummary must not be null");
        Objects.requireNonNull(source, "source must not be null");
    }
}
