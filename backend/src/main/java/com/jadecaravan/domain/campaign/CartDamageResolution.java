package com.jadecaravan.domain.campaign;

import java.util.UUID;

public record CartDamageResolution(
        Caravan caravan,
        UUID cartId,
        int damageTaken,
        int resultingHitPoints,
        boolean destroyed,
        boolean protectedByLevellingFromNothing) {
}
