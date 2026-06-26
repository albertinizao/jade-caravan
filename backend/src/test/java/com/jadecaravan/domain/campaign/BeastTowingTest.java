package com.jadecaravan.domain.campaign;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.jadecaravan.domain.catalog.BeastCatalogEntry;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class BeastTowingTest {

    @Test
    void towingAssignmentStopsBeastCountingAsTravellerForThatDay() {
        Beast beast = new Beast(
                BEAST_ID,
                CARAVAN_ID,
                horseCatalog(),
                "Work Horse",
                12,
                false,
                false,
                false,
                null,
                null);

        Beast towingBeast = beast.assignToTowing(new TowingAssignment(BEAST_ID, CART_ID, DAY_ID));

        assertTrue(beast.countsAsTraveller());
        assertTrue(towingBeast.activeAsTowing());
        assertFalse(towingBeast.countsAsTraveller());
    }

    private static BeastCatalogEntry horseCatalog() {
        return new BeastCatalogEntry(
                "work-horse",
                "Work Horse",
                "50 po",
                new BigDecimal("5000"),
                "75 po",
                new BigDecimal("7500"),
                5,
                "Medium",
                40,
                0,
                "Standard draft animal",
                false,
                "test",
                null);
    }

    private static final UUID BEAST_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID CARAVAN_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final UUID CART_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");
    private static final UUID DAY_ID = UUID.fromString("77777777-7777-7777-7777-777777777777");
}
