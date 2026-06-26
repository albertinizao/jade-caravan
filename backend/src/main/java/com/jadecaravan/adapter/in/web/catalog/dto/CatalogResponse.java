package com.jadecaravan.adapter.in.web.catalog.dto;

import java.util.List;
import java.util.Objects;

public record CatalogResponse(
        String catalogName,
        String title,
        String description,
        String versionId,
        boolean campaignAware,
        List<CatalogEntryResponse> entries) {

    public CatalogResponse {
        Objects.requireNonNull(catalogName, "catalogName must not be null");
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(description, "description must not be null");
        Objects.requireNonNull(versionId, "versionId must not be null");
        entries = List.copyOf(entries);
    }
}
