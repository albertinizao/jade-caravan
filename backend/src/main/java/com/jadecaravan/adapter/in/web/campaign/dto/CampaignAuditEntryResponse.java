package com.jadecaravan.adapter.in.web.campaign.dto;

import java.time.Instant;

public record CampaignAuditEntryResponse(
        String ruleSetVersionId,
        String entryType,
        String subjectType,
        String subjectId,
        String operationType,
        String title,
        String decisionKey,
        String currentResolution,
        String configurationValue,
        String reason,
        String actor,
        String source,
        Instant occurredAt) {
}
