package com.jadecaravan.adapter.out.persistence;

import com.jadecaravan.domain.rules.RuleDecisionKey;
import com.jadecaravan.domain.rules.RuleResolutionAuditEntry;
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
import java.util.UUID;

@Entity
@Table(name = "campaign_rule_audit_entry")
public class CampaignRuleAuditEntryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campaign_id", nullable = false)
    private CampaignRuleStateEntity campaignRuleState;

    @Column(name = "rule_set_version_id", nullable = false, length = 64)
    private String ruleSetVersionId;

    @Column(name = "decision_key", nullable = false, length = 64)
    private String decisionKey;

    @Column(name = "decision_title", nullable = false, length = 255)
    private String decisionTitle;

    @Column(name = "current_resolution", nullable = false, length = 4000)
    private String currentResolution;

    @Column(name = "configuration_value", length = 4000)
    private String configurationValue;

    @Column(name = "reason", nullable = false, length = 4000)
    private String reason;

    @Column(name = "actor", nullable = false, length = 255)
    private String actor;

    @Column(name = "source", nullable = false, length = 255)
    private String source;

    @Column(name = "resolved_at", nullable = false)
    private Instant resolvedAt;

    protected CampaignRuleAuditEntryEntity() {
    }

    private CampaignRuleAuditEntryEntity(
            CampaignRuleStateEntity campaignRuleState,
            String ruleSetVersionId,
            String decisionKey,
            String decisionTitle,
            String currentResolution,
            String configurationValue,
            String reason,
            String actor,
            String source,
            Instant resolvedAt) {
        this.campaignRuleState = Objects.requireNonNull(campaignRuleState, "campaignRuleState must not be null");
        this.ruleSetVersionId = Objects.requireNonNull(ruleSetVersionId, "ruleSetVersionId must not be null");
        this.decisionKey = Objects.requireNonNull(decisionKey, "decisionKey must not be null");
        this.decisionTitle = Objects.requireNonNull(decisionTitle, "decisionTitle must not be null");
        this.currentResolution = Objects.requireNonNull(currentResolution, "currentResolution must not be null");
        this.configurationValue = configurationValue;
        this.reason = Objects.requireNonNull(reason, "reason must not be null");
        this.actor = Objects.requireNonNull(actor, "actor must not be null");
        this.source = Objects.requireNonNull(source, "source must not be null");
        this.resolvedAt = Objects.requireNonNull(resolvedAt, "resolvedAt must not be null");
    }

    public static CampaignRuleAuditEntryEntity fromDomain(
            CampaignRuleStateEntity campaignRuleState,
            RuleResolutionAuditEntry entry) {
        return new CampaignRuleAuditEntryEntity(
                campaignRuleState,
                entry.ruleSetVersionId(),
                entry.decisionKey().name(),
                entry.decisionTitle(),
                entry.currentResolution(),
                entry.configurationValue(),
                entry.reason(),
                entry.actor(),
                entry.source(),
                entry.resolvedAt());
    }

    public RuleResolutionAuditEntry toDomain(UUID campaignId) {
        return new RuleResolutionAuditEntry(
                campaignId,
                ruleSetVersionId,
                RuleDecisionKey.valueOf(decisionKey),
                decisionTitle,
                currentResolution,
                configurationValue,
                reason,
                actor,
                source,
                resolvedAt);
    }
}
