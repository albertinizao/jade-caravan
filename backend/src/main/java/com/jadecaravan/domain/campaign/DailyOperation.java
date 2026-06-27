package com.jadecaravan.domain.campaign;

import java.math.BigDecimal;
import java.util.UUID;

public record DailyOperation(
        UUID id,
        UUID campaignDayId,
        DailyOperationType operationType,
        String title,
        BigDecimal quantity,
        String resourceType,
        String notes) {

    public DailyOperation {
        DomainValidation.requireNonNull(id, "id");
        DomainValidation.requireNonNull(campaignDayId, "campaignDayId");
        DomainValidation.requireNonNull(operationType, "operationType");
        DomainValidation.requireNonBlank(title, "title");
        if (quantity != null) {
            DomainValidation.requireNonNegative(quantity, "quantity");
        }
    }
}
