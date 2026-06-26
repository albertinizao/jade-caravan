package com.jadecaravan.application.catalog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jadecaravan.domain.catalog.CatalogDocument;
import com.jadecaravan.domain.catalog.CatalogRegistry;
import com.jadecaravan.domain.catalog.CatalogSummary;
import java.util.List;
import org.junit.jupiter.api.Test;

class CatalogQueryApplicationServiceTest {

    private final CatalogQueryApplicationService service = new CatalogQueryApplicationService(CatalogRegistry.seeded());

    @Test
    void exposesTheSeededCatalogSummaries() {
        List<CatalogSummary> summaries = service.getCatalogSummaries();

        assertThat(summaries).hasSize(6);
        assertThat(summaries).allMatch(summary -> summary.versionId().equals(CatalogRegistry.VERSION_ID));
        assertThat(summaries).anySatisfy(summary -> {
            assertThat(summary.catalogName().pathValue()).isEqualTo("cart-types");
            assertThat(summary.entryCount()).isEqualTo(11);
        });
        assertThat(summaries).anySatisfy(summary -> {
            assertThat(summary.catalogName().pathValue()).isEqualTo("roles");
            assertThat(summary.entryCount()).isGreaterThan(20);
        });
    }

    @Test
    void loadsTheCustomCampaignCartAsPartOfTheCartCatalog() {
        CatalogDocument<?> catalog = service.getCatalog("cart-types");

        assertThat(catalog.catalogName().pathValue()).isEqualTo("cart-types");
        assertThat(catalog.entries())
                .anySatisfy(entry -> {
                    assertThat(entry.key()).isEqualTo("TAVERN_CART");
                    assertThat(entry.campaignSpecific()).isTrue();
                    assertThat(entry.source()).contains("docs/09-estado-inicial.md");
                });
    }

    @Test
    void rejectsUnknownCatalogNames() {
        assertThatThrownBy(() -> service.getCatalog("not-a-catalog"))
                .isInstanceOf(CatalogNotFoundException.class)
                .hasMessageContaining("not-a-catalog");
    }
}
