package com.jadecaravan.domain.campaign;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record CampaignDailyCycleState(
        UUID campaignId,
        Caravan caravan,
        UUID activeDayId,
        CampaignDaySummary lastSummary,
        List<DailyOperation> operations) {

    public CampaignDailyCycleState {
        DomainValidation.requireNonNull(campaignId, "campaignId");
        DomainValidation.requireNonNull(caravan, "caravan");
        if (activeDayId != null && caravan.findCampaignDay(activeDayId).isEmpty()) {
            throw new IllegalArgumentException("activeDayId must reference a campaign day in the caravan");
        }
        if (lastSummary != null && !Objects.equals(activeDayId, lastSummary.campaignDayId())) {
            throw new IllegalArgumentException("lastSummary must belong to the active day");
        }
        operations = operations == null ? List.of() : List.copyOf(operations);
    }

    public static CampaignDailyCycleState seeded(
            UUID campaignId,
            Caravan caravan,
            UUID activeDayId,
            CampaignDaySummary lastSummary) {
        return new CampaignDailyCycleState(campaignId, caravan, activeDayId, lastSummary, List.of());
    }

    public CampaignDay activeDay() {
        if (activeDayId == null) {
            throw new IllegalStateException("Campaign daily cycle state does not have an active day");
        }
        return caravan.findCampaignDay(activeDayId)
                .orElseThrow(() -> new IllegalStateException("Active campaign day no longer exists"));
    }

    public CampaignDailyCycleState withCaravan(Caravan newCaravan) {
        return new CampaignDailyCycleState(campaignId, newCaravan, activeDayId, lastSummary, operations);
    }

    public CampaignDailyCycleState withActiveDayId(UUID newActiveDayId) {
        return new CampaignDailyCycleState(campaignId, caravan, newActiveDayId, lastSummary, operations);
    }

    public CampaignDailyCycleState withLastSummary(CampaignDaySummary newSummary) {
        return new CampaignDailyCycleState(campaignId, caravan, activeDayId, newSummary, operations);
    }

    public CampaignDailyCycleState withOperations(List<DailyOperation> newOperations) {
        return new CampaignDailyCycleState(campaignId, caravan, activeDayId, lastSummary, newOperations);
    }
}
