package com.jadecaravan.domain.campaign;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CampaignDayTest {

    @Test
    void closedCampaignDayBlocksDirectMutation() {
        CampaignDay closedDay = new CampaignDay(
                DAY_ID,
                CARAVAN_ID,
                1,
                CampaignDayStatus.RESOLVING,
                CampaignDayActivityType.TRAVEL,
                "plains",
                "Jade Road",
                "village",
                70,
                "clear",
                new BigDecimal("12"),
                new BigDecimal("14"),
                new BigDecimal("14"),
                List.of(),
                List.of(),
                List.of()).close();

        assertEquals(CampaignDayStatus.CLOSED, closedDay.status());
        assertThrows(IllegalStateException.class, () -> closedDay.withPlannedDistanceMiles(new BigDecimal("20")));
        assertThrows(IllegalStateException.class, () -> closedDay.addCheckResolution(new CheckResolution(
                CHECK_ID,
                DAY_ID,
                CheckType.SECURITY,
                List.of(),
                15,
                12,
                18,
                CheckOutcome.SUCCESS,
                "immutable")));
    }

    private static final UUID DAY_ID = UUID.fromString("88888888-8888-8888-8888-888888888888");
    private static final UUID CARAVAN_ID = UUID.fromString("99999999-9999-9999-9999-999999999999");
    private static final UUID CHECK_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
}
