package com.jadecaravan.adapter.in.web.campaign.dto;

import com.jadecaravan.domain.rules.RuleDecisionKey;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CampaignRuleDecisionRequest(
        @NotNull RuleDecisionKey decisionKey,
        @NotBlank String reason,
        String configurationValue,
        String actor,
        String source) {
}
