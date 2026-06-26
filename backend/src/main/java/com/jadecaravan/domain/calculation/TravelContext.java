package com.jadecaravan.domain.calculation;

import java.math.BigDecimal;

public record TravelContext(
        String terrain,
        boolean frozenTerrain,
        boolean nightTravel,
        Integer temperatureF,
        BigDecimal flatSpeedBonusMilesPerDay,
        BigDecimal flatSpeedPenaltyMilesPerDay) {

    public TravelContext {
        flatSpeedBonusMilesPerDay = flatSpeedBonusMilesPerDay == null ? BigDecimal.ZERO : flatSpeedBonusMilesPerDay;
        flatSpeedPenaltyMilesPerDay = flatSpeedPenaltyMilesPerDay == null ? BigDecimal.ZERO : flatSpeedPenaltyMilesPerDay;
    }

    public static TravelContext empty() {
        return new TravelContext(null, false, false, null, BigDecimal.ZERO, BigDecimal.ZERO);
    }
}
