package com.jadecaravan.domain.campaign;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record Caravan(
        UUID id,
        UUID campaignId,
        String name,
        int level,
        String ruleSetVersionId,
        CaravanStats baseStats,
        BigDecimal currentDiscontent,
        int currentDayNumber,
        List<Traveller> travellers,
        List<Cart> carts,
        List<Beast> beasts,
        List<InventoryLot> inventoryLots,
        List<CampaignDay> campaignDays,
        List<CheckResolution> checkResolutions,
        List<CaravanEvent> caravanEvents,
        List<TradeTransaction> tradeTransactions,
        List<LedgerEntry> ledgerEntries) {

    public Caravan {
        DomainValidation.requireNonNull(id, "id");
        DomainValidation.requireNonNull(campaignId, "campaignId");
        DomainValidation.requireNonBlank(name, "name");
        DomainValidation.requireRangeInclusive(level, "level", 1, Integer.MAX_VALUE);
        DomainValidation.requireNonBlank(ruleSetVersionId, "ruleSetVersionId");
        DomainValidation.requireNonNull(baseStats, "baseStats");
        DomainValidation.requireNonNegative(currentDiscontent, "currentDiscontent");
        DomainValidation.requireRangeInclusive(currentDayNumber, "currentDayNumber", 0, Integer.MAX_VALUE);
        travellers = DomainCollections.immutableCopy(travellers);
        carts = DomainCollections.immutableCopy(carts);
        beasts = DomainCollections.immutableCopy(beasts);
        inventoryLots = DomainCollections.immutableCopy(inventoryLots);
        campaignDays = DomainCollections.immutableCopy(campaignDays);
        checkResolutions = DomainCollections.immutableCopy(checkResolutions);
        caravanEvents = DomainCollections.immutableCopy(caravanEvents);
        tradeTransactions = DomainCollections.immutableCopy(tradeTransactions);
        ledgerEntries = DomainCollections.immutableCopy(ledgerEntries);
        validateOwnership(id, travellers, carts, beasts, inventoryLots, campaignDays);
    }

    public long countingTravellerCount() {
        return travellers.stream().filter(Traveller::countsAsTraveller).count();
    }

    public BigDecimal totalTravellerOccupancy() {
        return travellers.stream()
                .filter(Traveller::countsAsTraveller)
                .map(Traveller::occupancyUnits)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long towingBeastCount() {
        return beasts.stream().filter(beast -> !beast.countsAsTraveller()).count();
    }

    public Caravan withTraveller(Traveller traveller) {
        DomainValidation.requireNonNull(traveller, "traveller");
        ensureSameCaravan(id, traveller.caravanId(), "traveller");
        return new Caravan(
                id,
                campaignId,
                name,
                level,
                ruleSetVersionId,
                baseStats,
                currentDiscontent,
                currentDayNumber,
                DomainCollections.append(travellers, traveller),
                carts,
                beasts,
                inventoryLots,
                campaignDays,
                checkResolutions,
                caravanEvents,
                tradeTransactions,
                ledgerEntries);
    }

    public Caravan withCart(Cart cart) {
        DomainValidation.requireNonNull(cart, "cart");
        ensureSameCaravan(id, cart.caravanId(), "cart");
        return new Caravan(
                id,
                campaignId,
                name,
                level,
                ruleSetVersionId,
                baseStats,
                currentDiscontent,
                currentDayNumber,
                travellers,
                DomainCollections.append(carts, cart),
                beasts,
                inventoryLots,
                campaignDays,
                checkResolutions,
                caravanEvents,
                tradeTransactions,
                ledgerEntries);
    }

    public Caravan withBeast(Beast beast) {
        DomainValidation.requireNonNull(beast, "beast");
        ensureSameCaravan(id, beast.caravanId(), "beast");
        return new Caravan(
                id,
                campaignId,
                name,
                level,
                ruleSetVersionId,
                baseStats,
                currentDiscontent,
                currentDayNumber,
                travellers,
                carts,
                DomainCollections.append(beasts, beast),
                inventoryLots,
                campaignDays,
                checkResolutions,
                caravanEvents,
                tradeTransactions,
                ledgerEntries);
    }

    public Caravan withInventoryLot(InventoryLot inventoryLot) {
        DomainValidation.requireNonNull(inventoryLot, "inventoryLot");
        ensureSameCaravan(id, inventoryLot.caravanId(), "inventoryLot");
        return new Caravan(
                id,
                campaignId,
                name,
                level,
                ruleSetVersionId,
                baseStats,
                currentDiscontent,
                currentDayNumber,
                travellers,
                carts,
                beasts,
                DomainCollections.append(inventoryLots, inventoryLot),
                campaignDays,
                checkResolutions,
                caravanEvents,
                tradeTransactions,
                ledgerEntries);
    }

    public Caravan withCampaignDay(CampaignDay campaignDay) {
        DomainValidation.requireNonNull(campaignDay, "campaignDay");
        ensureSameCaravan(id, campaignDay.caravanId(), "campaignDay");
        return new Caravan(
                id,
                campaignId,
                name,
                level,
                ruleSetVersionId,
                baseStats,
                currentDiscontent,
                currentDayNumber,
                travellers,
                carts,
                beasts,
                inventoryLots,
                DomainCollections.append(campaignDays, campaignDay),
                checkResolutions,
                caravanEvents,
                tradeTransactions,
                ledgerEntries);
    }

    public Caravan withCheckResolution(CheckResolution checkResolution) {
        DomainValidation.requireNonNull(checkResolution, "checkResolution");
        return new Caravan(
                id,
                campaignId,
                name,
                level,
                ruleSetVersionId,
                baseStats,
                currentDiscontent,
                currentDayNumber,
                travellers,
                carts,
                beasts,
                inventoryLots,
                campaignDays,
                DomainCollections.append(checkResolutions, checkResolution),
                caravanEvents,
                tradeTransactions,
                ledgerEntries);
    }

    public Caravan withCaravanEvent(CaravanEvent caravanEvent) {
        DomainValidation.requireNonNull(caravanEvent, "caravanEvent");
        return new Caravan(
                id,
                campaignId,
                name,
                level,
                ruleSetVersionId,
                baseStats,
                currentDiscontent,
                currentDayNumber,
                travellers,
                carts,
                beasts,
                inventoryLots,
                campaignDays,
                checkResolutions,
                DomainCollections.append(caravanEvents, caravanEvent),
                tradeTransactions,
                ledgerEntries);
    }

    public Caravan withTradeTransaction(TradeTransaction tradeTransaction) {
        DomainValidation.requireNonNull(tradeTransaction, "tradeTransaction");
        return new Caravan(
                id,
                campaignId,
                name,
                level,
                ruleSetVersionId,
                baseStats,
                currentDiscontent,
                currentDayNumber,
                travellers,
                carts,
                beasts,
                inventoryLots,
                campaignDays,
                checkResolutions,
                caravanEvents,
                DomainCollections.append(tradeTransactions, tradeTransaction),
                ledgerEntries);
    }

    public Caravan withLedgerEntry(LedgerEntry ledgerEntry) {
        DomainValidation.requireNonNull(ledgerEntry, "ledgerEntry");
        return new Caravan(
                id,
                campaignId,
                name,
                level,
                ruleSetVersionId,
                baseStats,
                currentDiscontent,
                currentDayNumber,
                travellers,
                carts,
                beasts,
                inventoryLots,
                campaignDays,
                checkResolutions,
                caravanEvents,
                tradeTransactions,
                DomainCollections.append(ledgerEntries, ledgerEntry));
    }

    private static void validateOwnership(
            UUID expectedCampaignId,
            List<Traveller> travellers,
            List<Cart> carts,
            List<Beast> beasts,
            List<InventoryLot> inventoryLots,
            List<CampaignDay> campaignDays) {
        travellers.forEach(traveller -> ensureSameCaravan(expectedCampaignId, traveller.caravanId(), "traveller"));
        carts.forEach(cart -> ensureSameCaravan(expectedCampaignId, cart.caravanId(), "cart"));
        beasts.forEach(beast -> ensureSameCaravan(expectedCampaignId, beast.caravanId(), "beast"));
        inventoryLots.forEach(lot -> ensureSameCaravan(expectedCampaignId, lot.caravanId(), "inventoryLot"));
        campaignDays.forEach(day -> ensureSameCaravan(expectedCampaignId, day.caravanId(), "campaignDay"));
    }

    private static void ensureSameCaravan(UUID expectedCaravanId, UUID relatedCaravanId, String label) {
        if (!expectedCaravanId.equals(relatedCaravanId)) {
            throw new IllegalArgumentException(label + " must belong to caravan " + expectedCaravanId);
        }
    }
}
