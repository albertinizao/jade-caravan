package com.jadecaravan.domain.campaign;

public enum CampaignDayStatus {
    DRAFT,
    PLANNED,
    RESOLVING,
    CLOSED,
    CANCELLED;

    public boolean canTransitionTo(CampaignDayStatus targetStatus) {
        return switch (this) {
            case DRAFT -> targetStatus == PLANNED || targetStatus == CANCELLED;
            case PLANNED -> targetStatus == RESOLVING || targetStatus == CANCELLED;
            case RESOLVING -> targetStatus == CLOSED || targetStatus == CANCELLED;
            case CLOSED, CANCELLED -> false;
        };
    }
}
