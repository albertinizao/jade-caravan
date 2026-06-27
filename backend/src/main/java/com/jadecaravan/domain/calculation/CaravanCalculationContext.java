package com.jadecaravan.domain.calculation;

import java.util.List;

public record CaravanCalculationContext(
        TravelContext travelContext,
        List<String> activeFeatKeys,
        boolean fastingActive,
        boolean celebrationActive,
        boolean inSettlement,
        String settlementType) {

    public CaravanCalculationContext {
        travelContext = travelContext == null ? TravelContext.empty() : travelContext;
        activeFeatKeys = activeFeatKeys == null ? List.of() : List.copyOf(activeFeatKeys);
        settlementType = settlementType == null ? null : settlementType.trim().isEmpty() ? null : settlementType.trim();
    }

    public static CaravanCalculationContext empty() {
        return new CaravanCalculationContext(TravelContext.empty(), List.of(), false, false, false, null);
    }
}
