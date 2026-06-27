package com.jadecaravan.domain.campaign;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CaravanStatsTest {

    @Test
    void preservesFractionalOccupancyUnitsExactly() {
        Caravan caravan = new Caravan(
                TEST_CARAVAN_ID,
                TEST_CAMPAIGN_ID,
                "Jade Caravan",
                1,
                "rules-1",
                new CaravanStats(1, 1, 1, 1),
                BigDecimal.ZERO,
                1,
                java.util.List.of(new Traveller(
                        TEST_TRAVELLER_ID,
                        TEST_CARAVAN_ID,
                        "Tiny Traveller",
                        false,
                        true,
                        "Small",
                        1L,
                        new BigDecimal("0.5"),
                        true,
                        true,
                        true,
                        0,
                        1,
                        true,
                        true,
                        "ACTIVE",
                        null,
                        java.util.List.of(),
                        java.util.List.of(),
                        java.util.List.of())),
                java.util.List.of(),
                java.util.List.of(),
                java.util.List.of(),
                java.util.List.of(),
                java.util.List.of(),
                java.util.List.of(),
                java.util.List.of(),
                java.util.List.of(),
                java.util.List.of(),
                java.math.BigDecimal.ZERO,
                java.math.BigDecimal.ZERO);

        assertEquals(new BigDecimal("0.5"), caravan.travellers().get(0).occupancyUnits());
        assertEquals(new BigDecimal("0.5"), caravan.totalTravellerOccupancy());
    }

    @Test
    void rejectsBaseStatsOutsideInclusiveZeroToTenRange() {
        assertThrows(IllegalArgumentException.class, () -> new CaravanStats(-1, 1, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new CaravanStats(1, 11, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new CaravanStats(1, 1, 12, 1));
        assertThrows(IllegalArgumentException.class, () -> new CaravanStats(1, 1, 1, 99));
    }

    private static final java.util.UUID TEST_CAMPAIGN_ID = java.util.UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final java.util.UUID TEST_CARAVAN_ID = java.util.UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final java.util.UUID TEST_TRAVELLER_ID = java.util.UUID.fromString("33333333-3333-3333-3333-333333333333");
}
