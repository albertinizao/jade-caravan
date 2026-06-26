package com.jadecaravan.domain.catalog;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public record UpgradeCatalogEntry(
        String key,
        String name,
        String cost,
        BigDecimal costCp,
        String restriction,
        String stackingRule,
        List<String> incompatibilities,
        String effect,
        boolean campaignSpecific,
        String source,
        String note) implements CatalogEntry {

    public UpgradeCatalogEntry {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(cost, "cost must not be null");
        Objects.requireNonNull(restriction, "restriction must not be null");
        Objects.requireNonNull(stackingRule, "stackingRule must not be null");
        Objects.requireNonNull(effect, "effect must not be null");
        incompatibilities = List.copyOf(incompatibilities);
        Objects.requireNonNull(source, "source must not be null");
    }
}
