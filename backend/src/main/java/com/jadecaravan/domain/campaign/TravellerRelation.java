package com.jadecaravan.domain.campaign;

import java.util.UUID;

public record TravellerRelation(
        UUID sourceTravellerId,
        UUID targetTravellerId,
        TravellerRelationType relationType,
        String notes) {

    public TravellerRelation {
        DomainValidation.requireNonNull(sourceTravellerId, "sourceTravellerId");
        DomainValidation.requireNonNull(targetTravellerId, "targetTravellerId");
        DomainValidation.requireNonNull(relationType, "relationType");
        if (sourceTravellerId.equals(targetTravellerId)) {
            throw new IllegalArgumentException("sourceTravellerId and targetTravellerId must be different");
        }
    }
}
