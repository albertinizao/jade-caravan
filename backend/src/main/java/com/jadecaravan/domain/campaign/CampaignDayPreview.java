package com.jadecaravan.domain.campaign;

import com.jadecaravan.domain.calculation.CaravanCalculationSummary;
import com.jadecaravan.domain.calculation.TravelValidationResult;
import java.util.List;

public record CampaignDayPreview(
        TravelValidationResult travelValidation,
        CaravanCalculationSummary calculationSummary,
        List<String> alerts) {

    public CampaignDayPreview {
        DomainValidation.requireNonNull(travelValidation, "travelValidation");
        DomainValidation.requireNonNull(calculationSummary, "calculationSummary");
        alerts = alerts == null ? List.of() : List.copyOf(alerts);
    }
}
