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
import com.jadecaravan.domain.campaign.CartUpgradeInstance;
import com.jadecaravan.domain.campaign.DailyRoleAssignment;
import com.jadecaravan.domain.campaign.InventoryLot;
import com.jadecaravan.domain.campaign.Traveller;
import com.jadecaravan.domain.catalog.BeastCatalogEntry;
import com.jadecaravan.domain.catalog.CartCategory;
import com.jadecaravan.domain.catalog.CatalogRegistry;
import com.jadecaravan.domain.catalog.CartTypeCatalogEntry;
import com.jadecaravan.domain.rules.CampaignRuleState;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CaravanCalculationServiceTest {

    private final CaravanCalculationService service = new CaravanCalculationService();

    @Test
    void fridgeLocksPassengerCapacityEvenIfExtendedSpaceIsAlsoInstalled() {
        CatalogRegistry registry = CatalogRegistry.seeded();
        Cart cart = cart(
                registry.cartType("TRAVELLER_CART"),
                List.of(upgradeInstance("EXTENDED_SPACE_TRAVELLERS"), upgradeInstance("FRIDGE")),
                List.of(),
                List.of());

        CalculationResult<EffectiveCart> result = service.calculateEffectiveCart(cart, registry, CampaignRuleState.seeded(CAMPAIGN_ID));

        assertEquals(BigDecimal.ZERO, result.value().passengerCapacity());
    }

    @Test
    void dailyConsumptionUsesEffectiveCartConsumption() {
        CampaignDay day = campaignDay();
        Cart cart = cart(
                CatalogRegistry.seeded().cartType("TRAVELLER_CART"),
                List.of(upgradeInstance("TWO_HORSE_TRAIN")),
                List.of(),
                List.of());
        Caravan caravan = caravan(
                List.of(),
                List.of(cart),
                List.of(),
                List.of(),
                1);

        CalculationResult<BigDecimal> result = service.calculateDailyConsumption(caravan, day, CaravanCalculationContext.empty());

        assertEquals(new BigDecimal("3"), result.value());
    }

    @Test
    void dailyConsumptionSupportsFastingEfficiencyAndCelebration() {
        CampaignDay day = campaignDay();
        Caravan caravan = caravan(
                List.of(traveller("Cook", new BigDecimal("1"), 4L, false, null)),
                List.of(cart(CatalogRegistry.seeded().cartType("TRAVELLER_CART"), List.of(), List.of(), List.of())),
                List.of(),
                List.of(),
                1);

        CaravanCalculationContext context = new CaravanCalculationContext(
                TravelContext.empty(),
                List.of("INTERMITTENT_FAST", "EFFICIENT_CONSUMPTION", "CELEBRATION"),
                true,
                true,
                false,
                null);

        CalculationResult<BigDecimal> result = service.calculateDailyConsumption(caravan, day, context);

        assertEquals(new BigDecimal("4.00"), result.value());
    }

    @Test
    void roleModifierIncludesHeroTeamworkAndExpertTravellerCap() {
        CatalogRegistry registry = CatalogRegistry.seeded();
        CampaignDay day = campaignDay();
        Caravan caravan = caravan(
                List.of(
                        traveller("Guard One", BigDecimal.ONE, 1L, false, dailyRole("GUARD", day.id(), null)),
                        traveller("Guard Two", BigDecimal.ONE, 1L, false, dailyRole("GUARD", day.id(), null)),
                        traveller("Hero", BigDecimal.ONE, 1L, false, dailyRole("HERO", day.id(), null))),
                List.of(),
                List.of(),
                List.of(),
                1);

        CaravanCalculationContext context = new CaravanCalculationContext(
                TravelContext.empty(),
                List.of("HERO", "TEAMWORK", "EXPERT_TRAVELLERS"),
                false,
                false,
                false,
                null);

        CalculationResult<Integer> result = service.calculateRoleModifier(caravan, day, com.jadecaravan.domain.campaign.CheckType.SECURITY, registry, context);

        assertEquals(5, result.value());
    }

    @Test
    void documentedSpeedTableIsUsedForKnownCreatureSpeeds() {
        CatalogRegistry registry = CatalogRegistry.seeded();
        CampaignDay day = campaignDay();
        UUID beastId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();

        Cart cart = cart(
                cartId,
                registry.cartType("TRAVELLER_CART"),
                List.of(),
                List.of(),
                List.of(new com.jadecaravan.domain.campaign.TowingAssignment(beastId, cartId, day.id())));
        Beast beast = beastForCart(beastId, cartId, day.id(), 10, 50);
        Caravan caravan = caravan(
                List.of(),
                List.of(cart),
                List.of(beast),
                List.of(),
                1);

        CalculationResult<BigDecimal> result = service.calculateSpeedMilesPerDay(caravan, registry, CampaignRuleState.seeded(CAMPAIGN_ID), CaravanCalculationContext.empty(), day.id());

        assertEquals(new BigDecimal("32"), result.value());
    }

    @Test
    void caravanSummaryUsesFrozenTerrainWhenCalculatingIceRunnerRequirement() {
        CatalogRegistry registry = CatalogRegistry.seeded();
        CampaignDay day = campaignDay();
        UUID beastId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();

        Cart cart = cart(
                cartId,
                registry.cartType("MUSEUM_CART"),
                List.of(upgradeInstance("ICE_RUNNERS")),
                List.of(),
                List.of(new com.jadecaravan.domain.campaign.TowingAssignment(beastId, cartId, day.id())));
        Beast beast = beastForCart(beastId, cartId, day.id(), 10, 50);
        Caravan caravan = caravan(
                List.of(),
                List.of(cart),
                List.of(beast),
                List.of(),
                1);
        CaravanCalculationContext context = new CaravanCalculationContext(
                new TravelContext("frozen", true, false, 0, BigDecimal.ZERO, BigDecimal.ZERO),
                List.of(),
                false,
                false,
                false,
                null);

        CalculationResult<CaravanCalculationSummary> result = service.calculateCaravanSummary(
                caravan,
                registry,
                CampaignRuleState.seeded(CAMPAIGN_ID),
                day,
                context);

        assertEquals(new BigDecimal("2.50"), result.value().requiredTowingStrength());
    }

    @Test
    void caravanSummaryAggregatesTheMainTotals() {
        CatalogRegistry registry = CatalogRegistry.seeded();
        CampaignDay day = campaignDay();
        UUID beastId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();

        Cart cart = cart(
                cartId,
                registry.cartType("TRAVELLER_CART"),
                List.of(upgradeInstance("IMPROVED_WHEELS")),
                List.of(),
                List.of(new com.jadecaravan.domain.campaign.TowingAssignment(beastId, cartId, day.id())));
        Beast beast = beastForCart(beastId, cartId, day.id(), 10, 40);
        Caravan caravan = caravan(
                List.of(traveller("Traveller", BigDecimal.ONE, 2L, false, null)),
                List.of(cart),
                List.of(beast),
                List.of(),
                1);

        CaravanCalculationContext context = new CaravanCalculationContext(
                TravelContext.empty(),
                List.of(),
                false,
                false,
                false,
                null);

        CalculationResult<CaravanCalculationSummary> result = service.calculateCaravanSummary(caravan, registry, CampaignRuleState.seeded(CAMPAIGN_ID), day, context);

        assertTrue(result.isSuccessful());
        assertEquals("rules-v1", result.value().ruleSetVersionId());
        assertFalse(result.value().breakdown().isEmpty());
    }

    private static Cart cart(
            CartTypeCatalogEntry cartType,
            List<CartUpgradeInstance> upgrades,
            List<CartCargoAllocation> cargoAllocations,
            List<com.jadecaravan.domain.campaign.TowingAssignment> towingAssignments) {
        return cart(UUID.randomUUID(), cartType, upgrades, cargoAllocations, towingAssignments);
    }

    private static Cart cart(
            UUID cartId,
            CartTypeCatalogEntry cartType,
            List<CartUpgradeInstance> upgrades,
            List<CartCargoAllocation> cargoAllocations,
            List<com.jadecaravan.domain.campaign.TowingAssignment> towingAssignments) {
        List<CartUpgradeInstance> normalizedUpgrades = upgrades.stream()
                .map(upgrade -> new CartUpgradeInstance(cartId, upgrade.upgrade(), upgrade.active(), upgrade.notes()))
                .toList();
        List<CartCargoAllocation> normalizedCargoAllocations = cargoAllocations.stream()
                .map(allocation -> new CartCargoAllocation(cartId, allocation.inventoryLotId(), allocation.quantity(), allocation.notes()))
                .toList();
        List<com.jadecaravan.domain.campaign.TowingAssignment> normalizedTowingAssignments = towingAssignments.stream()
                .map(assignment -> new com.jadecaravan.domain.campaign.TowingAssignment(
                        assignment.beastId(), cartId, assignment.campaignDayId(), assignment.consecutiveTowingDays()))
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
                new com.jadecaravan.domain.campaign.TowingAssignment(beastId, cartId, dayId),
                null);
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

    private static CartUpgradeInstance upgradeInstance(String key) {
        return new CartUpgradeInstance(UUID.randomUUID(), CatalogRegistry.seeded().upgrade(key), true, null);
    }

    private static final UUID CAMPAIGN_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID CARAVAN_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID DAY_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
}
