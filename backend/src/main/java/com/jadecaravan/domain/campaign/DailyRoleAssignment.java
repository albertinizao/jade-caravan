package com.jadecaravan.domain.campaign;

import com.jadecaravan.domain.catalog.RoleCatalogEntry;
import java.util.UUID;

public record DailyRoleAssignment(
        UUID travellerId,
        UUID campaignDayId,
        RoleCatalogEntry role,
        UUID targetCartId,
        UUID targetTravellerId,
        String targetSkill,
        String targetLanguage,
        String optionJson) {

    public DailyRoleAssignment {
        DomainValidation.requireNonNull(travellerId, "travellerId");
        DomainValidation.requireNonNull(campaignDayId, "campaignDayId");
        DomainValidation.requireNonNull(role, "role");
    }
}
