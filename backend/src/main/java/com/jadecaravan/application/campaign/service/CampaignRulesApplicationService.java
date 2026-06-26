package com.jadecaravan.application.campaign.service;

import com.jadecaravan.application.campaign.port.in.CampaignRulesUseCase;
import com.jadecaravan.application.campaign.port.out.CampaignRulesRepository;
import com.jadecaravan.domain.rules.CampaignRuleState;
import com.jadecaravan.domain.rules.RuleDecision;
import com.jadecaravan.domain.rules.RuleDecisionKey;
import com.jadecaravan.domain.rules.RuleGateSummary;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class CampaignRulesApplicationService implements CampaignRulesUseCase {

    private final CampaignRulesRepository campaignRulesRepository;

    public CampaignRulesApplicationService(CampaignRulesRepository campaignRulesRepository) {
        this.campaignRulesRepository = Objects.requireNonNull(campaignRulesRepository, "campaignRulesRepository must not be null");
    }

    @Override
    public List<RuleDecision> getDecisions(UUID campaignId) {
        return loadState(campaignId).decisions();
    }

    @Override
    public RuleDecision resolveDecision(
            UUID campaignId,
            RuleDecisionKey decisionKey,
            String reason,
            String configurationValue) {
        CampaignRuleState state = loadState(campaignId);
        RuleDecision resolvedDecision = state.resolveDecision(decisionKey, reason, configurationValue);
        campaignRulesRepository.save(state);
        return resolvedDecision;
    }

    @Override
    public RuleGateSummary getActiveSummary(UUID campaignId) {
        return loadState(campaignId).activeSummary();
    }

    private CampaignRuleState loadState(UUID campaignId) {
        return campaignRulesRepository.loadOrCreate(campaignId);
    }
}
