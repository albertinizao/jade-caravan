package com.jadecaravan.application.catalog.port.in;

import com.jadecaravan.domain.catalog.CatalogDocument;
import com.jadecaravan.domain.catalog.CatalogSummary;
import java.util.List;

public interface CatalogQueryUseCase {

    CatalogDocument<?> getCatalog(String catalogName);

    List<CatalogSummary> getCatalogSummaries();
}
