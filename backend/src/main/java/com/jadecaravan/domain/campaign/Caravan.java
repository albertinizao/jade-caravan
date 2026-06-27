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
        List<CaravanFeatInstance> featInstances,
        List<CheckResolution> checkResolutions,
        List<CaravanEvent> caravanEvents,
        List<TradeTransaction> tradeTransactions,
        List<LedgerEntry> ledgerEntries,
        BigDecimal lastCargoGiftQuantity,
        BigDecimal lastTreasureGiftQuantity) {

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
        featInstances = DomainCollections.immutableCopy(featInstances);
        checkResolutions = DomainCollections.immutableCopy(checkResolutions);
        caravanEvents = DomainCollections.immutableCopy(caravanEvents);
        tradeTransactions = DomainCollections.immutableCopy(tradeTransactions);
        ledgerEntries = DomainCollections.immutableCopy(ledgerEntries);
        lastCargoGiftQuantity = normalizeNonNegative(lastCargoGiftQuantity, "lastCargoGiftQuantity");
        lastTreasureGiftQuantity = normalizeNonNegative(lastTreasureGiftQuantity, "lastTreasureGiftQuantity");
        validateOwnership(id, travellers, carts, beasts, inventoryLots, campaignDays, featInstances);
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

    public BigDecimal totalCargoOccupancy() {
        return inventoryLots.stream()
                .map(lot -> lot.quantity().multiply(lot.unitCapacity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<Cart> operativeCarts() {
        return carts.stream()
                .filter(Cart::isOperative)
                .toList();
    }

    public java.util.Optional<Cart> findCart(UUID cartId) {
        return carts.stream().filter(cart -> cart.id().equals(cartId)).findFirst();
    }

    public java.util.Optional<Traveller> findTraveller(UUID travellerId) {
        return travellers.stream().filter(traveller -> traveller.id().equals(travellerId)).findFirst();
    }

    public java.util.Optional<Beast> findBeast(UUID beastId) {
        return beasts.stream().filter(beast -> beast.id().equals(beastId)).findFirst();
    }

    public java.util.Optional<InventoryLot> findInventoryLot(UUID inventoryLotId) {
        return inventoryLots.stream().filter(lot -> lot.id().equals(inventoryLotId)).findFirst();
    }

    public java.util.Optional<CampaignDay> findCampaignDay(UUID campaignDayId) {
        return campaignDays.stream().filter(day -> day.id().equals(campaignDayId)).findFirst();
    }

    public java.util.Optional<CaravanFeatInstance> findFeatInstance(UUID featInstanceId) {
        return featInstances.stream().filter(featInstance -> featInstance.id().equals(featInstanceId)).findFirst();
    }

    public java.util.Optional<CaravanFeatInstance> findFeatInstance(String featKey) {
        if (featKey == null) {
            return java.util.Optional.empty();
        }
        String normalizedFeatKey = featKey.trim();
        return featInstances.stream()
                .filter(featInstance -> featInstance.featKey().equalsIgnoreCase(normalizedFeatKey))
                .findFirst();
    }

    public boolean hasUnusedFeat(String featKey) {
        return findFeatInstance(featKey).filter(featInstance -> !featInstance.consumed()).isPresent();
    }

    public List<String> activeFeatKeys() {
        return featInstances.stream()
                .filter(featInstance -> !featInstance.consumed())
                .map(CaravanFeatInstance::featKey)
                .toList();
    }

    public Caravan withCurrentDiscontent(BigDecimal newCurrentDiscontent) {
        DomainValidation.requireNonNegative(newCurrentDiscontent, "newCurrentDiscontent");
        return new Caravan(
                id,
                campaignId,
                name,
                level,
                ruleSetVersionId,
                baseStats,
                newCurrentDiscontent,
                currentDayNumber,
                travellers,
                carts,
                beasts,
                inventoryLots,
                campaignDays,
                featInstances,
                checkResolutions,
                caravanEvents,
                tradeTransactions,
                ledgerEntries,
                lastCargoGiftQuantity,
                lastTreasureGiftQuantity);
    }

    public Caravan adjustDiscontent(BigDecimal delta) {
        DomainValidation.requireNonNull(delta, "delta");
        BigDecimal adjustedDiscontent = currentDiscontent.add(delta);
        if (adjustedDiscontent.signum() < 0) {
            adjustedDiscontent = BigDecimal.ZERO;
        }
        return withCurrentDiscontent(adjustedDiscontent);
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
                featInstances,
                checkResolutions,
                caravanEvents,
                tradeTransactions,
                ledgerEntries,
                lastCargoGiftQuantity,
                lastTreasureGiftQuantity);
    }

    public Caravan replaceTraveller(Traveller traveller) {
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
                DomainCollections.replace(travellers, existing -> existing.id().equals(traveller.id()), traveller),
                carts,
                beasts,
                inventoryLots,
                campaignDays,
                featInstances,
                checkResolutions,
                caravanEvents,
                tradeTransactions,
                ledgerEntries,
                lastCargoGiftQuantity,
                lastTreasureGiftQuantity);
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
                featInstances,
                checkResolutions,
                caravanEvents,
                tradeTransactions,
                ledgerEntries,
                lastCargoGiftQuantity,
                lastTreasureGiftQuantity);
    }

    public Caravan replaceCart(Cart cart) {
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
                DomainCollections.replace(carts, existing -> existing.id().equals(cart.id()), cart),
                beasts,
                inventoryLots,
                campaignDays,
                featInstances,
                checkResolutions,
                caravanEvents,
                tradeTransactions,
                ledgerEntries,
                lastCargoGiftQuantity,
                lastTreasureGiftQuantity);
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
                featInstances,
                checkResolutions,
                caravanEvents,
                tradeTransactions,
                ledgerEntries,
                lastCargoGiftQuantity,
                lastTreasureGiftQuantity);
    }

    public Caravan replaceBeast(Beast beast) {
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
                DomainCollections.replace(beasts, existing -> existing.id().equals(beast.id()), beast),
                inventoryLots,
                campaignDays,
                featInstances,
                checkResolutions,
                caravanEvents,
                tradeTransactions,
                ledgerEntries,
                lastCargoGiftQuantity,
                lastTreasureGiftQuantity);
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
                featInstances,
                checkResolutions,
                caravanEvents,
                tradeTransactions,
                ledgerEntries,
                lastCargoGiftQuantity,
                lastTreasureGiftQuantity);
    }

    public Caravan replaceInventoryLot(InventoryLot inventoryLot) {
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
                DomainCollections.replace(inventoryLots, existing -> existing.id().equals(inventoryLot.id()), inventoryLot),
                campaignDays,
                featInstances,
                checkResolutions,
                caravanEvents,
                tradeTransactions,
                ledgerEntries,
                lastCargoGiftQuantity,
                lastTreasureGiftQuantity);
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
                featInstances,
                checkResolutions,
                caravanEvents,
                tradeTransactions,
                ledgerEntries,
                lastCargoGiftQuantity,
                lastTreasureGiftQuantity);
    }

    public Caravan replaceCampaignDay(CampaignDay campaignDay) {
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
                DomainCollections.replace(campaignDays, existing -> existing.id().equals(campaignDay.id()), campaignDay),
                featInstances,
                checkResolutions,
                caravanEvents,
                tradeTransactions,
                ledgerEntries,
                lastCargoGiftQuantity,
                lastTreasureGiftQuantity);
    }

    public Caravan withFeatInstance(CaravanFeatInstance featInstance) {
        DomainValidation.requireNonNull(featInstance, "featInstance");
        ensureSameCaravan(id, featInstance.caravanId(), "featInstance");
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
                DomainCollections.append(featInstances, featInstance),
                checkResolutions,
                caravanEvents,
                tradeTransactions,
                ledgerEntries,
                lastCargoGiftQuantity,
                lastTreasureGiftQuantity);
    }

    public Caravan replaceFeatInstance(CaravanFeatInstance featInstance) {
        DomainValidation.requireNonNull(featInstance, "featInstance");
        ensureSameCaravan(id, featInstance.caravanId(), "featInstance");
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
                DomainCollections.replace(featInstances, existing -> existing.id().equals(featInstance.id()), featInstance),
                checkResolutions,
                caravanEvents,
                tradeTransactions,
                ledgerEntries,
                lastCargoGiftQuantity,
                lastTreasureGiftQuantity);
    }

    public Caravan consumeFeat(String featKey) {
        DomainValidation.requireNonBlank(featKey, "featKey");
        CaravanFeatInstance featInstance = featInstances.stream()
                .filter(existingFeat -> existingFeat.featKey().equalsIgnoreCase(featKey.trim()))
                .filter(existingFeat -> !existingFeat.consumed())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Feat not available: " + featKey));
        return replaceFeatInstance(featInstance.withConsumed(true));
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
                featInstances,
                DomainCollections.append(checkResolutions, checkResolution),
                caravanEvents,
                tradeTransactions,
                ledgerEntries,
                lastCargoGiftQuantity,
                lastTreasureGiftQuantity);
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
                featInstances,
                checkResolutions,
                DomainCollections.append(caravanEvents, caravanEvent),
                tradeTransactions,
                ledgerEntries,
                lastCargoGiftQuantity,
                lastTreasureGiftQuantity);
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
                featInstances,
                checkResolutions,
                caravanEvents,
                DomainCollections.append(tradeTransactions, tradeTransaction),
                ledgerEntries,
                lastCargoGiftQuantity,
                lastTreasureGiftQuantity);
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
                featInstances,
                checkResolutions,
                caravanEvents,
                tradeTransactions,
                DomainCollections.append(ledgerEntries, ledgerEntry),
                lastCargoGiftQuantity,
                lastTreasureGiftQuantity);
    }

    public Caravan withCurrentDayNumber(int newCurrentDayNumber) {
        DomainValidation.requireRangeInclusive(newCurrentDayNumber, "newCurrentDayNumber", 0, Integer.MAX_VALUE);
        return new Caravan(
                id,
                campaignId,
                name,
                level,
                ruleSetVersionId,
                baseStats,
                currentDiscontent,
                newCurrentDayNumber,
                travellers,
                carts,
                beasts,
                inventoryLots,
                campaignDays,
                featInstances,
                checkResolutions,
                caravanEvents,
                tradeTransactions,
                ledgerEntries,
                lastCargoGiftQuantity,
                lastTreasureGiftQuantity);
    }

    public Caravan withLastCargoGiftQuantity(BigDecimal quantity) {
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
                featInstances,
                checkResolutions,
                caravanEvents,
                tradeTransactions,
                ledgerEntries,
                quantity,
                lastTreasureGiftQuantity);
    }

    public Caravan withLastTreasureGiftQuantity(BigDecimal quantity) {
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
                featInstances,
                checkResolutions,
                caravanEvents,
                tradeTransactions,
                ledgerEntries,
                lastCargoGiftQuantity,
                quantity);
    }

    private static void validateOwnership(
            UUID expectedCampaignId,
            List<Traveller> travellers,
            List<Cart> carts,
            List<Beast> beasts,
            List<InventoryLot> inventoryLots,
            List<CampaignDay> campaignDays,
            List<CaravanFeatInstance> featInstances) {
        travellers.forEach(traveller -> ensureSameCaravan(expectedCampaignId, traveller.caravanId(), "traveller"));
        carts.forEach(cart -> ensureSameCaravan(expectedCampaignId, cart.caravanId(), "cart"));
        beasts.forEach(beast -> ensureSameCaravan(expectedCampaignId, beast.caravanId(), "beast"));
        inventoryLots.forEach(lot -> ensureSameCaravan(expectedCampaignId, lot.caravanId(), "inventoryLot"));
        campaignDays.forEach(day -> ensureSameCaravan(expectedCampaignId, day.caravanId(), "campaignDay"));
        featInstances.forEach(featInstance -> ensureSameCaravan(expectedCampaignId, featInstance.caravanId(), "featInstance"));
    }

    private static void ensureSameCaravan(UUID expectedCaravanId, UUID relatedCaravanId, String label) {
        if (!expectedCaravanId.equals(relatedCaravanId)) {
            throw new IllegalArgumentException(label + " must belong to caravan " + expectedCaravanId);
        }
    }

    private static BigDecimal normalizeNonNegative(BigDecimal value, String name) {
        return value == null ? BigDecimal.ZERO : DomainValidation.requireNonNegative(value, name);
    }
}
