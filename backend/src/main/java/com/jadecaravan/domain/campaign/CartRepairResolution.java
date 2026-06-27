package com.jadecaravan.domain.campaign;

import java.util.UUID;

public record CartRepairResolution(
        Caravan caravan,
        UUID cartId,
        int materialsConsumed,
        int hitPointsRestored,
        int resultingHitPoints) {
}
