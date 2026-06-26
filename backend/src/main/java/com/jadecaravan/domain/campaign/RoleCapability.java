package com.jadecaravan.domain.campaign;

import com.jadecaravan.domain.catalog.RoleCatalogEntry;

public record RoleCapability(
        RoleCatalogEntry role,
        String source,
        String notes) {

    public RoleCapability {
        DomainValidation.requireNonNull(role, "role");
        DomainValidation.requireNonBlank(source, "source");
    }
}
