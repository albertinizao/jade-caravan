package com.jadecaravan.domain.campaign;

import java.util.List;
import java.util.UUID;

public record CheckResolution(
        UUID id,
        UUID campaignDayId,
        CheckType checkType,
        List<CheckModifier> modifiers,
        int dc,
        Integer naturalRoll,
        Integer total,
        CheckOutcome outcome,
        String notes) {

    public CheckResolution {
        DomainValidation.requireNonNull(id, "id");
        DomainValidation.requireNonNull(campaignDayId, "campaignDayId");
        DomainValidation.requireNonNull(checkType, "checkType");
        DomainValidation.requireNonNegative(dc, "dc");
        modifiers = DomainCollections.immutableCopy(modifiers);
        DomainValidation.requireNonNull(outcome, "outcome");
    }
}
