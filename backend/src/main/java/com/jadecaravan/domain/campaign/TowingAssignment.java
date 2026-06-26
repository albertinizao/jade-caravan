package com.jadecaravan.domain.campaign;

import java.util.UUID;

public record TowingAssignment(
        UUID beastId,
        UUID cartId,
        UUID campaignDayId,
        int consecutiveTowingDays) {

    public TowingAssignment {
        DomainValidation.requireNonNull(beastId, "beastId");
        DomainValidation.requireNonNull(cartId, "cartId");
        DomainValidation.requireNonNull(campaignDayId, "campaignDayId");
        DomainValidation.requireRangeInclusive(consecutiveTowingDays, "consecutiveTowingDays", 0, Integer.MAX_VALUE);
    }

    public TowingAssignment(UUID beastId, UUID cartId, UUID campaignDayId) {
        this(beastId, cartId, campaignDayId, 1);
    }

    public TowingAssignment withConsecutiveTowingDays(int newConsecutiveTowingDays) {
        return new TowingAssignment(beastId, cartId, campaignDayId, newConsecutiveTowingDays);
    }
}
