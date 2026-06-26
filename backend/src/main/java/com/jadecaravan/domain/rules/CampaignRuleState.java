package com.jadecaravan.domain.rules;

import java.util.EnumMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class CampaignRuleState {

    private final UUID campaignId;
    private final String ruleSetVersionId;
    private final EnumMap<RuleDecisionKey, RuleDecision> decisions;

    private CampaignRuleState(UUID campaignId, String ruleSetVersionId, EnumMap<RuleDecisionKey, RuleDecision> decisions) {
        this.campaignId = Objects.requireNonNull(campaignId, "campaignId must not be null");
        this.ruleSetVersionId = Objects.requireNonNull(ruleSetVersionId, "ruleSetVersionId must not be null");
        this.decisions = decisions;
    }

    public static CampaignRuleState seeded(UUID campaignId) {
        EnumMap<RuleDecisionKey, RuleDecision> seededDecisions = new EnumMap<>(RuleDecisionKey.class);
        for (RuleDecisionKey key : RuleDecisionKey.values()) {
            seededDecisions.put(key, key.seedDecision());
        }
        return new CampaignRuleState(campaignId, RuleDecisionCatalog.VERSION_ID, seededDecisions);
    }

    public static CampaignRuleState reconstitute(
            UUID campaignId,
            String ruleSetVersionId,
            List<RuleDecision> decisions) {
        EnumMap<RuleDecisionKey, RuleDecision> reconstitutedDecisions = new EnumMap<>(RuleDecisionKey.class);
        for (RuleDecision decision : decisions) {
            reconstitutedDecisions.put(decision.key(), decision);
        }
        for (RuleDecisionKey key : RuleDecisionKey.values()) {
            reconstitutedDecisions.putIfAbsent(key, key.seedDecision());
        }
        return new CampaignRuleState(campaignId, ruleSetVersionId, reconstitutedDecisions);
    }

    public UUID campaignId() {
        return campaignId;
    }

    public String ruleSetVersionId() {
        return ruleSetVersionId;
    }

    public synchronized List<RuleDecision> decisions() {
        return List.copyOf(decisions.values());
    }

    public synchronized RuleDecision decision(RuleDecisionKey key) {
        RuleDecision decision = decisions.get(key);
        if (decision == null) {
            throw new IllegalArgumentException("Unknown rule decision key: " + key);
        }
        return decision;
    }

    public synchronized RuleDecision resolveDecision(
            RuleDecisionKey key,
            String reason,
            String configurationValue) {
        RuleDecision existingDecision = decision(key);
        String normalizedConfigurationValue = normalize(configurationValue);
        String effectiveResolution = normalizedConfigurationValue != null
                ? normalizedConfigurationValue
                : key.documentedChoice() != null ? key.documentedChoice() : key.defaultProposal();
        RuleDecision resolvedDecision = existingDecision.resolve(
                effectiveResolution,
                normalizedConfigurationValue,
                reason);
        decisions.put(key, resolvedDecision);
        return resolvedDecision;
    }

    public synchronized RuleGateSummary activeSummary() {
        List<RuleDecision> unresolvedDecisions = decisions.values().stream()
                .filter(RuleDecision::blockingAutomation)
                .toList();
        return new RuleGateSummary(!unresolvedDecisions.isEmpty(), unresolvedDecisions);
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }
}
