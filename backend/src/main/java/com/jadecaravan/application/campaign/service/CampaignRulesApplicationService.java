package com.jadecaravan.application.campaign.service;

import com.jadecaravan.application.campaign.port.in.CampaignRulesUseCase;
import com.jadecaravan.application.campaign.port.out.CampaignAuditRepository;
import com.jadecaravan.application.campaign.port.out.CampaignRulesRepository;
import com.jadecaravan.domain.campaign.CampaignAuditEntry;
import com.jadecaravan.domain.rules.CampaignRuleState;
import com.jadecaravan.domain.rules.RuleDecision;
import com.jadecaravan.domain.rules.RuleDecisionKey;
import com.jadecaravan.domain.rules.RuleGateSummary;
import com.jadecaravan.domain.rules.RuleResolutionAuditEntry;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import jakarta.transaction.Transactional;

public class CampaignRulesApplicationService implements CampaignRulesUseCase {

    private final CampaignRulesRepository campaignRulesRepository;
    private final CampaignAuditRepository campaignAuditRepository;

    public CampaignRulesApplicationService(
            CampaignRulesRepository campaignRulesRepository,
            CampaignAuditRepository campaignAuditRepository) {
        this.campaignRulesRepository = Objects.requireNonNull(campaignRulesRepository, "campaignRulesRepository must not be null");
        this.campaignAuditRepository = Objects.requireNonNull(campaignAuditRepository, "campaignAuditRepository must not be null");
    }

    @Override
    public List<RuleDecision> getDecisions(UUID campaignId) {
        return loadState(campaignId).decisions();
    }

    @Override
    @Transactional
    public RuleDecision resolveDecision(
            UUID campaignId,
            RuleDecisionKey decisionKey,
            String reason,
            String configurationValue,
            String actor,
            String source) {
        CampaignRuleState state = loadState(campaignId);
        RuleDecision resolvedDecision = state.resolveDecision(decisionKey, reason, configurationValue, actor, source);
        campaignRulesRepository.save(state);
        campaignRulesRepository.appendAuditEntry(new RuleResolutionAuditEntry(
                campaignId,
                state.ruleSetVersionId(),
                resolvedDecision.key(),
                resolvedDecision.key().title(),
                resolvedDecision.currentResolution(),
                resolvedDecision.configurationValue(),
                resolvedDecision.reason(),
                resolvedDecision.actor(),
                resolvedDecision.source(),
                resolvedDecision.resolvedAt()));
        campaignAuditRepository.append(new CampaignAuditEntry(
                campaignId,
                state.ruleSetVersionId(),
                "RULE",
                "RULE_DECISION",
                resolvedDecision.key().name(),
                "RESOLVE_DECISION",
                resolvedDecision.key().title(),
                resolvedDecision.currentResolution(),
                resolvedDecision.configurationValue(),
                resolvedDecision.reason(),
                resolvedDecision.actor(),
                resolvedDecision.source(),
                resolvedDecision.resolvedAt()));
        return resolvedDecision;
    }

    @Override
    public RuleGateSummary getActiveSummary(UUID campaignId) {
        return loadState(campaignId).activeSummary();
    }

    @Override
    public List<RuleResolutionAuditEntry> getAuditTrail(UUID campaignId) {
        return campaignRulesRepository.loadAuditTrail(campaignId);
    }

    private CampaignRuleState loadState(UUID campaignId) {
        return campaignRulesRepository.loadOrCreate(campaignId);
    }
}
