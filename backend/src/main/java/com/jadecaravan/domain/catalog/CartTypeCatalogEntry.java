package com.jadecaravan.domain.catalog;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public record CartTypeCatalogEntry(
        String key,
        String name,
        CartCategory category,
        String cost,
        BigDecimal costCp,
        int hitPoints,
        int hardness,
        int propulsionRequirement,
        String towingCreatureLimit,
        BigDecimal consumption,
        BigDecimal passengerCapacity,
        BigDecimal cargoCapacity,
        List<String> restrictions,
        List<String> effects,
        boolean campaignSpecific,
        String source,
        String note) implements CatalogEntry {

    public CartTypeCatalogEntry {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(category, "category must not be null");
        Objects.requireNonNull(cost, "cost must not be null");
        Objects.requireNonNull(towingCreatureLimit, "towingCreatureLimit must not be null");
        Objects.requireNonNull(consumption, "consumption must not be null");
        Objects.requireNonNull(passengerCapacity, "passengerCapacity must not be null");
        Objects.requireNonNull(cargoCapacity, "cargoCapacity must not be null");
        restrictions = List.copyOf(restrictions);
        effects = List.copyOf(effects);
        Objects.requireNonNull(source, "source must not be null");
    }
}
