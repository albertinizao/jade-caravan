package com.jadecaravan.domain.campaign;

import java.util.UUID;

public record CaravanEvent(
        UUID id,
        UUID campaignDayId,
        String source,
        CaravanEventSeverity severity,
        String narrativeSummary,
        boolean requiresCheck,
        UUID checkResolutionId,
        boolean resolved,
        boolean effectsApplied) {

    public CaravanEvent {
        DomainValidation.requireNonNull(id, "id");
        DomainValidation.requireNonNull(campaignDayId, "campaignDayId");
        DomainValidation.requireNonBlank(source, "source");
        DomainValidation.requireNonNull(severity, "severity");
        DomainValidation.requireNonBlank(narrativeSummary, "narrativeSummary");
    }
}
