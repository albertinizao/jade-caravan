package com.jadecaravan.adapter.in.web.campaign.dto;

import java.util.List;

public record CampaignRuleGateSummaryResponse(
        String ruleSetVersionId,
        boolean automationBlocked,
        List<CampaignRuleDecisionResponse> unresolvedDecisions) {
}
