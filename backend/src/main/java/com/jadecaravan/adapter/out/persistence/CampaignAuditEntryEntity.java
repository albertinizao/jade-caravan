package com.jadecaravan.adapter.out.persistence;

import com.jadecaravan.domain.campaign.CampaignAuditEntry;
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
@Table(name = "campaign_audit_entry")
public class CampaignAuditEntryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campaign_id", nullable = false)
    private CampaignRuleStateEntity campaignRuleState;

    @Column(name = "rule_set_version_id", nullable = false, length = 64)
    private String ruleSetVersionId;

    @Column(name = "entry_type", nullable = false, length = 64)
    private String entryType;

    @Column(name = "subject_type", nullable = false, length = 64)
    private String subjectType;

    @Column(name = "subject_id", length = 128)
    private String subjectId;

    @Column(name = "operation_type", nullable = false, length = 64)
    private String operationType;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

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

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected CampaignAuditEntryEntity() {
    }

    private CampaignAuditEntryEntity(
            CampaignRuleStateEntity campaignRuleState,
            String ruleSetVersionId,
            String entryType,
            String subjectType,
            String subjectId,
            String operationType,
            String title,
            String currentResolution,
            String configurationValue,
            String reason,
            String actor,
            String source,
            Instant occurredAt) {
        this.campaignRuleState = Objects.requireNonNull(campaignRuleState, "campaignRuleState must not be null");
        this.ruleSetVersionId = Objects.requireNonNull(ruleSetVersionId, "ruleSetVersionId must not be null");
        this.entryType = Objects.requireNonNull(entryType, "entryType must not be null");
        this.subjectType = Objects.requireNonNull(subjectType, "subjectType must not be null");
        this.subjectId = subjectId;
        this.operationType = Objects.requireNonNull(operationType, "operationType must not be null");
        this.title = Objects.requireNonNull(title, "title must not be null");
        this.currentResolution = Objects.requireNonNull(currentResolution, "currentResolution must not be null");
        this.configurationValue = configurationValue;
        this.reason = Objects.requireNonNull(reason, "reason must not be null");
        this.actor = Objects.requireNonNull(actor, "actor must not be null");
        this.source = Objects.requireNonNull(source, "source must not be null");
        this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt must not be null");
    }

    public static CampaignAuditEntryEntity fromDomain(
            CampaignRuleStateEntity campaignRuleState,
            CampaignAuditEntry entry) {
        return new CampaignAuditEntryEntity(
                campaignRuleState,
                entry.ruleSetVersionId(),
                entry.entryType(),
                entry.subjectType(),
                entry.subjectId(),
                entry.operationType(),
                entry.title(),
                entry.currentResolution(),
                entry.configurationValue(),
                entry.reason(),
                entry.actor(),
                entry.source(),
                entry.occurredAt());
    }

    public CampaignAuditEntry toDomain(UUID campaignId) {
        return new CampaignAuditEntry(
                campaignId,
                ruleSetVersionId,
                entryType,
                subjectType,
                subjectId,
                operationType,
                title,
                currentResolution,
                configurationValue,
                reason,
                actor,
                source,
                occurredAt);
    }
}
