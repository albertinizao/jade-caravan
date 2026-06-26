package com.jadecaravan.adapter.in.web.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jadecaravan.adapter.in.web.catalog.dto.CatalogResponse;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void returnsTheRequestedCatalogWithStructuredEntries() throws Exception {
        String json = mockMvc.perform(get("/api/v1/catalogs/{catalogName}", "cart-types"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        CatalogResponse response = objectMapper.readValue(json, CatalogResponse.class);

        assertThat(response.catalogName()).isEqualTo("cart-types");
        assertThat(response.versionId()).isEqualTo("catalogs-v1");
        assertThat(response.entries()).hasSize(11);
        assertThat(response.entries())
                .anySatisfy(entry -> {
                    assertThat(entry.entryType()).isEqualTo("cart-type");
                    assertThat(entry.key()).isEqualTo("TAVERN_CART");
                    assertThat(entry.campaignSpecific()).isTrue();
                    assertThat(entry.attributes()).containsEntry("category", "SPECIAL");
                });
    }

    @Test
    void returnsProblemDetailsWhenTheCatalogDoesNotExist() throws Exception {
        String json = mockMvc.perform(get("/api/v1/catalogs/{catalogName}", "missing"))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<?, ?> problem = objectMapper.readValue(json, Map.class);

        assertThat(problem.get("title")).isEqualTo("Catálogo desconocido");
        assertThat(problem.get("status")).isEqualTo(404);
        assertThat(problem.get("type")).isEqualTo("https://caravan.local/problems/catalog-not-found");
    }
}
