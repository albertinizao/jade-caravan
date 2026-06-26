package com.jadecaravan.domain.campaign;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class LedgerEntryTest {

    @Test
    void ledgerEntryIsImmutableAndRequiresCoreFields() {
        LedgerEntry entry = new LedgerEntry(
                ENTRY_ID,
                DAY_ID,
                LedgerOperationType.SELL_CARGO,
                LedgerResourceType.CURRENCY,
                RESOURCE_ID,
                new BigDecimal("125"),
                "Sale of cargo",
                EVENT_ID,
                CREATED_AT);

        assertTrue(LedgerEntry.class.isRecord());
        assertAll(
                () -> assertEquals(ENTRY_ID, entry.id()),
                () -> assertEquals(DAY_ID, entry.campaignDayId()),
                () -> assertEquals(LedgerOperationType.SELL_CARGO, entry.operationType()),
                () -> assertEquals(LedgerResourceType.CURRENCY, entry.resourceType()),
                () -> assertEquals(new BigDecimal("125"), entry.delta()),
                () -> assertEquals("Sale of cargo", entry.reason()),
                () -> assertEquals(EVENT_ID, entry.relatedEventId()),
                () -> assertEquals(CREATED_AT, entry.createdAt()));

        assertThrows(NullPointerException.class, () -> new LedgerEntry(
                ENTRY_ID,
                DAY_ID,
                LedgerOperationType.SELL_CARGO,
                LedgerResourceType.CURRENCY,
                RESOURCE_ID,
                new BigDecimal("125"),
                null,
                EVENT_ID,
                CREATED_AT));
    }

    private static final UUID ENTRY_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID DAY_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    private static final UUID RESOURCE_ID = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
    private static final UUID EVENT_ID = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee");
    private static final Instant CREATED_AT = Instant.parse("2026-06-26T12:00:00Z");
}
