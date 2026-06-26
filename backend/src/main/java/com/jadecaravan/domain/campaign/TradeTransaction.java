package com.jadecaravan.domain.campaign;

import java.math.BigDecimal;
import java.util.UUID;

public record TradeTransaction(
        UUID id,
        UUID campaignDayId,
        TradeTransactionType transactionType,
        String cargoTypeId,
        BigDecimal quantity,
        long unitValueCp,
        long totalValueCp,
        UUID inventoryLotId,
        String notes) {

    public TradeTransaction {
        DomainValidation.requireNonNull(id, "id");
        DomainValidation.requireNonNull(campaignDayId, "campaignDayId");
        DomainValidation.requireNonNull(transactionType, "transactionType");
        DomainValidation.requireNonBlank(cargoTypeId, "cargoTypeId");
        DomainValidation.requireNonNegative(quantity, "quantity");
        DomainValidation.requireNonNegative(unitValueCp, "unitValueCp");
        DomainValidation.requireNonNegative(totalValueCp, "totalValueCp");
    }
}
