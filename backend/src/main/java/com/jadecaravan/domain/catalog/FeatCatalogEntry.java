package com.jadecaravan.domain.catalog;

import java.util.Objects;

public record FeatCatalogEntry(
        String key,
        String name,
        String requirement,
        String effect,
        String usageLimit,
        String stackingRule,
        String persistenceRule,
        boolean campaignSpecific,
        String source,
        String note) implements CatalogEntry {

    public FeatCatalogEntry {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(requirement, "requirement must not be null");
        Objects.requireNonNull(effect, "effect must not be null");
        Objects.requireNonNull(usageLimit, "usageLimit must not be null");
        Objects.requireNonNull(stackingRule, "stackingRule must not be null");
        Objects.requireNonNull(persistenceRule, "persistenceRule must not be null");
        Objects.requireNonNull(source, "source must not be null");
    }
}
