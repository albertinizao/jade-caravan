package com.jadecaravan.application.campaign.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jadecaravan.application.campaign.port.out.CampaignAuditRepository;
import com.jadecaravan.application.campaign.port.out.CampaignDailyCycleRepository;
import com.jadecaravan.application.campaign.port.out.CampaignRulesRepository;
import com.jadecaravan.domain.campaign.CampaignAuditEntry;
import com.jadecaravan.domain.campaign.CampaignDayPreview;
import com.jadecaravan.domain.campaign.CampaignDayStatus;
import com.jadecaravan.domain.campaign.CampaignDaySummary;
import com.jadecaravan.domain.campaign.CampaignDailyCycleState;
import com.jadecaravan.domain.rules.CampaignRuleState;
import com.jadecaravan.domain.rules.RuleDecision;
import com.jadecaravan.domain.rules.RuleResolutionAuditEntry;
import com.jadecaravan.domain.catalog.CatalogRegistry;
import com.jadecaravan.domain.calculation.CaravanCalculationService;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.junit.jupiter.api.Test;

class CampaignDailyCycleApplicationServiceTest {

    private final CampaignDailyCycleRepository dailyCycleRepository = new InMemoryCampaignDailyCycleRepository();
    private final CampaignRulesRepository campaignRulesRepository = new InMemoryCampaignRulesRepository();
    private final CampaignAuditRepository campaignAuditRepository = new InMemoryCampaignAuditRepository();
    private final CampaignDailyCycleApplicationService service = new CampaignDailyCycleApplicationService(
            dailyCycleRepository,
            campaignRulesRepository,
            campaignAuditRepository,
            CatalogRegistry.seeded(),
            new CaravanCalculationService(),
            Clock.fixed(Instant.parse("2026-06-27T00:00:00Z"), ZoneOffset.UTC));

    @Test
    void seedsPreviewableStateAndProducesADeterministicPreview() {
        UUID campaignId = UUID.fromString("44444444-4444-4444-4444-444444444444");

        CampaignDailyCycleState state = service.getState(campaignId);
        CampaignDayPreview preview = service.previewDay(campaignId);

        assertThat(state.activeDay()).isNotNull();
        assertThat(state.activeDay().status()).isEqualTo(CampaignDayStatus.DRAFT);
        assertThat(preview.calculationSummary().ruleSetVersionId()).isEqualTo("decision-gate-v1");
        assertThat(preview.alerts()).isNotNull();
    }

    @Test
    void closesADayAndAllowsAnAuditedReopen() {
        UUID campaignId = UUID.fromString("55555555-5555-5555-5555-555555555555");
        CampaignDailyCycleState initialState = service.getState(campaignId);

        CampaignDaySummary summary = service.closeDay(campaignId, initialState.activeDayId(), "Director de juego", "test", "Cierre de prueba");

        assertThat(summary.status()).isEqualTo(CampaignDayStatus.CLOSED);
        assertThat(service.getState(campaignId).activeDay().isClosed()).isTrue();

        CampaignDailyCycleState reopened = service.reopenDay(campaignId, initialState.activeDayId(), "Director de juego", "test", "Reabrir para ajuste");
        assertThat(reopened.activeDay().status()).isEqualTo(CampaignDayStatus.RESOLVING);
    }

    @Test
    void rejectsReopeningAnOpenDay() {
        UUID campaignId = UUID.fromString("66666666-6666-6666-6666-666666666666");
        CampaignDailyCycleState state = service.getState(campaignId);

        assertThatThrownBy(() -> service.reopenDay(campaignId, state.activeDayId(), "Director de juego", "test", "No aplica"))
                .isInstanceOf(IllegalStateException.class);
    }

    private static final class InMemoryCampaignDailyCycleRepository implements CampaignDailyCycleRepository {
        private final ConcurrentMap<UUID, CampaignDailyCycleState> states = new ConcurrentHashMap<>();

        @Override
        public java.util.Optional<CampaignDailyCycleState> findByCampaignId(UUID campaignId) {
            return java.util.Optional.ofNullable(states.get(campaignId));
        }

        @Override
        public CampaignDailyCycleState save(CampaignDailyCycleState state) {
            states.put(state.campaignId(), state);
            return state;
        }
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

        @Override
        public void appendAuditEntry(RuleResolutionAuditEntry entry) {
        }

        @Override
        public List<RuleResolutionAuditEntry> loadAuditTrail(UUID campaignId) {
            return List.of();
        }
    }

    private static final class InMemoryCampaignAuditRepository implements CampaignAuditRepository {
        @Override
        public void append(CampaignAuditEntry entry) {
        }

        @Override
        public List<CampaignAuditEntry> loadTrail(UUID campaignId) {
            return List.of();
        }
    }
}
