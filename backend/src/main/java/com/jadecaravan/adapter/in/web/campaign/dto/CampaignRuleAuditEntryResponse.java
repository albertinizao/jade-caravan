package com.jadecaravan.adapter.in.web.campaign.dto;

import com.jadecaravan.domain.rules.RuleDecisionKey;
import java.time.Instant;

public record CampaignRuleAuditEntryResponse(
        String ruleSetVersionId,
        RuleDecisionKey decisionKey,
        String decisionTitle,
        String currentResolution,
        String configurationValue,
        String reason,
        String actor,
        String source,
        Instant resolvedAt) {
}
