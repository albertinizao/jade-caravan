package com.jadecaravan.domain.catalog;

import java.math.BigDecimal;
import java.util.Objects;

public record BeastCatalogEntry(
        String key,
        String name,
        String priceBase,
        BigDecimal priceBaseCp,
        String priceTrained,
        BigDecimal priceTrainedCp,
        int strength,
        String size,
        int speedFeet,
        Integer temperatureAdaptation,
        String adaptationNotes,
        boolean campaignSpecific,
        String source,
        String note) implements CatalogEntry {

    public BeastCatalogEntry {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(priceBase, "priceBase must not be null");
        Objects.requireNonNull(priceTrained, "priceTrained must not be null");
        Objects.requireNonNull(size, "size must not be null");
        Objects.requireNonNull(adaptationNotes, "adaptationNotes must not be null");
        Objects.requireNonNull(source, "source must not be null");
    }
}
