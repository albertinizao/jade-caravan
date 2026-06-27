package com.jadecaravan.domain.calculation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.jadecaravan.domain.campaign.Beast;
import com.jadecaravan.domain.campaign.CampaignDay;
import com.jadecaravan.domain.campaign.CampaignDayActivityType;
import com.jadecaravan.domain.campaign.CampaignDayStatus;
import com.jadecaravan.domain.campaign.Caravan;
import com.jadecaravan.domain.campaign.CaravanStats;
import com.jadecaravan.domain.campaign.Cart;
import com.jadecaravan.domain.campaign.CartCargoAllocation;
import com.jadecaravan.domain.campaign.CartPassengerAssignment;
import com.jadecaravan.domain.campaign.CartUpgradeInstance;
import com.jadecaravan.domain.campaign.DailyRoleAssignment;
import com.jadecaravan.domain.campaign.InventoryLot;
import com.jadecaravan.domain.campaign.TowingAssignment;
import com.jadecaravan.domain.campaign.Traveller;
import com.jadecaravan.domain.catalog.BeastCatalogEntry;
import com.jadecaravan.domain.catalog.CartCategory;
import com.jadecaravan.domain.catalog.CartTypeCatalogEntry;
import com.jadecaravan.domain.catalog.CatalogRegistry;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CaravanBusinessRulesTest {

    @Test
    void blocksTravelWhenPassengerOccupancyExceedsCapacity() {
        CatalogRegistry registry = CatalogRegistry.seeded();
        CampaignDay day = campaignDay();
        UUID cartId = UUID.randomUUID();
        UUID beastId = UUID.randomUUID();

        Cart cart = cart(
                cartId,
                customCartType("TEST_CART", "Test cart", 1, 4, 4, 1, 1, 1, 1),
                List.of(),
                List.of(),
                List.of(new TowingAssignment(beastId, cartId, day.id())));
        Beast towingBeast = beastForCart(beastId, cartId, day.id(), 10, 40);

        Caravan caravan = caravan(
                List.of(traveller("Loady", new BigDecimal("4.5"), 1L, false, dailyRole("WAGONER", day.id(), cart.id()))),
                List.of(cart),
                List.of(towingBeast),
                List.of(),
                1);

        TravelValidationResult result = CaravanBusinessRules.validateTravel(caravan, registry, null, day, TravelContext.empty());

        assertFalse(result.travelAllowed());
        assertTrue(result.blockers().stream().anyMatch(blocker -> blocker.code() == BusinessRuleCode.PASSENGER_CAPACITY_EXCEEDED));
    }

    @Test
    void blocksTravelWhenDriverIsMissingFromAnOperativeCart() {
        CatalogRegistry registry = CatalogRegistry.seeded();
        CampaignDay day = campaignDay();
        UUID firstCartId = UUID.randomUUID();
        UUID secondCartId = UUID.randomUUID();
        UUID beastId = UUID.randomUUID();

        Cart firstCart = cart(
                firstCartId,
                customCartType("FIRST_CART", "First cart", 1, 4, 4, 1, 1, 1, 1),
                List.of(),
                List.of(),
                List.of(new TowingAssignment(beastId, firstCartId, day.id())));
        Cart secondCart = cart(
                secondCartId,
                customCartType("SECOND_CART", "Second cart", 1, 4, 4, 1, 1, 1, 1),
                List.of(),
                List.of(),
                List.of());
        Beast towingBeast = beastForCart(beastId, firstCartId, day.id(), 10, 40);

        Caravan caravan = caravan(
                List.of(
                        traveller("Driver", BigDecimal.ONE, 1L, false, dailyRole("WAGONER", day.id(), firstCart.id()))),
                List.of(firstCart, secondCart),
                List.of(towingBeast),
                List.of(),
                1);

        TravelValidationResult result = CaravanBusinessRules.validateTravel(caravan, registry, null, day, TravelContext.empty());

        assertTrue(result.blockers().stream().anyMatch(blocker -> blocker.code() == BusinessRuleCode.MISSING_DRIVER));
    }

    @Test
    void museumCartOnlyAcceptsTreasure() {
        CatalogRegistry registry = CatalogRegistry.seeded();
        CampaignDay day = campaignDay();
        Cart museumCart = cart(
                registry.cartType("MUSEUM_CART"),
                List.of(),
                List.of(cargoAllocation(lot("SUPPLIES", "supplies", BigDecimal.ONE, BigDecimal.ONE, Map.of()), BigDecimal.ONE)),
                List.of());

        Caravan caravan = caravan(
                List.of(),
                List.of(museumCart),
                List.of(),
                List.of(lot("SUPPLIES", "supplies", BigDecimal.ONE, BigDecimal.ONE, Map.of())),
                1);

        List<CalculationIssue> issues = CaravanBusinessRules.validateCartRestrictions(caravan, registry, day, museumCart);

        assertTrue(issues.stream().anyMatch(issue -> issue.code() == BusinessRuleCode.CARGO_TYPE_NOT_ALLOWED));
    }

    @Test
    void scoutDoesNotConsume() {
        CampaignDay day = campaignDay();
        Cart cart = cart(customCartType("ZERO", "Zero cart", 1, 4, 4, 0, 0, 1, 1), List.of(), List.of(), List.of());
        Caravan caravan = caravan(
                List.of(traveller("Scout", BigDecimal.ONE, 1L, false, dailyRole("SCOUT", day.id(), null))),
                List.of(cart),
                List.of(),
                List.of(),
                1);

        CalculationResult<BigDecimal> result = CaravanBusinessRules.calculateDailyConsumption(caravan, day);

        assertEquals(BigDecimal.ZERO, result.value());
    }

    @Test
    void cookConvertsOneSupplyUnitIntoFifteenProvisions() {
        CampaignDay day = campaignDay();
        Caravan caravan = caravan(
                List.of(traveller("Cook", BigDecimal.ONE, 1L, false, dailyRole("COOK", day.id(), null))),
                List.of(),
                List.of(),
                List.of(),
                1);

        CalculationResult<BigDecimal> result = CaravanBusinessRules.calculateSupplyYield(caravan, day, 1);

        assertEquals(new BigDecimal("15"), result.value());
    }

    @Test
    void perishableLotDegradesEveryTwoDays() {
        InventoryLot lot = lot("PERISHABLES", "perishables", new BigDecimal("1"), new BigDecimal("10"), Map.of());

        InventoryLot degraded = CaravanBusinessRules.advancePerishableLot(lot, 2, false);

        assertEquals(new BigDecimal("9"), degraded.remainingProvisions());
        assertEquals(BigDecimal.ZERO, degraded.perishableDecayProgress());
    }

    @Test
    void fridgeRemovesPassengerCapacity() {
        CatalogRegistry registry = CatalogRegistry.seeded();
        Cart cart = cart(
                registry.cartType("TRAVELLER_CART"),
                List.of(upgradeInstance("FRIDGE")),
                List.of(),
                List.of());
        Caravan caravan = caravan(List.of(), List.of(cart), List.of(), List.of(), 1);

        CalculationResult<BigDecimal> result = CaravanBusinessRules.calculatePassengerCapacity(caravan, registry, null);

        assertEquals(BigDecimal.ZERO, result.value());
    }

    @Test
    void coldInsulationAppliesOnceEvenWhenMultipleCartsHaveIt() {
        CatalogRegistry registry = CatalogRegistry.seeded();
        UUID cartOneId = UUID.randomUUID();
        UUID cartTwoId = UUID.randomUUID();
        UUID beastId = UUID.randomUUID();
        CampaignDay day = campaignDay();
        Cart cartOne = cart(cartOneId, registry.cartType("TRAVELLER_CART"), List.of(upgradeInstance("COLD_INSULATION")), List.of(), List.of(new TowingAssignment(beastId, cartOneId, day.id())));
        Cart cartTwo = cart(cartTwoId, registry.cartType("TRAVELLER_CART"), List.of(upgradeInstance("COLD_INSULATION")), List.of(), List.of());
        Beast towingBeast = beastForCart(beastId, cartOneId, day.id(), 10, 100);
        Caravan caravan = caravan(List.of(), List.of(cartOne, cartTwo), List.of(towingBeast), List.of(), 1);

        CalculationResult<BigDecimal> result = CaravanBusinessRules.calculateSpeedMilesPerDay(caravan, registry, null, TravelContext.empty(), day.id());

        assertEquals(new BigDecimal("6.00"), result.value());
    }

    @Test
    void iceRunnersMultiplyRequiredStrengthOutsideIce() {
        CatalogRegistry registry = CatalogRegistry.seeded();
        Cart cart = cart(
                customCartType("ICE_CART", "Ice cart", 1, 4, 5, 1, 1, 1, 1),
                List.of(upgradeInstance("ICE_RUNNERS")),
                List.of(),
                List.of());

        BigDecimal required = CaravanBusinessRules.requiredTowingStrengthForCart(cart, TravelContext.empty());

        assertEquals(new BigDecimal("20"), required);
    }

    @Test
    void improvedWheelsAddsEightMilesPerDayWhenAllOperativeCartsHaveIt() {
        CatalogRegistry registry = CatalogRegistry.seeded();
        UUID cartOneId = UUID.randomUUID();
        UUID cartTwoId = UUID.randomUUID();
        UUID beastId = UUID.randomUUID();
        CampaignDay day = campaignDay();
        Cart cartOne = cart(cartOneId, registry.cartType("TRAVELLER_CART"), List.of(upgradeInstance("IMPROVED_WHEELS")), List.of(), List.of(new TowingAssignment(beastId, cartOneId, day.id())));
        Cart cartTwo = cart(cartTwoId, registry.cartType("TRAVELLER_CART"), List.of(upgradeInstance("IMPROVED_WHEELS")), List.of(), List.of());
        Beast towingBeast = beastForCart(beastId, cartOneId, day.id(), 10, 100);
        Caravan caravan = caravan(List.of(), List.of(cartOne, cartTwo), List.of(towingBeast), List.of(), 1);

        CalculationResult<BigDecimal> result = CaravanBusinessRules.calculateSpeedMilesPerDay(caravan, registry, null, TravelContext.empty(), day.id());

        assertEquals(new BigDecimal("18.00"), result.value());
    }

    @Test
    void armouredCartRaisesRequiredTowingStrength() {
        Cart cart = cart(
                customCartType("ARMOURED_CART", "Armoured cart", 1, 4, 10, 1, 1, 1, 1),
                List.of(upgradeInstance("ARMOURED")),
                List.of(),
                List.of());

        BigDecimal required = CaravanBusinessRules.requiredTowingStrengthForCart(cart, TravelContext.empty());

        assertEquals(new BigDecimal("20"), required);
    }

    @Test
    void mutinyTriggersAtEquality() {
        Caravan caravan = caravan(List.of(), List.of(), List.of(), List.of(), 1).withCurrentDiscontent(new BigDecimal("5"));

        assertTrue(CaravanBusinessRules.isMutinyTriggered(caravan));
    }

    private static Cart cart(
            CartTypeCatalogEntry cartType,
            List<CartUpgradeInstance> upgrades,
            List<CartCargoAllocation> cargoAllocations,
            List<TowingAssignment> towingAssignments) {
        return cart(UUID.randomUUID(), cartType, upgrades, cargoAllocations, towingAssignments);
    }

    private static Cart cart(
            UUID cartId,
            CartTypeCatalogEntry cartType,
            List<CartUpgradeInstance> upgrades,
            List<CartCargoAllocation> cargoAllocations,
            List<TowingAssignment> towingAssignments) {
        List<CartUpgradeInstance> normalizedUpgrades = upgrades.stream()
                .map(upgrade -> new CartUpgradeInstance(cartId, upgrade.upgrade(), upgrade.active(), upgrade.notes()))
                .toList();
        List<CartCargoAllocation> normalizedCargoAllocations = cargoAllocations.stream()
                .map(allocation -> new CartCargoAllocation(cartId, allocation.inventoryLotId(), allocation.quantity(), allocation.notes()))
                .toList();
        List<TowingAssignment> normalizedTowingAssignments = towingAssignments.stream()
                .map(assignment -> new TowingAssignment(assignment.beastId(), cartId, assignment.campaignDayId(), assignment.consecutiveTowingDays()))
                .toList();
        return new Cart(
                cartId,
                CARAVAN_ID,
                cartType.name(),
                cartType,
                20,
                false,
                null,
                normalizedUpgrades,
                List.of(),
                normalizedCargoAllocations,
                normalizedTowingAssignments);
    }

    private static Traveller traveller(String name, BigDecimal occupancyUnits, long foodConsumption, boolean humanoid, DailyRoleAssignment assignment) {
        UUID travellerId = UUID.randomUUID();
        List<DailyRoleAssignment> normalizedAssignments = assignment == null
                ? List.of()
                : List.of(new DailyRoleAssignment(
                        travellerId,
                        assignment.campaignDayId(),
                        assignment.role(),
                        assignment.targetCartId(),
                        assignment.targetTravellerId(),
                        assignment.targetSkill(),
                        assignment.targetLanguage(),
                        assignment.optionJson()));
        return new Traveller(
                travellerId,
                CARAVAN_ID,
                name,
                false,
                humanoid,
                "Medium",
                foodConsumption,
                occupancyUnits,
                true,
                true,
                true,
                0,
                1,
                true,
                true,
                "ACTIVE",
                null,
                List.of(),
                List.of(),
                normalizedAssignments);
    }

    private static Beast beastForCart(UUID beastId, UUID cartId, UUID dayId, int strength, int speedFeet) {
        BeastCatalogEntry beastType = new BeastCatalogEntry(
                "custom-beast",
                "Custom Beast",
                "0",
                BigDecimal.ZERO,
                "0",
                BigDecimal.ZERO,
                strength,
                "Grande",
                speedFeet,
                0,
                "test",
                false,
                "test",
                null);
        return new Beast(
                beastId,
                CARAVAN_ID,
                beastType,
                "Beast",
                10,
                false,
                false,
                true,
                new TowingAssignment(beastId, cartId, dayId),
                null);
    }

    private static InventoryLot lot(String cargoTypeId, String subtype, BigDecimal quantity, BigDecimal remainingProvisions, Map<String, String> metadata) {
        Map<String, String> combinedMetadata = new java.util.HashMap<>(metadata);
        combinedMetadata.put("subtype", subtype);
        return new InventoryLot(
                UUID.randomUUID(),
                CARAVAN_ID,
                cargoTypeId,
                quantity,
                BigDecimal.ONE,
                100,
                null,
                null,
                remainingProvisions,
                BigDecimal.ZERO,
                combinedMetadata);
    }

    private static CartCargoAllocation cargoAllocation(InventoryLot lot, BigDecimal quantity) {
        return new CartCargoAllocation(UUID.randomUUID(), lot.id(), quantity, null);
    }

    private static CartUpgradeInstance upgradeInstance(String key) {
        return new CartUpgradeInstance(UUID.randomUUID(), CatalogRegistry.seeded().upgrade(key), true, null);
    }

    private static DailyRoleAssignment dailyRole(String roleKey, UUID dayId, UUID cartId) {
        return new DailyRoleAssignment(
                UUID.randomUUID(),
                dayId,
                CatalogRegistry.seeded().role(roleKey),
                cartId,
                null,
                null,
                null,
                null);
    }

    private static CampaignDay campaignDay() {
        return new CampaignDay(
                DAY_ID,
                CARAVAN_ID,
                1,
                CampaignDayStatus.DRAFT,
                CampaignDayActivityType.TRAVEL,
                "plains",
                "Road",
                "settlement",
                70,
                "clear",
                BigDecimal.ONE,
                BigDecimal.ONE,
                BigDecimal.ONE,
                List.of(),
                List.of(),
                List.of());
    }

    private static Caravan caravan(
            List<Traveller> travellers,
            List<Cart> carts,
            List<Beast> beasts,
            List<InventoryLot> inventoryLots,
            int level) {
        return new Caravan(
                CARAVAN_ID,
                CAMPAIGN_ID,
                "Jade Caravan",
                level,
                "rules-v1",
                new CaravanStats(1, 1, 1, 1),
                BigDecimal.ZERO,
                1,
                travellers,
                carts,
                beasts,
                inventoryLots,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of());
    }

    private static CartTypeCatalogEntry customCartType(
            String key,
            String name,
            int hitPoints,
            int hardness,
            int propulsionRequirement,
            int consumption,
            int passengerCapacity,
            int cargoCapacity,
            int towingLimit) {
        return new CartTypeCatalogEntry(
                key,
                name,
                CartCategory.SPECIAL,
                "0",
                BigDecimal.ZERO,
                hitPoints,
                hardness,
                propulsionRequirement,
                towingLimit + " / " + towingLimit,
                BigDecimal.valueOf(consumption),
                BigDecimal.valueOf(passengerCapacity),
                BigDecimal.valueOf(cargoCapacity),
                List.of(),
                List.of(),
                false,
                "test",
                null);
    }

    private static final UUID CAMPAIGN_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID CARAVAN_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID DAY_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
}
