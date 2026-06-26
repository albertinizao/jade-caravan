package com.jadecaravan.domain.campaign;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record CampaignAuditEntry(
        UUID campaignId,
        String ruleSetVersionId,
        String entryType,
        String subjectType,
        String subjectId,
        String operationType,
        String title,
        String currentResolution,
        String configurationValue,
        String reason,
        String actor,
        String source,
        Instant occurredAt) {

    public CampaignAuditEntry {
        Objects.requireNonNull(campaignId, "campaignId must not be null");
        Objects.requireNonNull(ruleSetVersionId, "ruleSetVersionId must not be null");
        Objects.requireNonNull(entryType, "entryType must not be null");
        Objects.requireNonNull(subjectType, "subjectType must not be null");
        Objects.requireNonNull(operationType, "operationType must not be null");
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(currentResolution, "currentResolution must not be null");
        Objects.requireNonNull(reason, "reason must not be null");
        Objects.requireNonNull(actor, "actor must not be null");
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
    }
}
