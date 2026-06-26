package com.jadecaravan.adapter.in.web.catalog.dto;

import java.util.Map;
import java.util.Objects;
import java.util.LinkedHashMap;
import java.util.Collections;

public record CatalogEntryResponse(
        String entryType,
        String key,
        String name,
        boolean campaignSpecific,
        String source,
        String note,
        Map<String, Object> attributes) {

    public CatalogEntryResponse {
        Objects.requireNonNull(entryType, "entryType must not be null");
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(source, "source must not be null");
        attributes = Collections.unmodifiableMap(new LinkedHashMap<>(attributes));
    }
}
