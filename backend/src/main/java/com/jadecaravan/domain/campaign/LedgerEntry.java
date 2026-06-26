package com.jadecaravan.domain.campaign;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LedgerEntry(
        UUID id,
        UUID campaignDayId,
        LedgerOperationType operationType,
        LedgerResourceType resourceType,
        UUID resourceId,
        BigDecimal delta,
        String reason,
        UUID relatedEventId,
        Instant createdAt) {

    public LedgerEntry {
        DomainValidation.requireNonNull(id, "id");
        DomainValidation.requireNonNull(campaignDayId, "campaignDayId");
        DomainValidation.requireNonNull(operationType, "operationType");
        DomainValidation.requireNonNull(resourceType, "resourceType");
        DomainValidation.requireNonNull(delta, "delta");
        DomainValidation.requireNonBlank(reason, "reason");
        DomainValidation.requireNonNull(createdAt, "createdAt");
    }
}
