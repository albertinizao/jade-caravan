package com.jadecaravan.domain.calculation;

import com.jadecaravan.domain.campaign.Beast;
import com.jadecaravan.domain.campaign.CampaignDay;
import com.jadecaravan.domain.campaign.Cart;
import com.jadecaravan.domain.campaign.CartCargoAllocation;
import com.jadecaravan.domain.campaign.CartPassengerAssignment;
import com.jadecaravan.domain.campaign.CartUpgradeInstance;
import com.jadecaravan.domain.campaign.Caravan;
import com.jadecaravan.domain.campaign.CaravanStats;
import com.jadecaravan.domain.campaign.CheckModifier;
import com.jadecaravan.domain.campaign.CheckOutcome;
import com.jadecaravan.domain.campaign.CheckResolution;
import com.jadecaravan.domain.campaign.CheckType;
import com.jadecaravan.domain.campaign.DailyRoleAssignment;
import com.jadecaravan.domain.campaign.InventoryLot;
import com.jadecaravan.domain.campaign.RoleCapability;
import com.jadecaravan.domain.campaign.TowingAssignment;
import com.jadecaravan.domain.campaign.Traveller;
import com.jadecaravan.domain.catalog.CargoCatalogEntry;
import com.jadecaravan.domain.catalog.CatalogName;
import com.jadecaravan.domain.catalog.CatalogRegistry;
import com.jadecaravan.domain.catalog.CartTypeCatalogEntry;
import com.jadecaravan.domain.catalog.UpgradeCatalogEntry;
import com.jadecaravan.domain.rules.CampaignRuleState;
import com.jadecaravan.domain.rules.RuleDecisionKey;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class CaravanBusinessRules {

    private static final BigDecimal EXTENDED_SPACE_FACTOR = new BigDecimal("0.25");
    private static final BigDecimal FRIDGE_PASSENGER_CAPACITY = BigDecimal.ZERO;
    private static final BigDecimal DEFAULT_SUPPLY_YIELD_PER_UNIT = new BigDecimal("10");
    private static final BigDecimal COOK_SUPPLY_YIELD_PER_UNIT = new BigDecimal("15");
    private static final BigDecimal LARGE_OR_LARGER_STRENGTH_MULTIPLIER = new BigDecimal("2");
    private static final BigDecimal DEFAULT_TOWING_SPEED_UNIT_DIVISOR = new BigDecimal("10");

    private CaravanBusinessRules() {
    }

    public static CalculationResult<BigDecimal> calculatePassengerCapacity(
            Caravan caravan,
            CatalogRegistry catalogRegistry,
            CampaignRuleState ruleState) {
        Objects.requireNonNull(caravan, "caravan must not be null");
        Objects.requireNonNull(catalogRegistry, "catalogRegistry must not be null");

        RoundingMode roundingMode = resolveExtendedSpaceRounding(ruleState);
        List<CalculationBreakdownItem> breakdown = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (Cart cart : caravan.operativeCarts()) {
            BigDecimal cartCapacity = effectivePassengerCapacity(cart, roundingMode);
            breakdown.add(new CalculationBreakdownItem(
                    "Passenger capacity from " + cart.name(),
                    cartCapacity,
                    cart.cartType().source(),
                    cart.cartType().name()));
            total = total.add(cartCapacity);
        }

        return new CalculationResult<>(total, breakdown, List.of(), List.of(), caravan.ruleSetVersionId());
    }

    public static CalculationResult<BigDecimal> calculateCargoCapacity(
            Caravan caravan,
            CatalogRegistry catalogRegistry,
            CampaignRuleState ruleState) {
        Objects.requireNonNull(caravan, "caravan must not be null");
        Objects.requireNonNull(catalogRegistry, "catalogRegistry must not be null");

        RoundingMode roundingMode = resolveExtendedSpaceRounding(ruleState);
        List<CalculationBreakdownItem> breakdown = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (Cart cart : caravan.operativeCarts()) {
            BigDecimal cartCapacity = effectiveCargoCapacity(cart, roundingMode);
            breakdown.add(new CalculationBreakdownItem(
                    "Cargo capacity from " + cart.name(),
                    cartCapacity,
                    cart.cartType().source(),
                    cart.cartType().name()));
            total = total.add(cartCapacity);
        }

        return new CalculationResult<>(total, breakdown, List.of(), List.of(), caravan.ruleSetVersionId());
    }

    public static TravelValidationResult validateTravel(
            Caravan caravan,
            CatalogRegistry catalogRegistry,
            CampaignRuleState ruleState,
            CampaignDay campaignDay,
            TravelContext travelContext) {
        Objects.requireNonNull(caravan, "caravan must not be null");
        Objects.requireNonNull(catalogRegistry, "catalogRegistry must not be null");
        Objects.requireNonNull(campaignDay, "campaignDay must not be null");
        TravelContext context = travelContext == null ? TravelContext.empty() : travelContext;

        List<CalculationBreakdownItem> breakdown = new ArrayList<>();
        List<CalculationIssue> warnings = new ArrayList<>();
        List<CalculationIssue> blockers = new ArrayList<>();

        CalculationResult<BigDecimal> passengerCapacity = calculatePassengerCapacity(caravan, catalogRegistry, ruleState);
        CalculationResult<BigDecimal> cargoCapacity = calculateCargoCapacity(caravan, catalogRegistry, ruleState);
        CalculationResult<BigDecimal> towingStrength = calculateTowingStrength(caravan, catalogRegistry, campaignDay.id());
        CalculationResult<BigDecimal> requiredTowingStrength = calculateRequiredTowingStrength(
                caravan,
                catalogRegistry,
                context);

        breakdown.addAll(passengerCapacity.breakdown());
        breakdown.addAll(cargoCapacity.breakdown());
        breakdown.addAll(towingStrength.breakdown());
        breakdown.addAll(requiredTowingStrength.breakdown());

        BigDecimal passengerOccupancy = caravan.totalTravellerOccupancy();
        BigDecimal cargoOccupancy = caravan.totalCargoOccupancy();

        breakdown.add(new CalculationBreakdownItem(
                "Passenger occupancy",
                passengerOccupancy,
                "caravan.travelers",
                "Sum of travellers that count as travellers"));
        breakdown.add(new CalculationBreakdownItem(
                "Cargo occupancy",
                cargoOccupancy,
                "caravan.inventoryLots",
                "Quantity multiplied by unit capacity"));

        if (passengerOccupancy.compareTo(passengerCapacity.value()) > 0) {
            blockers.add(issue(
                    BusinessRuleCode.PASSENGER_CAPACITY_EXCEEDED,
                    "Passenger occupancy exceeds effective passenger capacity",
                    "caravan",
                    Map.of(
                            "occupancy", passengerOccupancy.toPlainString(),
                            "capacity", passengerCapacity.value().toPlainString())));
        }
        if (cargoOccupancy.compareTo(cargoCapacity.value()) > 0) {
            blockers.add(issue(
                    BusinessRuleCode.CARGO_CAPACITY_EXCEEDED,
                    "Cargo occupancy exceeds effective cargo capacity",
                    "caravan",
                    Map.of(
                            "occupancy", cargoOccupancy.toPlainString(),
                            "capacity", cargoCapacity.value().toPlainString())));
        }

        for (Cart cart : caravan.operativeCarts()) {
            List<CalculationIssue> cartIssues = validateCartRestrictions(caravan, catalogRegistry, campaignDay, cart);
            blockers.addAll(cartIssues);

            if (!hasDriverForCart(caravan, campaignDay, cart)) {
                blockers.add(issue(
                        BusinessRuleCode.MISSING_DRIVER,
                        "Operative cart has no daily assigned driver",
                        cart.id().toString(),
                        Map.of("cart", cart.name())));
            }

            BigDecimal cartTowing = towingStrengthForCart(caravan, catalogRegistry, campaignDay.id(), cart);
            BigDecimal cartRequiredTowing = requiredTowingStrengthForCart(cart, context);
            if (cartTowing.compareTo(cartRequiredTowing) < 0) {
                blockers.add(issue(
                        BusinessRuleCode.INSUFFICIENT_TOWING_STRENGTH,
                        "Towing strength is below the cart requirement",
                        cart.id().toString(),
                        Map.of(
                                "towingStrength", cartTowing.toPlainString(),
                                "requiredStrength", cartRequiredTowing.toPlainString())));
            }

            if (cartTowing.compareTo(cartRequiredTowing.multiply(new BigDecimal("2"))) >= 0) {
                // no fatigue from towing strain
            } else {
                int thresholdDays = towingFatigueThresholdDays(cartTowing, cartRequiredTowing);
                int consecutiveDays = currentTowingStreak(caravan, campaignDay.id(), cart);
                if (thresholdDays > 0 && consecutiveDays >= thresholdDays) {
                    warnings.add(issue(
                            BusinessRuleCode.TOWING_FATIGUE_THRESHOLD_REACHED,
                            "Towing fatigue threshold reached for cart/beast assignment",
                            cart.id().toString(),
                            Map.of(
                                    "consecutiveDays", Integer.toString(consecutiveDays),
                                    "thresholdDays", Integer.toString(thresholdDays))));
                }
            }
        }

        return new TravelValidationResult(
                blockers.isEmpty(),
                passengerOccupancy,
                passengerCapacity.value(),
                cargoOccupancy,
                cargoCapacity.value(),
                towingStrength.value(),
                requiredTowingStrength.value(),
                breakdown,
                warnings,
                blockers,
                caravan.ruleSetVersionId());
    }

    public static CalculationResult<BigDecimal> calculateTowingStrength(
            Caravan caravan,
            CatalogRegistry catalogRegistry,
            UUID campaignDayId) {
        Objects.requireNonNull(caravan, "caravan must not be null");
        Objects.requireNonNull(catalogRegistry, "catalogRegistry must not be null");
        Objects.requireNonNull(campaignDayId, "campaignDayId must not be null");

        List<CalculationBreakdownItem> breakdown = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (Cart cart : caravan.operativeCarts()) {
            for (TowingAssignment assignment : cart.towingAssignments()) {
                if (!campaignDayId.equals(assignment.campaignDayId())) {
                    continue;
                }
                Beast beast = caravan.findBeast(assignment.beastId()).orElse(null);
                if (beast == null || !beast.activeAsTowing()) {
                    continue;
                }
                BigDecimal beastStrength = BigDecimal.valueOf(beast.strength());
                if (isLargeOrLarger(beast)) {
                    beastStrength = beastStrength.multiply(LARGE_OR_LARGER_STRENGTH_MULTIPLIER);
                    breakdown.add(new CalculationBreakdownItem(
                            "Double towing strength for large or larger beast " + beast.name(),
                            beastStrength,
                            beast.beastType().source(),
                            beast.beastType().adaptationNotes()));
                } else {
                    breakdown.add(new CalculationBreakdownItem(
                            "Towing strength for beast " + beast.name(),
                            beastStrength,
                            beast.beastType().source(),
                            beast.beastType().adaptationNotes()));
                }
                total = total.add(beastStrength);
            }
        }

        return new CalculationResult<>(total, breakdown, List.of(), List.of(), caravan.ruleSetVersionId());
    }

    public static CalculationResult<BigDecimal> calculateRequiredTowingStrength(
            Caravan caravan,
            CatalogRegistry catalogRegistry) {
        return calculateRequiredTowingStrength(caravan, catalogRegistry, TravelContext.empty());
    }

    public static CalculationResult<BigDecimal> calculateRequiredTowingStrength(
            Caravan caravan,
            CatalogRegistry catalogRegistry,
            TravelContext travelContext) {
        Objects.requireNonNull(caravan, "caravan must not be null");
        Objects.requireNonNull(catalogRegistry, "catalogRegistry must not be null");
        TravelContext context = travelContext == null ? TravelContext.empty() : travelContext;

        List<CalculationBreakdownItem> breakdown = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Cart cart : caravan.operativeCarts()) {
            BigDecimal required = requiredTowingStrengthForCart(cart, context);
            breakdown.add(new CalculationBreakdownItem(
                    "Base propulsion requirement for " + cart.name(),
                    required,
                    cart.cartType().source(),
                    cart.cartType().name()));
            total = total.add(required);
        }
        return new CalculationResult<>(total, breakdown, List.of(), List.of(), caravan.ruleSetVersionId());
    }

    public static CalculationResult<BigDecimal> calculateSpeedMilesPerDay(
            Caravan caravan,
            CatalogRegistry catalogRegistry,
            CampaignRuleState ruleState,
            TravelContext travelContext,
            UUID campaignDayId) {
        Objects.requireNonNull(caravan, "caravan must not be null");
        Objects.requireNonNull(catalogRegistry, "catalogRegistry must not be null");
        Objects.requireNonNull(campaignDayId, "campaignDayId must not be null");
        TravelContext context = travelContext == null ? TravelContext.empty() : travelContext;

        List<CalculationBreakdownItem> breakdown = new ArrayList<>();
        List<CalculationIssue> warnings = new ArrayList<>();
        List<CalculationIssue> blockers = new ArrayList<>();

        BigDecimal baseSpeed = slowestTowingCreatureSpeedMiles(caravan, campaignDayId);
        breakdown.add(new CalculationBreakdownItem(
                "Base speed from slowest towing creature",
                baseSpeed,
                "caravan.beasts",
                "Lowest towing speed converted to miles/day"));

        BigDecimal speed = baseSpeed;

        if (allOperativeCartsHaveUpgrade(caravan, "IMPROVED_WHEELS")) {
            speed = speed.add(BigDecimal.valueOf(8));
            breakdown.add(new CalculationBreakdownItem(
                    "All operative carts have improved wheels",
                    BigDecimal.valueOf(8),
                    "docs/04-catalogos.md#2",
                    "+8 miles/day"));
        }

        long coldInsulationCount = caravan.operativeCarts().stream()
                .filter(cart -> cart.hasActiveUpgrade("COLD_INSULATION"))
                .count();
        if (coldInsulationCount > 0) {
            speed = speed.subtract(BigDecimal.valueOf(4));
            breakdown.add(new CalculationBreakdownItem(
                    "Cold insulation penalty",
                    BigDecimal.valueOf(-4),
                    "docs/04-catalogos.md#2",
                    "Applied once if any cart has cold insulation"));
        }

        long heatInsulationCount = caravan.operativeCarts().stream()
                .filter(cart -> cart.hasActiveUpgrade("HEAT_INSULATION"))
                .count();
        if (heatInsulationCount > 0) {
            BigDecimal heatPenalty = BigDecimal.valueOf(heatInsulationCount).multiply(BigDecimal.valueOf(-1));
            speed = speed.add(heatPenalty);
            breakdown.add(new CalculationBreakdownItem(
                    "Heat insulation penalty",
                    heatPenalty,
                    "docs/04-catalogos.md#2",
                    "Applied once per cart with heat insulation"));
        }

        if (context.flatSpeedBonusMilesPerDay().signum() != 0) {
            speed = speed.add(context.flatSpeedBonusMilesPerDay());
            breakdown.add(new CalculationBreakdownItem(
                    "Context speed bonus",
                    context.flatSpeedBonusMilesPerDay(),
                    "travel-context",
                    "Custom campaign or terrain bonus"));
        }

        if (context.flatSpeedPenaltyMilesPerDay().signum() != 0) {
            speed = speed.subtract(context.flatSpeedPenaltyMilesPerDay());
            breakdown.add(new CalculationBreakdownItem(
                    "Context speed penalty",
                    context.flatSpeedPenaltyMilesPerDay().negate(),
                    "travel-context",
                    "Custom campaign or terrain penalty"));
        }

        if (context.nightTravel()) {
            breakdown.add(new CalculationBreakdownItem(
                    "Night travel",
                    BigDecimal.ZERO,
                    "travel-context",
                    "Night travel context acknowledged"));
        }

        if (speed.signum() < 0) {
            speed = BigDecimal.ZERO;
        }

        return new CalculationResult<>(speed, breakdown, warnings, blockers, caravan.ruleSetVersionId());
    }

    public static CalculationResult<BigDecimal> calculateDailyConsumption(
            Caravan caravan,
            CampaignDay campaignDay) {
        Objects.requireNonNull(caravan, "caravan must not be null");
        Objects.requireNonNull(campaignDay, "campaignDay must not be null");

        List<CalculationBreakdownItem> breakdown = new ArrayList<>();
        BigDecimal travellerConsumption = caravan.travellers().stream()
                .filter(Traveller::countsAsTraveller)
                .filter(Traveller::needsFood)
                .filter(traveller -> !traveller.isScoutOn(campaignDay.id()))
                .map(traveller -> BigDecimal.valueOf(traveller.foodConsumption()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        breakdown.add(new CalculationBreakdownItem(
                "Traveller consumption",
                travellerConsumption,
                "caravan.travellers",
                "Scouts excluded"));

        BigDecimal cartConsumption = caravan.operativeCarts().stream()
                .map(cart -> BigDecimal.valueOf(cart.cartType().consumption().longValue()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        breakdown.add(new CalculationBreakdownItem(
                "Operative cart consumption",
                cartConsumption,
                "caravan.carts",
                "Sum of operative cart consumption"));

        BigDecimal total = travellerConsumption.add(cartConsumption);
        return new CalculationResult<>(total, breakdown, List.of(), List.of(), caravan.ruleSetVersionId());
    }

    public static CalculationResult<BigDecimal> calculateSupplyYield(
            Caravan caravan,
            CampaignDay campaignDay,
            int supplyUnitsConsumed) {
        Objects.requireNonNull(caravan, "caravan must not be null");
        Objects.requireNonNull(campaignDay, "campaignDay must not be null");
        if (supplyUnitsConsumed < 0) {
            throw new IllegalArgumentException("supplyUnitsConsumed must not be negative");
        }

        int cookCount = activeRoleCount(caravan, campaignDay.id(), "COOK");
        BigDecimal yieldPerUnit = cookCount > 0 ? COOK_SUPPLY_YIELD_PER_UNIT : DEFAULT_SUPPLY_YIELD_PER_UNIT;
        BigDecimal totalYield = yieldPerUnit.multiply(BigDecimal.valueOf(supplyUnitsConsumed));

        List<CalculationBreakdownItem> breakdown = List.of(new CalculationBreakdownItem(
                "Supply conversion",
                totalYield,
                "docs/04-catalogos.md#4",
                cookCount > 0 ? "At least one cook increases yield" : "Base supply yield"));
        return new CalculationResult<>(totalYield, breakdown, List.of(), List.of(), caravan.ruleSetVersionId());
    }

    public static InventoryLot advancePerishableLot(
            InventoryLot lot,
            int days,
            boolean protectedByFridge) {
        Objects.requireNonNull(lot, "lot must not be null");
        if (days < 0) {
            throw new IllegalArgumentException("days must not be negative");
        }
        if (protectedByFridge || days == 0 || !isPerishableLot(lot)) {
            return lot;
        }

        BigDecimal currentRemaining = lot.remainingProvisions() == null ? BigDecimal.ZERO : lot.remainingProvisions();
        BigDecimal currentProgress = lot.perishableDecayProgress() == null ? BigDecimal.ZERO : lot.perishableDecayProgress();
        BigDecimal progress = currentProgress.add(BigDecimal.valueOf(days));
        BigDecimal losses = progress.divideToIntegralValue(BigDecimal.valueOf(2));
        BigDecimal nextProgress = progress.remainder(BigDecimal.valueOf(2));
        BigDecimal nextRemaining = currentRemaining.subtract(losses);
        if (nextRemaining.signum() < 0) {
            nextRemaining = BigDecimal.ZERO;
        }

        return lot.withRemainingProvisions(nextRemaining).withPerishableDecayProgress(nextProgress);
    }

    public static CalculationResult<BigDecimal> calculateMutinyPenalty(Caravan caravan) {
        Objects.requireNonNull(caravan, "caravan must not be null");
        BigDecimal effectiveMorale = BigDecimal.valueOf(caravan.baseStats().morale());
        BigDecimal penalty = caravan.currentDiscontent().subtract(effectiveMorale);
        if (penalty.signum() < 0) {
            penalty = BigDecimal.ZERO;
        }
        List<CalculationBreakdownItem> breakdown = List.of(new CalculationBreakdownItem(
                "Mutiny penalty",
                penalty.negate(),
                "caravan.currentDiscontent",
                "Applied when discontent exceeds morale"));
        return new CalculationResult<>(penalty.negate(), breakdown, List.of(), List.of(), caravan.ruleSetVersionId());
    }

    public static CheckResolution resolveCheck(
            Caravan caravan,
            CheckType checkType,
            int dc,
            Integer naturalRoll,
            List<CheckModifier> additionalModifiers,
            CampaignDay campaignDay) {
        Objects.requireNonNull(caravan, "caravan must not be null");
        Objects.requireNonNull(checkType, "checkType must not be null");
        Objects.requireNonNull(campaignDay, "campaignDay must not be null");
        List<CheckModifier> modifiers = new ArrayList<>();
        if (additionalModifiers != null) {
            modifiers.addAll(additionalModifiers);
        }

        int baseValue = switch (checkType) {
            case ATTACK -> caravan.baseStats().offense();
            case ARMOR_CLASS -> caravan.baseStats().defense();
            case SECURITY -> caravan.baseStats().mobility();
            case DETERMINATION -> caravan.baseStats().morale();
            default -> 0;
        };

        if (caravan.currentDiscontent().compareTo(BigDecimal.valueOf(caravan.baseStats().morale())) > 0) {
            BigDecimal mutinyPenalty = calculateMutinyPenalty(caravan).value();
            modifiers.add(new CheckModifier("Mutiny", mutinyPenalty.intValue(), true, "Discontent above morale"));
        }

        int modifierTotal = modifiers.stream()
                .filter(CheckModifier::applied)
                .mapToInt(CheckModifier::value)
                .sum();
        Integer total = naturalRoll == null ? null : naturalRoll + baseValue + modifierTotal;

        CheckOutcome outcome = naturalRoll == null
                ? CheckOutcome.MANUAL
                : resolveOutcome(naturalRoll, total, dc);

        return new CheckResolution(
                java.util.UUID.randomUUID(),
                campaignDay.id(),
                checkType,
                modifiers,
                dc,
                naturalRoll,
                total,
                outcome,
                "Resolved by business rules");
    }

    public static int calculateRepairHitPoints(Caravan caravan) {
        Objects.requireNonNull(caravan, "caravan must not be null");
        return caravan.level() * 15;
    }

    public static boolean isMutinyTriggered(Caravan caravan) {
        Objects.requireNonNull(caravan, "caravan must not be null");
        return caravan.currentDiscontent().compareTo(BigDecimal.valueOf(caravan.baseStats().morale())) >= 0;
    }

    public static int calculateDestructionCrewDamage(int d10RollOne, int d10RollTwo) {
        if (d10RollOne < 1 || d10RollOne > 10 || d10RollTwo < 1 || d10RollTwo > 10) {
            throw new IllegalArgumentException("d10 rolls must be between 1 and 10");
        }
        return d10RollOne + d10RollTwo + 5;
    }

    public static CalculationResult<BigDecimal> calculateLongTermCareMultiplier(Cart cart) {
        Objects.requireNonNull(cart, "cart must not be null");
        BigDecimal multiplier = isMedicalCart(cart) ? BigDecimal.valueOf(2) : BigDecimal.ONE;
        return new CalculationResult<>(
                multiplier,
                List.of(new CalculationBreakdownItem(
                        "Long-term care multiplier",
                        multiplier,
                        cart.cartType().source(),
                        isMedicalCart(cart) ? "Medical cart doubles long-term care" : "Standard care")),
                List.of(),
                List.of(),
                cart.cartType().key());
    }

    public static List<CalculationIssue> validateCartRestrictions(
            Caravan caravan,
            CatalogRegistry catalogRegistry,
            CampaignDay campaignDay,
            Cart cart) {
        Objects.requireNonNull(caravan, "caravan must not be null");
        Objects.requireNonNull(catalogRegistry, "catalogRegistry must not be null");
        Objects.requireNonNull(campaignDay, "campaignDay must not be null");
        Objects.requireNonNull(cart, "cart must not be null");

        List<CalculationIssue> issues = new ArrayList<>();
        String cartTypeKey = normalized(cart.cartType().key());
        String cartName = normalized(cart.cartType().name());

        if (isSlaveCart(cartTypeKey, cartName) && cart.passengerAssignments().stream().anyMatch(passenger -> caravan.findTraveller(passenger.travellerId()).map(traveller -> !traveller.isSlave()).orElse(true))) {
            issues.add(issue(
                    BusinessRuleCode.SLAVE_CART_REQUIRES_SLAVES,
                    "Slave cart only accepts slaves",
                    cart.id().toString(),
                    Map.of("cart", cart.name())));
        }

        if (isPrisonerCart(cartTypeKey, cartName) && cart.passengerAssignments().stream().anyMatch(passenger -> caravan.findTraveller(passenger.travellerId()).map(traveller -> !traveller.isPrisoner()).orElse(true))) {
            issues.add(issue(
                    BusinessRuleCode.PRISONER_CART_REQUIRES_PRISONERS,
                    "Prisoner cart only accepts prisoners",
                    cart.id().toString(),
                    Map.of("cart", cart.name())));
        }

        if (isZooCart(cartTypeKey, cartName) && cart.passengerAssignments().stream().anyMatch(passenger -> caravan.findTraveller(passenger.travellerId()).map(Traveller::isHumanoidCreature).orElse(true))) {
            issues.add(issue(
                    BusinessRuleCode.ZOO_CART_REJECTS_HUMANOIDS,
                    "Zoo cart rejects humanoids",
                    cart.id().toString(),
                    Map.of("cart", cart.name())));
        }

        if (isGardenCart(cartTypeKey, cartName)) {
            for (Traveller traveller : caravan.travellers()) {
                if (traveller.isFarmerOn(campaignDay.id(), cart.id())
                        && cart.passengerAssignments().stream().noneMatch(assignment -> assignment.travellerId().equals(traveller.id()))) {
                    issues.add(issue(
                            BusinessRuleCode.GARDEN_CART_REQUIRES_HOUSED_FARMERS,
                            "Garden cart only allows housed/assigned travellers to act as farmers",
                            traveller.id().toString(),
                            Map.of("cart", cart.name())));
                }
            }
        }

        if (isSchoolCart(cartTypeKey, cartName)) {
            long teacherAssignments = caravan.travellers().stream()
                    .filter(traveller -> traveller.isTeacherOn(campaignDay.id(), cart.id()))
                    .count();
            if (teacherAssignments > 2) {
                issues.add(issue(
                        BusinessRuleCode.SCHOOL_CART_TOO_MANY_TEACHERS,
                        "School cart allows at most two active teachers",
                        cart.id().toString(),
                        Map.of("teachers", Long.toString(teacherAssignments))));
            }
        }

        if (isOracleCart(cartTypeKey, cartName) && !cart.isOperative()) {
            if (cart.passengerAssignments().stream().map(assignment -> caravan.findTraveller(assignment.travellerId()).orElse(null)).filter(Objects::nonNull).anyMatch(traveller -> traveller.hasDailyRole("ORACLE", campaignDay.id()) || traveller.hasDailyRole("DIVINER", campaignDay.id(), cart.id()))) {
                issues.add(issue(
                        BusinessRuleCode.ORACLE_CART_REQUIRES_OPERATIONAL_CART,
                        "Oracle cart enables Oracle role only when operative",
                        cart.id().toString(),
                        Map.of("cart", cart.name())));
            }
        }

        if (isMuseumCart(cartTypeKey, cartName)) {
            for (CartCargoAllocation allocation : cart.cargoAllocations()) {
                InventoryLot lot = caravan.findInventoryLot(allocation.inventoryLotId()).orElse(null);
                if (lot == null || !isTreasureCargo(lot.cargoTypeId())) {
                    issues.add(issue(
                            BusinessRuleCode.CARGO_TYPE_NOT_ALLOWED,
                            "Museum cart only accepts treasure",
                            cart.id().toString(),
                            Map.of("cart", cart.name())));
                }
            }
        }

        if (isSupplyCart(cartTypeKey, cartName)) {
            for (CartCargoAllocation allocation : cart.cargoAllocations()) {
                InventoryLot lot = caravan.findInventoryLot(allocation.inventoryLotId()).orElse(null);
                if (lot == null || !(isSuppliesCargo(lot.cargoTypeId()) || isPerishableLot(lot))) {
                    issues.add(issue(
                            BusinessRuleCode.CARGO_TYPE_NOT_ALLOWED,
                            "Supply cart only accepts supplies and perishables",
                            cart.id().toString(),
                            Map.of("cart", cart.name())));
                }
            }
        }

        if (isSpecialCargoCart(cartTypeKey, cartName)) {
            String explicitSubtype = null;
            for (CartCargoAllocation allocation : cart.cargoAllocations()) {
                InventoryLot lot = caravan.findInventoryLot(allocation.inventoryLotId()).orElse(null);
                if (lot == null) {
                    issues.add(issue(
                            BusinessRuleCode.CARGO_TYPE_NOT_ALLOWED,
                            "Special cargo cart requires assigned inventory lots",
                            cart.id().toString(),
                            Map.of("cart", cart.name())));
                    continue;
                }
                String lotSubtype = normalizedSubtype(lot);
                if (lotSubtype == null) {
                    lotSubtype = normalized(lot.cargoTypeId());
                }
                if (explicitSubtype == null) {
                    explicitSubtype = lotSubtype;
                } else if (!explicitSubtype.equals(lotSubtype)) {
                    issues.add(issue(
                            BusinessRuleCode.SPECIAL_CARGO_SUBTYPE_MISMATCH,
                            "Special cargo carts enforce subtype purity",
                            cart.id().toString(),
                            Map.of(
                                    "firstSubtype", explicitSubtype,
                                    "currentSubtype", lotSubtype)));
                }
            }
        }

        return issues;
    }

    public static BigDecimal effectivePassengerCapacity(Cart cart, RoundingMode roundingMode) {
        BigDecimal capacity = cart.cartType().passengerCapacity();
        if (cart.hasActiveUpgrade("FRIDGE")) {
            return FRIDGE_PASSENGER_CAPACITY;
        }
        if (cart.hasActiveUpgrade("EXTENDED_SPACE_TRAVELLERS")) {
            BigDecimal bonus = roundedPercentageBonus(capacity, EXTENDED_SPACE_FACTOR, 1, roundingMode);
            capacity = capacity.add(bonus);
        }
        return capacity;
    }

    public static BigDecimal effectiveCargoCapacity(Cart cart, RoundingMode roundingMode) {
        BigDecimal capacity = cart.cartType().cargoCapacity();
        if (cart.hasActiveUpgrade("EXTENDED_SPACE_CARGO")) {
            BigDecimal bonus = roundedPercentageBonus(capacity, EXTENDED_SPACE_FACTOR, 2, roundingMode);
            capacity = capacity.add(bonus);
        }
        return capacity;
    }

    public static BigDecimal requiredTowingStrengthForCart(Cart cart, TravelContext travelContext) {
        BigDecimal required = BigDecimal.valueOf(effectivePropulsionRequirement(cart));
        if (cart.hasActiveUpgrade("ICE_RUNNERS")) {
            if (travelContext != null && travelContext.frozenTerrain()) {
                required = required.divide(BigDecimal.valueOf(2), 2, RoundingMode.CEILING);
            } else {
                required = required.multiply(BigDecimal.valueOf(4));
            }
        }
        return required;
    }

    static BigDecimal effectiveCartConsumption(Cart cart) {
        BigDecimal consumption = cart.cartType().consumption();
        if (cart.hasActiveUpgrade("TWO_HORSE_TRAIN")) {
            consumption = consumption.add(BigDecimal.ONE);
        }
        if (cart.hasActiveUpgrade("FOUR_HORSE_TRAIN")) {
            consumption = consumption.add(BigDecimal.valueOf(2));
        }
        if (cart.hasActiveUpgrade("SIX_HORSE_TRAIN")) {
            consumption = consumption.add(BigDecimal.valueOf(2));
        }
        if (cart.hasActiveUpgrade("EIGHT_HORSE_TRAIN")) {
            consumption = consumption.add(BigDecimal.valueOf(2));
        }
        return consumption;
    }

    static int effectivePropulsionRequirement(Cart cart) {
        int requirement = cart.cartType().propulsionRequirement();
        if (cart.hasActiveUpgrade("ARMOURED") || cart.hasActiveUpgrade("ARMORED")) {
            requirement *= 2;
        }
        return requirement;
    }

    static BigDecimal baseSpeedMilesPerDay(int speedFeet) {
        return switch (speedFeet) {
            case 20 -> BigDecimal.valueOf(8);
            case 30 -> BigDecimal.valueOf(16);
            case 40 -> BigDecimal.valueOf(24);
            case 50 -> BigDecimal.valueOf(32);
            case 60 -> BigDecimal.valueOf(40);
            default -> BigDecimal.valueOf(speedFeet).divide(BigDecimal.TEN, 2, RoundingMode.HALF_UP);
        };
    }

    public static int towingFatigueThresholdDays(BigDecimal towingStrength, BigDecimal requiredStrength) {
        if (requiredStrength == null || requiredStrength.signum() <= 0 || towingStrength == null) {
            return 0;
        }
        BigDecimal ratio = towingStrength.divide(requiredStrength, 4, RoundingMode.HALF_UP);
        if (ratio.compareTo(new BigDecimal("2")) >= 0) {
            return Integer.MAX_VALUE;
        }
        if (ratio.compareTo(new BigDecimal("1.5")) >= 0) {
            return 10;
        }
        if (ratio.compareTo(BigDecimal.ONE) >= 0) {
            return 5;
        }
        return 0;
    }

    public static int currentTowingStreak(Caravan caravan, UUID campaignDayId, Cart cart) {
        return cart.towingAssignments().stream()
                .filter(assignment -> campaignDayId.equals(assignment.campaignDayId()))
                .mapToInt(TowingAssignment::consecutiveTowingDays)
                .max()
                .orElse(0);
    }

    public static int activeRoleCount(Caravan caravan, UUID campaignDayId, String roleKey) {
        return (int) caravan.travellers().stream()
                .filter(traveller -> traveller.hasDailyRole(roleKey, campaignDayId))
                .count();
    }

    private static CalculationIssue issue(BusinessRuleCode code, String message, String subject, Map<String, String> details) {
        return new CalculationIssue(code, message, subject, details, "business-rules");
    }

    private static boolean hasDriverForCart(Caravan caravan, CampaignDay campaignDay, Cart cart) {
        return caravan.travellers().stream()
                .anyMatch(traveller -> traveller.hasDailyRole("WAGONER", campaignDay.id(), cart.id()));
    }

    private static boolean allOperativeCartsHaveUpgrade(Caravan caravan, String upgradeKey) {
        List<Cart> operativeCarts = caravan.operativeCarts();
        if (operativeCarts.isEmpty()) {
            return false;
        }
        return operativeCarts.stream().allMatch(cart -> cart.hasActiveUpgrade(upgradeKey));
    }

    private static BigDecimal towingStrengthForCart(Caravan caravan, CatalogRegistry catalogRegistry, UUID campaignDayId, Cart cart) {
        BigDecimal total = BigDecimal.ZERO;
        for (TowingAssignment assignment : cart.towingAssignments()) {
            if (!campaignDayId.equals(assignment.campaignDayId())) {
                continue;
            }
            Beast beast = caravan.findBeast(assignment.beastId()).orElse(null);
            if (beast == null || !beast.activeAsTowing()) {
                continue;
            }
            BigDecimal strength = BigDecimal.valueOf(beast.strength());
            if (isLargeOrLarger(beast)) {
                strength = strength.multiply(LARGE_OR_LARGER_STRENGTH_MULTIPLIER);
            }
            strength = strength.add(cartUpgradeTowingBonus(cart));
            total = total.add(strength);
        }
        return total;
    }

    private static BigDecimal cartUpgradeTowingBonus(Cart cart) {
        BigDecimal total = BigDecimal.ZERO;
        if (cart.hasActiveUpgrade("TWO_HORSE_TRAIN")) {
            total = total.add(BigDecimal.valueOf(5));
        }
        if (cart.hasActiveUpgrade("FOUR_HORSE_TRAIN")) {
            total = total.add(BigDecimal.valueOf(10));
        }
        if (cart.hasActiveUpgrade("SIX_HORSE_TRAIN")) {
            total = total.add(BigDecimal.valueOf(10));
        }
        if (cart.hasActiveUpgrade("EIGHT_HORSE_TRAIN")) {
            total = total.add(BigDecimal.valueOf(10));
        }
        return total;
    }

    private static BigDecimal slowestTowingCreatureSpeedMiles(Caravan caravan, UUID campaignDayId) {
        List<BigDecimal> speeds = new ArrayList<>();
        for (Cart cart : caravan.operativeCarts()) {
            for (TowingAssignment assignment : cart.towingAssignments()) {
                if (!campaignDayId.equals(assignment.campaignDayId())) {
                    continue;
                }
                caravan.findBeast(assignment.beastId())
                        .filter(Beast::activeAsTowing)
                        .ifPresent(beast -> speeds.add(BigDecimal.valueOf(beast.speedFeet()).divide(DEFAULT_TOWING_SPEED_UNIT_DIVISOR, 2, RoundingMode.HALF_UP)));
            }
        }
        if (speeds.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return speeds.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
    }

    private static CheckOutcome resolveOutcome(int naturalRoll, int total, int dc) {
        if (naturalRoll == 20) {
            return CheckOutcome.CRITICAL_SUCCESS;
        }
        if (naturalRoll == 1) {
            return CheckOutcome.CRITICAL_FAILURE;
        }
        return total >= dc ? CheckOutcome.SUCCESS : CheckOutcome.FAILURE;
    }

    private static boolean isLargeOrLarger(Beast beast) {
        String normalizedSize = normalized(beast.size());
        return normalizedSize.contains("grande")
                || normalizedSize.contains("enorme")
                || normalizedSize.contains("gigante")
                || normalizedSize.contains("large")
                || normalizedSize.contains("huge")
                || normalizedSize.contains("gargant");
    }

    private static boolean isSlaveCart(String cartTypeKey, String cartName) {
        return cartTypeKey.contains("slave") || cartName.contains("esclav");
    }

    private static boolean isPrisonerCart(String cartTypeKey, String cartName) {
        return cartTypeKey.contains("prison") || cartName.contains("prision");
    }

    private static boolean isZooCart(String cartTypeKey, String cartName) {
        return cartTypeKey.contains("zoo") || cartName.contains("zool");
    }

    private static boolean isMuseumCart(String cartTypeKey, String cartName) {
        return cartTypeKey.contains("museum") || cartName.contains("museo");
    }

    private static boolean isGardenCart(String cartTypeKey, String cartName) {
        return cartTypeKey.contains("garden") || cartName.contains("huerto");
    }

    private static boolean isSchoolCart(String cartTypeKey, String cartName) {
        return cartTypeKey.contains("school") || cartName.contains("escuela");
    }

    private static boolean isOracleCart(String cartTypeKey, String cartName) {
        return cartTypeKey.contains("oracle") || cartName.contains("adivino") || cartName.contains("oracle");
    }

    private static boolean isSupplyCart(String cartTypeKey, String cartName) {
        return cartTypeKey.contains("supply") || cartName.contains("suministro");
    }

    private static boolean isMedicalCart(String cartTypeKey, String cartName) {
        return cartTypeKey.contains("medical") || cartName.contains("medic");
    }

    private static boolean isMedicalCart(Cart cart) {
        return isMedicalCart(normalized(cart.cartType().key()), normalized(cart.cartType().name()));
    }

    private static boolean isSpecialCargoCart(String cartTypeKey, String cartName) {
        return cartTypeKey.contains("special") || cartTypeKey.contains("cargo") || cartName.contains("especial") || cartName.contains("carga");
    }

    private static boolean isTreasureCargo(String cargoTypeId) {
        return normalized(cargoTypeId).contains("treasure") || normalized(cargoTypeId).contains("tesoro");
    }

    private static boolean isSuppliesCargo(String cargoTypeId) {
        String normalizedCargoType = normalized(cargoTypeId);
        return normalizedCargoType.contains("supplies") || normalizedCargoType.contains("suministr");
    }

    private static boolean isPerishableLot(InventoryLot lot) {
        return lot != null && (isPerishablesCargo(lot.cargoTypeId()) || lot.remainingProvisions() != null && lot.remainingProvisions().signum() >= 0 && normalizedSubtype(lot) != null && normalizedSubtype(lot).contains("perish"));
    }

    private static boolean isPerishablesCargo(String cargoTypeId) {
        String normalizedCargoType = normalized(cargoTypeId);
        return normalizedCargoType.contains("perish") || normalizedCargoType.contains("pereced");
    }

    private static String normalizedSubtype(InventoryLot lot) {
        if (lot.metadata() == null) {
            return null;
        }
        String subtype = lot.metadata().get("subtype");
        return subtype == null ? null : normalized(subtype);
    }

    private static BigDecimal roundedPercentageBonus(BigDecimal baseCapacity, BigDecimal factor, int minimumBonus, RoundingMode roundingMode) {
        if (baseCapacity == null) {
            return BigDecimal.valueOf(minimumBonus);
        }
        BigDecimal calculated = baseCapacity.multiply(factor);
        BigDecimal rounded = calculated.setScale(0, roundingMode);
        if (rounded.compareTo(BigDecimal.valueOf(minimumBonus)) < 0) {
            rounded = BigDecimal.valueOf(minimumBonus);
        }
        return rounded;
    }

    private static RoundingMode resolveExtendedSpaceRounding(CampaignRuleState ruleState) {
        String choice = resolveDecision(ruleState, RuleDecisionKey.D_01_EXTENDED_SPACE_ROUNDING);
        if (choice == null) {
            return RoundingMode.CEILING;
        }
        String normalizedChoice = normalized(choice);
        if (normalizedChoice.contains("floor")) {
            return RoundingMode.FLOOR;
        }
        return RoundingMode.CEILING;
    }

    private static String resolveDecision(CampaignRuleState ruleState, RuleDecisionKey key) {
        if (ruleState == null) {
            return key.documentedChoice() != null ? key.documentedChoice() : key.defaultProposal();
        }
        var decision = ruleState.decision(key);
        if (decision.currentResolution() != null && !decision.currentResolution().isBlank()) {
            return decision.currentResolution();
        }
        return key.documentedChoice() != null ? key.documentedChoice() : key.defaultProposal();
    }

    private static String normalized(String value) {
        if (value == null) {
            return "";
        }
        String decomposed = Normalizer.normalize(value, Normalizer.Form.NFD);
        String stripped = decomposed.replaceAll("\\p{M}+", "");
        return stripped.trim().toLowerCase(Locale.ROOT);
    }
}
