package com.jadecaravan.domain.catalog;

import java.util.List;
import java.util.Objects;

public record CatalogDocument<T extends CatalogEntry>(
        CatalogName catalogName,
        String versionId,
        String title,
        String description,
        boolean campaignAware,
        List<T> entries) {

    public CatalogDocument {
        Objects.requireNonNull(catalogName, "catalogName must not be null");
        Objects.requireNonNull(versionId, "versionId must not be null");
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(description, "description must not be null");
        entries = List.copyOf(entries);
    }
}
