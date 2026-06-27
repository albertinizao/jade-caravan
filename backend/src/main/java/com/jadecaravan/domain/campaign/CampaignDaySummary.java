package com.jadecaravan.domain.campaign;

import com.jadecaravan.domain.calculation.CalculationIssue;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CampaignDaySummary(
        UUID campaignDayId,
        CampaignDayStatus status,
        BigDecimal plannedDistanceMiles,
        BigDecimal actualDistanceMiles,
        BigDecimal plannedConsumption,
        BigDecimal actualConsumption,
        BigDecimal consumptionDeficit,
        BigDecimal production,
        BigDecimal discontentBefore,
        BigDecimal discontentAfter,
        List<CalculationIssue> warnings,
        List<CalculationIssue> blockers,
        List<String> continuingAlerts,
        Instant closedAt,
        String ruleSetVersionId) {

    public CampaignDaySummary {
        DomainValidation.requireNonNull(campaignDayId, "campaignDayId");
        DomainValidation.requireNonNull(status, "status");
        DomainValidation.requireNonNull(plannedDistanceMiles, "plannedDistanceMiles");
        DomainValidation.requireNonNull(actualDistanceMiles, "actualDistanceMiles");
        DomainValidation.requireNonNull(plannedConsumption, "plannedConsumption");
        DomainValidation.requireNonNull(actualConsumption, "actualConsumption");
        DomainValidation.requireNonNull(consumptionDeficit, "consumptionDeficit");
        DomainValidation.requireNonNull(production, "production");
        DomainValidation.requireNonNull(discontentBefore, "discontentBefore");
        DomainValidation.requireNonNull(discontentAfter, "discontentAfter");
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
        blockers = blockers == null ? List.of() : List.copyOf(blockers);
        continuingAlerts = continuingAlerts == null ? List.of() : List.copyOf(continuingAlerts);
        DomainValidation.requireNonNull(closedAt, "closedAt");
        DomainValidation.requireNonBlank(ruleSetVersionId, "ruleSetVersionId");
    }
}
