package com.jadecaravan.domain.catalog;

import java.util.Objects;

public record CargoCatalogEntry(
        String key,
        String name,
        String cost,
        String costCp,
        String capacity,
        String capacityUnits,
        String value,
        String valueCp,
        String degradation,
        String detail,
        boolean campaignSpecific,
        String source,
        String note) implements CatalogEntry {

    public CargoCatalogEntry {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(cost, "cost must not be null");
        Objects.requireNonNull(capacity, "capacity must not be null");
        Objects.requireNonNull(value, "value must not be null");
        Objects.requireNonNull(degradation, "degradation must not be null");
        Objects.requireNonNull(detail, "detail must not be null");
        Objects.requireNonNull(source, "source must not be null");
    }
}
