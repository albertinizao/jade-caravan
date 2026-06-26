package com.jadecaravan.application.campaign.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.jadecaravan.application.campaign.port.out.CampaignRulesRepository;
import com.jadecaravan.domain.rules.CampaignRuleState;
import com.jadecaravan.domain.rules.RuleDecision;
import com.jadecaravan.domain.rules.RuleDecisionKey;
import com.jadecaravan.domain.rules.RuleDecisionStatus;
import com.jadecaravan.domain.rules.RuleGateSummary;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class CampaignRulesApplicationServiceTest {

    private final CampaignRulesRepository repository = new InMemoryCampaignRulesRepository();
    private final CampaignRulesApplicationService service = new CampaignRulesApplicationService(repository);

    @Test
    void seedsFourteenDecisionsAndPreservesTheDocumentedResolution() {
        UUID campaignId = UUID.fromString("11111111-1111-1111-1111-111111111111");

        List<RuleDecision> decisions = service.getDecisions(campaignId);
        var decisionByKey = decisions.stream().collect(Collectors.toMap(RuleDecision::key, decision -> decision));

        assertThat(decisions).hasSize(14);
        assertThat(decisionByKey.get(RuleDecisionKey.D_13_DESTRUCTION_RULE_PRIORITY).status())
                .isEqualTo(RuleDecisionStatus.RESOLVED);
        assertThat(decisionByKey.get(RuleDecisionKey.D_13_DESTRUCTION_RULE_PRIORITY).currentResolution())
                .isEqualTo("2d10+5");
        assertThat(decisionByKey.get(RuleDecisionKey.D_01_EXTENDED_SPACE_ROUNDING).status())
                .isEqualTo(RuleDecisionStatus.PENDING);

        RuleGateSummary summary = service.getActiveSummary(campaignId);
        assertThat(summary.automationBlocked()).isTrue();
        assertThat(summary.unresolvedDecisions()).hasSize(13);
    }

    private static final class InMemoryCampaignRulesRepository implements CampaignRulesRepository {
        private final ConcurrentMap<UUID, CampaignRuleState> states = new ConcurrentHashMap<>();

        @Override
        public CampaignRuleState loadOrCreate(UUID campaignId) {
            return states.computeIfAbsent(campaignId, CampaignRuleState::seeded);
        }

        @Override
        public void save(CampaignRuleState state) {
            states.put(state.campaignId(), state);
        }
    }

    @Test
    void resolvesADecisionAndShrinksTheBlockingSummary() {
        UUID campaignId = UUID.fromString("22222222-2222-2222-2222-222222222222");

        RuleDecision resolvedDecision = service.resolveDecision(
                campaignId,
                RuleDecisionKey.D_01_EXTENDED_SPACE_ROUNDING,
                "Use ceiling rounding for the campaign rule set.",
                "ceil(capacidadBase × 25%)");

        assertThat(resolvedDecision.status()).isEqualTo(RuleDecisionStatus.RESOLVED);
        assertThat(resolvedDecision.currentResolution()).isEqualTo("ceil(capacidadBase × 25%)");
        assertThat(resolvedDecision.configurationValue()).isEqualTo("ceil(capacidadBase × 25%)");
        assertThat(resolvedDecision.reason()).isEqualTo("Use ceiling rounding for the campaign rule set.");

        RuleGateSummary summary = service.getActiveSummary(campaignId);
        assertThat(summary.automationBlocked()).isTrue();
        assertThat(summary.unresolvedDecisions()).hasSize(12);
    }
}
