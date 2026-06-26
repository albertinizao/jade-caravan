package com.jadecaravan.adapter.in.web.catalog;

import com.jadecaravan.adapter.in.web.catalog.dto.CatalogEntryResponse;
import com.jadecaravan.adapter.in.web.catalog.dto.CatalogResponse;
import com.jadecaravan.application.catalog.port.in.CatalogQueryUseCase;
import com.jadecaravan.domain.catalog.BeastCatalogEntry;
import com.jadecaravan.domain.catalog.CargoCatalogEntry;
import com.jadecaravan.domain.catalog.CatalogDocument;
import com.jadecaravan.domain.catalog.CatalogEntry;
import com.jadecaravan.domain.catalog.CartTypeCatalogEntry;
import com.jadecaravan.domain.catalog.FeatCatalogEntry;
import com.jadecaravan.domain.catalog.RoleCatalogEntry;
import com.jadecaravan.domain.catalog.UpgradeCatalogEntry;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(value = "/api/v1/catalogs", produces = MediaType.APPLICATION_JSON_VALUE)
public class CatalogController {

    private final CatalogQueryUseCase catalogQueryUseCase;

    public CatalogController(CatalogQueryUseCase catalogQueryUseCase) {
        this.catalogQueryUseCase = catalogQueryUseCase;
    }

    @GetMapping("/{catalogName}")
    public CatalogResponse getCatalog(@PathVariable String catalogName) {
        CatalogDocument<?> catalog = catalogQueryUseCase.getCatalog(catalogName);
        return new CatalogResponse(
                catalog.catalogName().pathValue(),
                catalog.title(),
                catalog.description(),
                catalog.versionId(),
                catalog.campaignAware(),
                catalog.entries().stream().map(CatalogController::toResponse).toList());
    }

    private static CatalogEntryResponse toResponse(CatalogEntry entry) {
        Map<String, Object> attributes = new LinkedHashMap<>();
        String entryType;

        if (entry instanceof CartTypeCatalogEntry cart) {
            entryType = "cart-type";
            attributes.put("category", cart.category().name());
            attributes.put("cost", cart.cost());
            attributes.put("costCp", cart.costCp());
            attributes.put("hitPoints", cart.hitPoints());
            attributes.put("hardness", cart.hardness());
            attributes.put("propulsionRequirement", cart.propulsionRequirement());
            attributes.put("towingCreatureLimit", cart.towingCreatureLimit());
            attributes.put("consumption", cart.consumption());
            attributes.put("passengerCapacity", cart.passengerCapacity());
            attributes.put("cargoCapacity", cart.cargoCapacity());
            attributes.put("restrictions", cart.restrictions());
            attributes.put("effects", cart.effects());
        } else if (entry instanceof UpgradeCatalogEntry upgrade) {
            entryType = "upgrade";
            attributes.put("cost", upgrade.cost());
            attributes.put("costCp", upgrade.costCp());
            attributes.put("restriction", upgrade.restriction());
            attributes.put("stackingRule", upgrade.stackingRule());
            attributes.put("incompatibilities", upgrade.incompatibilities());
            attributes.put("effect", upgrade.effect());
        } else if (entry instanceof CargoCatalogEntry cargo) {
            entryType = "cargo";
            attributes.put("cost", cargo.cost());
            attributes.put("costCp", cargo.costCp());
            attributes.put("capacity", cargo.capacity());
            attributes.put("capacityUnits", cargo.capacityUnits());
            attributes.put("value", cargo.value());
            attributes.put("valueCp", cargo.valueCp());
            attributes.put("degradation", cargo.degradation());
            attributes.put("detail", cargo.detail());
        } else if (entry instanceof RoleCatalogEntry role) {
            entryType = "role";
            attributes.put("hardLimit", role.hardLimit());
            attributes.put("requirement", role.requirement());
            attributes.put("benefitSummary", role.benefitSummary());
            attributes.put("optionalSubsystem", role.optionalSubsystem());
        } else if (entry instanceof FeatCatalogEntry feat) {
            entryType = "feat";
            attributes.put("requirement", feat.requirement());
            attributes.put("effect", feat.effect());
            attributes.put("usageLimit", feat.usageLimit());
            attributes.put("stackingRule", feat.stackingRule());
            attributes.put("persistenceRule", feat.persistenceRule());
        } else if (entry instanceof BeastCatalogEntry beast) {
            entryType = "beast";
            attributes.put("priceBase", beast.priceBase());
            attributes.put("priceBaseCp", beast.priceBaseCp());
            attributes.put("priceTrained", beast.priceTrained());
            attributes.put("priceTrainedCp", beast.priceTrainedCp());
            attributes.put("strength", beast.strength());
            attributes.put("size", beast.size());
            attributes.put("speedFeet", beast.speedFeet());
            attributes.put("temperatureAdaptation", beast.temperatureAdaptation());
            attributes.put("adaptationNotes", beast.adaptationNotes());
        } else {
            throw new IllegalStateException("Unsupported catalog entry type: " + entry.getClass().getName());
        }

        return new CatalogEntryResponse(
                entryType,
                entry.key(),
                entry.name(),
                entry.campaignSpecific(),
                entry.source(),
                entry.note(),
                attributes);
    }
}
