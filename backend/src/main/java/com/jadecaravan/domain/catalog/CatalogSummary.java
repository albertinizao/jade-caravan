package com.jadecaravan.domain.catalog;

public record CatalogSummary(
        CatalogName catalogName,
        String title,
        String description,
        String versionId,
        boolean campaignAware,
        int entryCount) {
}
