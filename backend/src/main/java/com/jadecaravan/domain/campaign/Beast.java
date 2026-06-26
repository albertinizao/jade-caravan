package com.jadecaravan.domain.campaign;

import com.jadecaravan.domain.catalog.BeastCatalogEntry;
import java.util.UUID;

public record Beast(
        UUID id,
        UUID caravanId,
        BeastCatalogEntry beastType,
        String name,
        int currentHitPoints,
        boolean trainedForCombat,
        boolean fatigued,
        boolean activeAsTowing,
        TowingAssignment towingAssignment,
        String notes) {

    public Beast {
        DomainValidation.requireNonNull(id, "id");
        DomainValidation.requireNonNull(caravanId, "caravanId");
        DomainValidation.requireNonNull(beastType, "beastType");
        DomainValidation.requireNonNegative(currentHitPoints, "currentHitPoints");
        if (towingAssignment != null && !id.equals(towingAssignment.beastId())) {
            throw new IllegalArgumentException("towingAssignment must belong to the beast");
        }
        if (activeAsTowing && towingAssignment == null) {
            throw new IllegalArgumentException("activeAsTowing requires a towingAssignment");
        }
    }

    public int strength() {
        return beastType.strength();
    }

    public String size() {
        return beastType.size();
    }

    public int speedFeet() {
        return beastType.speedFeet();
    }

    public Integer temperatureAdaptation() {
        return beastType.temperatureAdaptation();
    }

    public boolean countsAsTraveller() {
        return !activeAsTowing;
    }

    public Beast assignToTowing(TowingAssignment assignment) {
        DomainValidation.requireNonNull(assignment, "assignment");
        if (!id.equals(assignment.beastId())) {
            throw new IllegalArgumentException("assignment must belong to the beast");
        }
        return new Beast(
                id,
                caravanId,
                beastType,
                name,
                currentHitPoints,
                trainedForCombat,
                fatigued,
                true,
                assignment,
                notes);
    }

    public Beast releaseFromTowing() {
        return new Beast(
                id,
                caravanId,
                beastType,
                name,
                currentHitPoints,
                trainedForCombat,
                fatigued,
                false,
                null,
                notes);
    }
}
