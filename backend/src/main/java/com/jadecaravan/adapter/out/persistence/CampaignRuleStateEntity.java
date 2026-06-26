package com.jadecaravan.adapter.out.persistence;

import com.jadecaravan.domain.rules.CampaignRuleState;
import com.jadecaravan.domain.rules.RuleDecision;
import com.jadecaravan.domain.rules.RuleDecisionCatalog;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "campaign_rule_state")
public class CampaignRuleStateEntity {

    @Id
    @Column(name = "campaign_id", nullable = false, updatable = false)
    private UUID campaignId;

    @Column(name = "rule_set_version_id", nullable = false, length = 64)
    private String ruleSetVersionId;

    @OneToMany(mappedBy = "campaignRuleState", orphanRemoval = true, cascade = jakarta.persistence.CascadeType.ALL)
    @OrderBy("decisionOrder ASC")
    private List<CampaignRuleDecisionEntity> decisions = new ArrayList<>();

    protected CampaignRuleStateEntity() {
    }

    private CampaignRuleStateEntity(UUID campaignId, String ruleSetVersionId) {
        this.campaignId = Objects.requireNonNull(campaignId, "campaignId must not be null");
        this.ruleSetVersionId = Objects.requireNonNull(ruleSetVersionId, "ruleSetVersionId must not be null");
    }

    public static CampaignRuleStateEntity seeded(UUID campaignId) {
        CampaignRuleStateEntity entity = new CampaignRuleStateEntity(campaignId, RuleDecisionCatalog.VERSION_ID);
        entity.resetDecisions(CampaignRuleState.seeded(campaignId).decisions());
        return entity;
    }

    public CampaignRuleState toDomain() {
        List<RuleDecision> domainDecisions = decisions.stream()
                .map(CampaignRuleDecisionEntity::toDomain)
                .toList();
        return CampaignRuleState.reconstitute(campaignId, ruleSetVersionId, domainDecisions);
    }

    public void replaceWith(CampaignRuleState state) {
        this.ruleSetVersionId = state.ruleSetVersionId();
        syncDecisions(state.decisions());
    }

    public void resetDecisions(List<RuleDecision> ruleDecisions) {
        decisions.clear();
        int index = 0;
        for (RuleDecision decision : ruleDecisions) {
            decisions.add(CampaignRuleDecisionEntity.fromDomain(this, decision, index++));
        }
    }

    private void syncDecisions(List<RuleDecision> ruleDecisions) {
        Map<String, CampaignRuleDecisionEntity> existingDecisionsByKey = new LinkedHashMap<>();
        for (CampaignRuleDecisionEntity decisionEntity : decisions) {
            existingDecisionsByKey.put(decisionEntity.decisionKey(), decisionEntity);
        }

        List<CampaignRuleDecisionEntity> syncedDecisions = new ArrayList<>(ruleDecisions.size());
        int index = 0;
        for (RuleDecision decision : ruleDecisions) {
            CampaignRuleDecisionEntity existingDecision = existingDecisionsByKey.remove(decision.key().name());
            if (existingDecision == null) {
                syncedDecisions.add(CampaignRuleDecisionEntity.fromDomain(this, decision, index++));
            } else {
                existingDecision.updateFrom(decision, index++);
                syncedDecisions.add(existingDecision);
            }
        }

        decisions.clear();
        decisions.addAll(syncedDecisions);
    }

    public UUID campaignId() {
        return campaignId;
    }
}
