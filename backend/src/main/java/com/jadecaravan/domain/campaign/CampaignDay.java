package com.jadecaravan.domain.campaign;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CampaignDay(
        UUID id,
        UUID caravanId,
        int dayNumber,
        CampaignDayStatus status,
        CampaignDayActivityType activityType,
        String terrainType,
        String location,
        String settlementType,
        Integer temperatureF,
        String weatherSeverity,
        BigDecimal travelHours,
        BigDecimal plannedDistanceMiles,
        BigDecimal resolvedDistanceMiles,
        List<CheckResolution> checkResolutions,
        List<CaravanEvent> caravanEvents,
        List<TradeTransaction> tradeTransactions) {

    public CampaignDay {
        DomainValidation.requireNonNull(id, "id");
        DomainValidation.requireNonNull(caravanId, "caravanId");
        DomainValidation.requireRangeInclusive(dayNumber, "dayNumber", 1, Integer.MAX_VALUE);
        DomainValidation.requireNonNull(status, "status");
        DomainValidation.requireNonNull(activityType, "activityType");
        if (temperatureF != null) {
            // no-op validation; null allowed for non-weather days
        }
        if (travelHours != null) {
            DomainValidation.requireNonNegative(travelHours, "travelHours");
        }
        if (plannedDistanceMiles != null) {
            DomainValidation.requireNonNegative(plannedDistanceMiles, "plannedDistanceMiles");
        }
        if (resolvedDistanceMiles != null) {
            DomainValidation.requireNonNegative(resolvedDistanceMiles, "resolvedDistanceMiles");
        }
        checkResolutions = DomainCollections.immutableCopy(checkResolutions);
        caravanEvents = DomainCollections.immutableCopy(caravanEvents);
        tradeTransactions = DomainCollections.immutableCopy(tradeTransactions);
        for (CheckResolution checkResolution : checkResolutions) {
            if (!id.equals(checkResolution.campaignDayId())) {
                throw new IllegalArgumentException("checkResolutions must belong to the campaign day");
            }
        }
        for (CaravanEvent caravanEvent : caravanEvents) {
            if (!id.equals(caravanEvent.campaignDayId())) {
                throw new IllegalArgumentException("caravanEvents must belong to the campaign day");
            }
        }
        for (TradeTransaction tradeTransaction : tradeTransactions) {
            if (!id.equals(tradeTransaction.campaignDayId())) {
                throw new IllegalArgumentException("tradeTransactions must belong to the campaign day");
            }
        }
    }

    public boolean isClosed() {
        return status == CampaignDayStatus.CLOSED;
    }

    public CampaignDay withStatus(CampaignDayStatus newStatus) {
        DomainValidation.requireNonNull(newStatus, "newStatus");
        if (!status.canTransitionTo(newStatus)) {
            throw new IllegalStateException("Cannot transition campaign day from " + status + " to " + newStatus);
        }
        return new CampaignDay(
                id,
                caravanId,
                dayNumber,
                newStatus,
                activityType,
                terrainType,
                location,
                settlementType,
                temperatureF,
                weatherSeverity,
                travelHours,
                plannedDistanceMiles,
                resolvedDistanceMiles,
                checkResolutions,
                caravanEvents,
                tradeTransactions);
    }

    public CampaignDay close() {
        return withStatus(CampaignDayStatus.CLOSED);
    }

    public CampaignDay reopen(CampaignDayStatus newStatus) {
        DomainValidation.requireNonNull(newStatus, "newStatus");
        if (!isClosed()) {
            throw new IllegalStateException("Only closed campaign days can be reopened");
        }
        if (newStatus == CampaignDayStatus.CLOSED || newStatus == CampaignDayStatus.CANCELLED) {
            throw new IllegalArgumentException("Reopened campaign days must transition back to an open status");
        }
        return new CampaignDay(
                id,
                caravanId,
                dayNumber,
                newStatus,
                activityType,
                terrainType,
                location,
                settlementType,
                temperatureF,
                weatherSeverity,
                travelHours,
                plannedDistanceMiles,
                resolvedDistanceMiles,
                checkResolutions,
                caravanEvents,
                tradeTransactions);
    }

    public CampaignDay withPlannedDistanceMiles(BigDecimal distanceMiles) {
        ensureMutable();
        DomainValidation.requireNonNegative(distanceMiles, "distanceMiles");
        return new CampaignDay(
                id,
                caravanId,
                dayNumber,
                status,
                activityType,
                terrainType,
                location,
                settlementType,
                temperatureF,
                weatherSeverity,
                travelHours,
                distanceMiles,
                resolvedDistanceMiles,
                checkResolutions,
                caravanEvents,
                tradeTransactions);
    }

    public CampaignDay withResolvedDistanceMiles(BigDecimal distanceMiles) {
        ensureMutable();
        DomainValidation.requireNonNegative(distanceMiles, "distanceMiles");
        return new CampaignDay(
                id,
                caravanId,
                dayNumber,
                status,
                activityType,
                terrainType,
                location,
                settlementType,
                temperatureF,
                weatherSeverity,
                travelHours,
                plannedDistanceMiles,
                distanceMiles,
                checkResolutions,
                caravanEvents,
                tradeTransactions);
    }

    public CampaignDay addCheckResolution(CheckResolution checkResolution) {
        ensureMutable();
        DomainValidation.requireNonNull(checkResolution, "checkResolution");
        if (!id.equals(checkResolution.campaignDayId())) {
            throw new IllegalArgumentException("checkResolution must belong to the campaign day");
        }
        return new CampaignDay(
                id,
                caravanId,
                dayNumber,
                status,
                activityType,
                terrainType,
                location,
                settlementType,
                temperatureF,
                weatherSeverity,
                travelHours,
                plannedDistanceMiles,
                resolvedDistanceMiles,
                DomainCollections.append(checkResolutions, checkResolution),
                caravanEvents,
                tradeTransactions);
    }

    public CampaignDay addCaravanEvent(CaravanEvent caravanEvent) {
        ensureMutable();
        DomainValidation.requireNonNull(caravanEvent, "caravanEvent");
        if (!id.equals(caravanEvent.campaignDayId())) {
            throw new IllegalArgumentException("caravanEvent must belong to the campaign day");
        }
        return new CampaignDay(
                id,
                caravanId,
                dayNumber,
                status,
                activityType,
                terrainType,
                location,
                settlementType,
                temperatureF,
                weatherSeverity,
                travelHours,
                plannedDistanceMiles,
                resolvedDistanceMiles,
                checkResolutions,
                DomainCollections.append(caravanEvents, caravanEvent),
                tradeTransactions);
    }

    public CampaignDay addTradeTransaction(TradeTransaction tradeTransaction) {
        ensureMutable();
        DomainValidation.requireNonNull(tradeTransaction, "tradeTransaction");
        if (!id.equals(tradeTransaction.campaignDayId())) {
            throw new IllegalArgumentException("tradeTransaction must belong to the campaign day");
        }
        return new CampaignDay(
                id,
                caravanId,
                dayNumber,
                status,
                activityType,
                terrainType,
                location,
                settlementType,
                temperatureF,
                weatherSeverity,
                travelHours,
                plannedDistanceMiles,
                resolvedDistanceMiles,
                checkResolutions,
                caravanEvents,
                DomainCollections.append(tradeTransactions, tradeTransaction));
    }

    private void ensureMutable() {
        if (isClosed() || status == CampaignDayStatus.CANCELLED) {
            throw new IllegalStateException("Campaign day is closed and cannot be edited directly");
        }
    }
}
