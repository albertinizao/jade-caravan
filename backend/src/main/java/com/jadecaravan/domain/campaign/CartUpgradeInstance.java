package com.jadecaravan.domain.campaign;

import com.jadecaravan.domain.catalog.UpgradeCatalogEntry;
import java.util.UUID;

public record CartUpgradeInstance(
        UUID cartId,
        UpgradeCatalogEntry upgrade,
        boolean active,
        String notes) {

    public CartUpgradeInstance {
        DomainValidation.requireNonNull(cartId, "cartId");
        DomainValidation.requireNonNull(upgrade, "upgrade");
    }
}
