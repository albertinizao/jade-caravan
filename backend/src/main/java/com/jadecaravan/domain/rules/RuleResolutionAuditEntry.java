package com.jadecaravan.domain.rules;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record RuleResolutionAuditEntry(
        UUID campaignId,
        String ruleSetVersionId,
        RuleDecisionKey decisionKey,
        String decisionTitle,
        String currentResolution,
        String configurationValue,
        String reason,
        String actor,
        String source,
        Instant resolvedAt) {

    public RuleResolutionAuditEntry {
        Objects.requireNonNull(campaignId, "campaignId must not be null");
        Objects.requireNonNull(ruleSetVersionId, "ruleSetVersionId must not be null");
        Objects.requireNonNull(decisionKey, "decisionKey must not be null");
        Objects.requireNonNull(decisionTitle, "decisionTitle must not be null");
        Objects.requireNonNull(currentResolution, "currentResolution must not be null");
        Objects.requireNonNull(resolvedAt, "resolvedAt must not be null");
    }
}
