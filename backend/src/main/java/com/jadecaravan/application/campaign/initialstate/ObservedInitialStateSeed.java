package com.jadecaravan.application.campaign.initialstate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jadecaravan.domain.campaign.Beast;
import com.jadecaravan.domain.campaign.CampaignDailyCycleState;
import com.jadecaravan.domain.campaign.CampaignDay;
import com.jadecaravan.domain.campaign.CampaignDayActivityType;
import com.jadecaravan.domain.campaign.CampaignDayStatus;
import com.jadecaravan.domain.campaign.Caravan;
import com.jadecaravan.domain.campaign.CaravanFeatInstance;
import com.jadecaravan.domain.campaign.CaravanStats;
import com.jadecaravan.domain.campaign.Cart;
import com.jadecaravan.domain.campaign.CartCargoAllocation;
import com.jadecaravan.domain.campaign.CartPassengerAssignment;
import com.jadecaravan.domain.campaign.CartUpgradeInstance;
import com.jadecaravan.domain.campaign.InventoryLot;
import com.jadecaravan.domain.campaign.RoleCapability;
import com.jadecaravan.domain.campaign.TowingAssignment;
import com.jadecaravan.domain.campaign.Traveller;
import com.jadecaravan.domain.campaign.TravellerContract;
import com.jadecaravan.domain.catalog.BeastCatalogEntry;
import com.jadecaravan.domain.catalog.CatalogRegistry;
import com.jadecaravan.domain.catalog.CartCategory;
import com.jadecaravan.domain.catalog.CartTypeCatalogEntry;
import com.jadecaravan.domain.catalog.RoleCatalogEntry;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class ObservedInitialStateSeed {

    private static final String RESOURCE_PATH = "/initial-state/observed-campaign.json";
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final Snapshot snapshot;
    private final CatalogRegistry catalogRegistry;

    private ObservedInitialStateSeed(Snapshot snapshot, CatalogRegistry catalogRegistry) {
        this.snapshot = Objects.requireNonNull(snapshot, "snapshot must not be null");
        this.catalogRegistry = Objects.requireNonNull(catalogRegistry, "catalogRegistry must not be null");
    }

    public static ObservedInitialStateSeed create(CatalogRegistry catalogRegistry) {
        return new ObservedInitialStateSeed(loadSnapshot(), catalogRegistry);
    }

    public String campaignName() {
        return snapshot.campaignName();
    }

    public String ruleSetVersionId() {
        return snapshot.ruleSetVersionId();
    }

    public InitialStateSummary summary() {
        return snapshot.summary();
    }

    public List<String> warnings() {
        return snapshot.warnings();
    }

    public CampaignDailyCycleState buildState(UUID campaignId, String ruleSetVersionId) {
        Objects.requireNonNull(campaignId, "campaignId must not be null");
        if (ruleSetVersionId == null || ruleSetVersionId.isBlank()) {
            throw new IllegalArgumentException("ruleSetVersionId must not be blank");
        }

        UUID caravanId = deterministicId(campaignId, "caravan", snapshot.campaignName());
        UUID dayId = deterministicId(campaignId, "day", snapshot.activeDay().dayNumber() + ":" + snapshot.activeDay().location());

        Map<String, UUID> cartIds = new LinkedHashMap<>();
        Map<String, Cart> cartsByName = new LinkedHashMap<>();
        for (CartSeed cartSeed : snapshot.carts()) {
            UUID cartId = deterministicId(campaignId, "cart", cartSeed.name());
            cartIds.put(cartSeed.name(), cartId);
            cartsByName.put(cartSeed.name(), buildCart(caravanId, cartId, cartSeed));
        }

        Map<String, UUID> travellerIds = new LinkedHashMap<>();
        Map<String, Traveller> travellersByName = new LinkedHashMap<>();
        for (TravellerSeed travellerSeed : snapshot.travellers()) {
            UUID travellerId = deterministicId(campaignId, "traveller", travellerSeed.name());
            travellerIds.put(travellerSeed.name(), travellerId);
            travellersByName.put(travellerSeed.name(), buildTraveller(caravanId, travellerId, travellerSeed));
        }

        Map<String, UUID> inventoryLotIds = new LinkedHashMap<>();
        List<InventoryLot> inventoryLots = new ArrayList<>();
        for (InventoryLotSeed inventoryLotSeed : snapshot.inventoryLots()) {
            UUID inventoryLotId = deterministicId(campaignId, "inventory-lot", inventoryLotSeed.name() + ":" + inventoryLotSeed.cartName());
            inventoryLotIds.put(inventoryLotSeed.name(), inventoryLotId);
            inventoryLots.add(buildInventoryLot(caravanId, cartIds, inventoryLotId, inventoryLotSeed));
        }

        Map<String, UUID> beastIds = new LinkedHashMap<>();
        Map<String, Beast> beastsByName = new LinkedHashMap<>();
        for (BeastSeed beastSeed : snapshot.beasts()) {
            UUID beastId = deterministicId(campaignId, "beast", beastSeed.name());
            beastIds.put(beastSeed.name(), beastId);
            beastsByName.put(beastSeed.name(), buildBeast(caravanId, dayId, cartIds, beastId, beastSeed));
        }

        List<Cart> carts = new ArrayList<>();
        for (CartSeed cartSeed : snapshot.carts()) {
            Cart cart = cartsByName.get(cartSeed.name());
            for (TravellerSeed travellerSeed : snapshot.travellers()) {
                if (cartSeed.name().equals(travellerSeed.cartName()) && travellersByName.containsKey(travellerSeed.name())) {
                    Traveller traveller = travellersByName.get(travellerSeed.name());
                    cart = cart.withPassengerAssignment(new CartPassengerAssignment(
                            cart.id(),
                            traveller.id(),
                            BigDecimal.valueOf(travellerSeed.occupancyUnits()),
                            travellerSeed.description()));
                }
            }
            for (InventoryLotSeed inventoryLotSeed : snapshot.inventoryLots()) {
                if (cartSeed.name().equals(inventoryLotSeed.cartName())) {
                    UUID inventoryLotId = inventoryLotIds.get(inventoryLotSeed.name());
                    cart = cart.withCargoAllocation(new CartCargoAllocation(
                            cart.id(),
                            inventoryLotId,
                            BigDecimal.valueOf(inventoryLotSeed.quantity()),
                            inventoryLotSeed.metadata().getOrDefault("sourceName", inventoryLotSeed.cargoTypeId())));
                }
            }
            for (BeastSeed beastSeed : snapshot.beasts()) {
                if (cartSeed.name().equals(beastSeed.towingCartName())) {
                    UUID beastId = beastIds.get(beastSeed.name());
                    cart = cart.withTowingAssignment(new TowingAssignment(beastId, cart.id(), dayId));
                }
            }
            carts.add(cart);
        }

        List<Traveller> travellers = new ArrayList<>(travellersByName.values());
        List<Beast> beasts = new ArrayList<>(beastsByName.values());

        CampaignDay campaignDay = new CampaignDay(
                dayId,
                caravanId,
                snapshot.activeDay().dayNumber(),
                CampaignDayStatus.valueOf(snapshot.activeDay().status()),
                CampaignDayActivityType.valueOf(snapshot.activeDay().activityType()),
                snapshot.activeDay().terrainType(),
                snapshot.activeDay().location(),
                snapshot.activeDay().settlementType(),
                snapshot.activeDay().temperatureF(),
                snapshot.activeDay().weatherSeverity(),
                BigDecimal.valueOf(snapshot.activeDay().travelHours()),
                BigDecimal.valueOf(snapshot.activeDay().plannedDistanceMiles()),
                null,
                List.of(),
                List.of(),
                List.of());

        Caravan caravan = new Caravan(
                caravanId,
                campaignId,
                snapshot.campaignName(),
                1,
                ruleSetVersionId,
                new CaravanStats(
                        snapshot.baseStats().offense(),
                        snapshot.baseStats().defense(),
                        snapshot.baseStats().mobility(),
                        snapshot.baseStats().morale()),
                ZERO,
                snapshot.activeDay().dayNumber(),
                travellers,
                carts,
                beasts,
                inventoryLots,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                ZERO,
                ZERO);
        caravan = caravan.withCampaignDay(campaignDay);

        return CampaignDailyCycleState.seeded(campaignId, caravan, dayId, null);
    }

    private Cart buildCart(UUID caravanId, UUID cartId, CartSeed cartSeed) {
        CartTypeCatalogEntry cartType = new CartTypeCatalogEntry(
                slugKey("cart", cartSeed.name()),
                cartSeed.typeName(),
                CartCategory.valueOf(cartSeed.category()),
                "observed",
                null,
                cartSeed.maxHitPoints(),
                cartSeed.hardness(),
                cartSeed.propulsionRequirement(),
                cartSeed.towingCreatureLimit(),
                BigDecimal.valueOf(cartSeed.consumption()),
                BigDecimal.valueOf(cartSeed.passengerCapacity()),
                BigDecimal.valueOf(cartSeed.cargoCapacity()),
                List.of(),
                cartSeed.notes() == null ? List.of() : List.of(cartSeed.notes()),
                true,
                "docs/09-estado-inicial.md",
                "Observed current snapshot imported from the Excel workbook");

        List<CartUpgradeInstance> upgrades = cartSeed.upgrades().stream()
                .map(upgradeKey -> new CartUpgradeInstance(
                        cartId,
                        catalogRegistry.upgrade(upgradeKey),
                        true,
                        "Observed upgrade from the Excel snapshot"))
                .toList();

        return new Cart(
                cartId,
                caravanId,
                cartSeed.name(),
                cartType,
                cartSeed.currentHitPoints(),
                false,
                cartSeed.notes(),
                List.of(),
                upgrades,
                List.of(),
                List.of(),
                List.of());
    }

    private Traveller buildTraveller(UUID caravanId, UUID travellerId, TravellerSeed travellerSeed) {
        TravellerContract contract = travellerSeed.salaryCp() > 0
                ? new TravellerContract(
                        "observed-snapshot",
                        travellerSeed.salaryCp(),
                        true,
                        travellerSeed.description(),
                        Instant.EPOCH)
                : null;

        List<RoleCapability> roleCapabilities = travellerSeed.roleCapabilities().stream()
                .map(role -> new RoleCapability(customRole(role), "docs/09-estado-inicial.md", "Observed role capability from the Excel snapshot"))
                .toList();

        return new Traveller(
                travellerId,
                caravanId,
                travellerSeed.name(),
                travellerSeed.playerCharacter(),
                travellerSeed.humanoid(),
                travellerSeed.humanoid() ? "Medium" : "Large",
                Math.max(0L, Math.round(travellerSeed.foodConsumption())),
                BigDecimal.valueOf(travellerSeed.occupancyUnits()),
                travellerSeed.countsAsTraveller(),
                travellerSeed.needsRest(),
                travellerSeed.needsFood(),
                travellerSeed.baseAttackBonus(),
                travellerSeed.hitDice(),
                true,
                true,
                travellerSeed.status(),
                contract,
                List.of(),
                roleCapabilities,
                List.of());
    }

    private InventoryLot buildInventoryLot(UUID caravanId, Map<String, UUID> cartIds, UUID inventoryLotId, InventoryLotSeed inventoryLotSeed) {
        UUID cartId = cartIds.get(inventoryLotSeed.cartName());
        return new InventoryLot(
                inventoryLotId,
                caravanId,
                inventoryLotSeed.cargoTypeId(),
                BigDecimal.valueOf(inventoryLotSeed.quantity()),
                BigDecimal.valueOf(inventoryLotSeed.unitCapacity()),
                inventoryLotSeed.unitValueCp(),
                cartId,
                inventoryLotSeed.originSettlementId(),
                inventoryLotSeed.remainingProvisions() == null ? null : BigDecimal.valueOf(inventoryLotSeed.remainingProvisions()),
                BigDecimal.valueOf(inventoryLotSeed.perishableDecayProgress()),
                inventoryLotSeed.metadata());
    }

    private Beast buildBeast(UUID caravanId, UUID campaignDayId, Map<String, UUID> cartIds, UUID beastId, BeastSeed beastSeed) {
        BeastCatalogEntry beastType = catalogRegistry.beast(resolveBeastTypeKey(beastSeed.typeKey(), beastSeed.typeName()));
        TowingAssignment towingAssignment = null;
        if (beastSeed.activeAsTowing() && beastSeed.towingCartName() != null) {
            towingAssignment = new TowingAssignment(beastId, cartIds.get(beastSeed.towingCartName()), campaignDayId);
        }
        return new Beast(
                beastId,
                caravanId,
                beastType,
                beastSeed.name(),
                beastSeed.currentHitPoints(),
                beastSeed.trainedForCombat(),
                beastSeed.fatigued(),
                beastSeed.activeAsTowing(),
                towingAssignment,
                beastSeed.notes());
    }

    private static RoleCatalogEntry customRole(String roleName) {
        String normalized = roleName == null || roleName.isBlank() ? "observed_role" : roleName.trim();
        return new RoleCatalogEntry(
                slugKey("role", normalized),
                normalized,
                "observed",
                "spreadsheet snapshot",
                "Observed role or capability from the Excel snapshot",
                false,
                true,
                "docs/09-estado-inicial.md",
                "Imported as a custom role capability because the snapshot only exposes visible labels");
    }

    private static String resolveBeastTypeKey(String observedTypeKey, String observedTypeName) {
        if (observedTypeKey != null) {
            String normalizedKey = observedTypeKey.trim().toUpperCase();
            if ("CABALLO_YAKUTO".equals(normalizedKey)) {
                return "YAKUTO_HORSE";
            }
            return normalizedKey;
        }
        if (observedTypeName != null && observedTypeName.equalsIgnoreCase("Caballo yakuto")) {
            return "YAKUTO_HORSE";
        }
        return "YAK";
    }

    private static String slugKey(String prefix, String value) {
        return (prefix + "_" + slug(value)).toUpperCase();
    }

    private static String slug(String value) {
        if (value == null || value.isBlank()) {
            return "observed";
        }
        String normalized = value.trim().toLowerCase();
        StringBuilder builder = new StringBuilder(normalized.length());
        boolean previousDash = false;
        for (int index = 0; index < normalized.length(); index++) {
            char character = normalized.charAt(index);
            if (Character.isLetterOrDigit(character)) {
                builder.append(character);
                previousDash = false;
            } else if (!previousDash) {
                builder.append('-');
                previousDash = true;
            }
        }
        String result = builder.toString().replaceAll("^-+|-+$", "");
        return result.isBlank() ? "observed" : result;
    }

    private static UUID deterministicId(UUID campaignId, String namespace, String value) {
        return UUID.nameUUIDFromBytes((campaignId + ":" + namespace + ":" + value).getBytes(StandardCharsets.UTF_8));
    }

    private static Snapshot loadSnapshot() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        try (InputStream inputStream = ObservedInitialStateSeed.class.getResourceAsStream(RESOURCE_PATH)) {
            if (inputStream == null) {
                throw new IllegalStateException("Initial state snapshot resource not found: " + RESOURCE_PATH);
            }
            return objectMapper.readValue(inputStream, Snapshot.class);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not read initial state snapshot", ex);
        }
    }

    public record Snapshot(
            String campaignName,
            String ruleSetVersionId,
            InitialStateSummary summary,
            List<String> warnings,
            BaseStats baseStats,
            ActiveDay activeDay,
            List<CartSeed> carts,
            List<TravellerSeed> travellers,
            List<BeastSeed> beasts,
            List<InventoryLotSeed> inventoryLots) {

        public Snapshot {
            warnings = List.copyOf(warnings);
            carts = List.copyOf(carts);
            travellers = List.copyOf(travellers);
            beasts = List.copyOf(beasts);
            inventoryLots = List.copyOf(inventoryLots);
        }
    }

    public record InitialStateSummary(
            int cartCount,
            int travellerCount,
            int beastCount,
            int consumptionTotal,
            int salaryTotalVisible,
            int salaryTotalCp,
            String travellersTransported) {
    }

    public record BaseStats(int offense, int defense, int mobility, int morale) {
    }

    public record ActiveDay(
            int dayNumber,
            String status,
            String activityType,
            String location,
            String place,
            String destination,
            int temperatureF,
            String weatherSeverity,
            double travelHours,
            double plannedDistanceMiles,
            String terrainType,
            String settlementType,
            int plannedTension) {
    }

    public record CartSeed(
            String name,
            String typeName,
            String category,
            String notes,
            int currentHitPoints,
            int maxHitPoints,
            int hardness,
            int propulsionRequirement,
            String towingCreatureLimit,
            double consumption,
            double passengerCapacity,
            double cargoCapacity,
            double currentPassengerOccupancy,
            double currentPassengerCapacity,
            double currentCargoOccupancy,
            double currentCargoCapacity,
            List<String> upgrades) {

        public CartSeed {
            upgrades = List.copyOf(upgrades);
        }
    }

    public record TravellerSeed(
            String name,
            String description,
            String currentRole,
            String additionalRole,
            String cartName,
            double foodConsumption,
            double occupancyUnits,
            double salaryVisible,
            int salaryCp,
            String bed,
            boolean playerCharacter,
            boolean humanoid,
            boolean countsAsTraveller,
            boolean needsRest,
            boolean needsFood,
            int baseAttackBonus,
            int hitDice,
            String status,
            List<String> roleCapabilities) {

        public TravellerSeed {
            roleCapabilities = List.copyOf(roleCapabilities);
        }
    }

    public record BeastSeed(
            String name,
            String typeName,
            String typeKey,
            int currentHitPoints,
            boolean trainedForCombat,
            boolean fatigued,
            boolean activeAsTowing,
            String towingCartName,
            String notes) {
    }

    public record InventoryLotSeed(
            String name,
            String cartName,
            String cargoTypeId,
            int quantity,
            double unitCapacity,
            long unitValueCp,
            UUID originSettlementId,
            Double remainingProvisions,
            double perishableDecayProgress,
            Map<String, String> metadata) {

        public InventoryLotSeed {
            metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        }
    }
}
