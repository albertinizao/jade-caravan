package com.jadecaravan.application.campaign.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.jadecaravan.domain.rules.RuleDecision;
import com.jadecaravan.domain.rules.RuleDecisionKey;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CampaignRulesPersistenceTest {

    @Autowired
    private CampaignRulesApplicationService service;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void persistsDecisionResolutionAcrossPersistenceContextReloads() {
        UUID campaignId = UUID.fromString("44444444-4444-4444-4444-444444444444");

        service.getDecisions(campaignId);
        RuleDecision resolvedDecision = service.resolveDecision(
                campaignId,
                RuleDecisionKey.D_02_TOWING_BONUS_STACKING,
                "Persist the campaign decision in the database.",
                "+4 per installed towing tier",
                "Director de juego",
                "Decisión manual de campaña");

        entityManager.clear();

        RuleDecision reloadedDecision = service.getDecisions(campaignId).stream()
                .filter(decision -> decision.key() == RuleDecisionKey.D_02_TOWING_BONUS_STACKING)
                .findFirst()
                .orElseThrow();

        assertThat(resolvedDecision.currentResolution()).isEqualTo("+4 per installed towing tier");
        assertThat(reloadedDecision.status().name()).isEqualTo("RESOLVED");
        assertThat(reloadedDecision.currentResolution()).isEqualTo("+4 per installed towing tier");
        assertThat(reloadedDecision.reason()).isEqualTo("Persist the campaign decision in the database.");
        assertThat(service.getAuditTrail(campaignId)).hasSizeGreaterThanOrEqualTo(1);
    }
}
