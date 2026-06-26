package com.jadecaravan.domain.catalog;

import java.util.Arrays;
import java.util.Locale;

public enum CatalogName {
    CART_TYPES("cart-types", "Tipos de carro", "Catálogo de carros base y campaña"),
    UPGRADES("upgrades", "Mejoras", "Catálogo de mejoras de carro"),
    CARGO("cargo", "Cargamento", "Catálogo de cargamento y recursos"),
    ROLES("roles", "Roles", "Catálogo de roles de viajeros"),
    FEATS("feats", "Dotes", "Catálogo de dotes de caravana"),
    BEASTS("beasts", "Bestias", "Catálogo de bestias de tiro");

    private final String pathValue;
    private final String title;
    private final String description;

    CatalogName(String pathValue, String title, String description) {
        this.pathValue = pathValue;
        this.title = title;
        this.description = description;
    }

    public String pathValue() {
        return pathValue;
    }

    public String title() {
        return title;
    }

    public String description() {
        return description;
    }

    public static CatalogName fromPathValue(String rawValue) {
        if (rawValue == null) {
            throw new IllegalArgumentException("Catalog name must not be null");
        }

        String normalized = rawValue.trim().toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(candidate -> candidate.pathValue.equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown catalog: " + rawValue));
    }
}
