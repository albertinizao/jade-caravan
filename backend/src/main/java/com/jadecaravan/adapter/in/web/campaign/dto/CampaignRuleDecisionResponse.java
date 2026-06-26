package com.jadecaravan.adapter.in.web.campaign.dto;

import com.jadecaravan.domain.rules.RuleDecisionKey;
import com.jadecaravan.domain.rules.RuleDecisionStatus;

public record CampaignRuleDecisionResponse(
        RuleDecisionKey decisionKey,
        String title,
        String description,
        String defaultProposal,
        RuleDecisionStatus status,
        String currentResolution,
        String configurationValue,
        String reason,
        boolean blockingAutomation) {
}
