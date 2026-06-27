package com.jadecaravan.adapter.out.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jadecaravan.domain.campaign.CampaignDailyCycleState;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "campaign_daily_cycle_state")
public class CampaignDailyCycleStateEntity {

    @Id
    @Column(name = "campaign_id", nullable = false, updatable = false)
    private UUID campaignId;

    @Lob
    @Column(name = "snapshot_json", nullable = false)
    private String snapshotJson;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected CampaignDailyCycleStateEntity() {
    }

    private CampaignDailyCycleStateEntity(UUID campaignId, String snapshotJson, Instant updatedAt) {
        this.campaignId = Objects.requireNonNull(campaignId, "campaignId must not be null");
        this.snapshotJson = Objects.requireNonNull(snapshotJson, "snapshotJson must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    }

    public static CampaignDailyCycleStateEntity fromDomain(CampaignDailyCycleState state, ObjectMapper objectMapper) {
        try {
            return new CampaignDailyCycleStateEntity(
                    state.campaignId(),
                    objectMapper.writeValueAsString(state),
                    Instant.now());
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Could not serialize campaign daily cycle state", ex);
        }
    }

    public CampaignDailyCycleState toDomain(ObjectMapper objectMapper) {
        try {
            return objectMapper.readValue(snapshotJson, CampaignDailyCycleState.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Could not deserialize campaign daily cycle state", ex);
        }
    }

    public UUID campaignId() {
        return campaignId;
    }
}
