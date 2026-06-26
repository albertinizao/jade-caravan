package com.jadecaravan.adapter.out.persistence;

import com.jadecaravan.domain.rules.RuleDecision;
import com.jadecaravan.domain.rules.RuleDecisionKey;
import com.jadecaravan.domain.rules.RuleDecisionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "campaign_rule_decision")
public class CampaignRuleDecisionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campaign_id", nullable = false)
    private CampaignRuleStateEntity campaignRuleState;

    @Column(name = "decision_key", nullable = false, length = 64)
    private String decisionKey;

    @Column(name = "decision_order", nullable = false)
    private int decisionOrder;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "current_resolution", length = 4000)
    private String currentResolution;

    @Column(name = "configuration_value", length = 4000)
    private String configurationValue;

    @Column(name = "reason", length = 4000)
    private String reason;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    protected CampaignRuleDecisionEntity() {
    }

    private CampaignRuleDecisionEntity(
            CampaignRuleStateEntity campaignRuleState,
            String decisionKey,
            int decisionOrder,
            String status,
            String currentResolution,
            String configurationValue,
            String reason,
            Instant resolvedAt) {
        this.campaignRuleState = Objects.requireNonNull(campaignRuleState, "campaignRuleState must not be null");
        this.decisionKey = Objects.requireNonNull(decisionKey, "decisionKey must not be null");
        this.decisionOrder = decisionOrder;
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.currentResolution = currentResolution;
        this.configurationValue = configurationValue;
        this.reason = reason;
        this.resolvedAt = resolvedAt;
    }

    public static CampaignRuleDecisionEntity fromDomain(
            CampaignRuleStateEntity campaignRuleState,
            RuleDecision decision,
            int decisionOrder) {
        return new CampaignRuleDecisionEntity(
                campaignRuleState,
                decision.key().name(),
                decisionOrder,
                decision.status().name(),
                decision.currentResolution(),
                decision.configurationValue(),
                decision.reason(),
                decision.resolvedAt());
    }

    public void updateFrom(RuleDecision decision, int decisionOrder) {
        this.decisionOrder = decisionOrder;
        this.status = decision.status().name();
        this.currentResolution = decision.currentResolution();
        this.configurationValue = decision.configurationValue();
        this.reason = decision.reason();
        this.resolvedAt = decision.resolvedAt();
    }

    public RuleDecision toDomain() {
        RuleDecisionKey key = RuleDecisionKey.valueOf(decisionKey);
        RuleDecisionStatus decisionStatus = RuleDecisionStatus.valueOf(status);
        if (decisionStatus == RuleDecisionStatus.PENDING) {
            return RuleDecision.pending(key);
        }
        return RuleDecision.resolved(key, currentResolution, configurationValue, reason, resolvedAt);
    }

    public String decisionKey() {
        return decisionKey;
    }

    public int decisionOrder() {
        return decisionOrder;
    }
}
