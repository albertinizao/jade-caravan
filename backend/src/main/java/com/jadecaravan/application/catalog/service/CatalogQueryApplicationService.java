package com.jadecaravan.application.catalog.service;

import com.jadecaravan.application.catalog.port.in.CatalogQueryUseCase;
import com.jadecaravan.domain.catalog.CatalogDocument;
import com.jadecaravan.domain.catalog.CatalogName;
import com.jadecaravan.domain.catalog.CatalogRegistry;
import com.jadecaravan.domain.catalog.CatalogSummary;
import java.util.List;
import java.util.Objects;

public class CatalogQueryApplicationService implements CatalogQueryUseCase {

    private final CatalogRegistry catalogRegistry;

    public CatalogQueryApplicationService(CatalogRegistry catalogRegistry) {
        this.catalogRegistry = Objects.requireNonNull(catalogRegistry, "catalogRegistry must not be null");
    }

    @Override
    public CatalogDocument<?> getCatalog(String catalogName) {
        try {
            CatalogName resolvedCatalogName = CatalogName.fromPathValue(catalogName);
            return catalogRegistry.catalog(resolvedCatalogName);
        } catch (IllegalArgumentException ex) {
            throw new CatalogNotFoundException(catalogName, ex);
        }
    }

    @Override
    public List<CatalogSummary> getCatalogSummaries() {
        return catalogRegistry.summaries();
    }
}
