package com.jadecaravan.application.campaign.port.in;

import com.jadecaravan.domain.rules.RuleDecision;
import com.jadecaravan.domain.rules.RuleDecisionKey;
import com.jadecaravan.domain.rules.RuleGateSummary;
import com.jadecaravan.domain.rules.RuleResolutionAuditEntry;
import java.util.List;
import java.util.UUID;

public interface CampaignRulesUseCase {

    List<RuleDecision> getDecisions(UUID campaignId);

    RuleDecision resolveDecision(
            UUID campaignId,
            RuleDecisionKey decisionKey,
            String reason,
            String configurationValue,
            String actor,
            String source);

    RuleGateSummary getActiveSummary(UUID campaignId);

    List<RuleResolutionAuditEntry> getAuditTrail(UUID campaignId);
}
