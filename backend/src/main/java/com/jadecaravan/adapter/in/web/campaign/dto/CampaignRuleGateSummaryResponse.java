package com.jadecaravan.adapter.in.web.campaign.dto;

import java.util.List;

public record CampaignRuleGateSummaryResponse(
        boolean automationBlocked,
        List<CampaignRuleDecisionResponse> unresolvedDecisions) {
}
