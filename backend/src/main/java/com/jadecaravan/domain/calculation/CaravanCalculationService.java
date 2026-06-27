package com.jadecaravan.domain.calculation;

import com.jadecaravan.domain.campaign.Beast;
import com.jadecaravan.domain.campaign.CampaignDay;
import com.jadecaravan.domain.campaign.Cart;
import com.jadecaravan.domain.campaign.CartUpgradeInstance;
import com.jadecaravan.domain.campaign.Caravan;
import com.jadecaravan.domain.campaign.CaravanEventSeverity;
import com.jadecaravan.domain.campaign.CheckType;
import com.jadecaravan.domain.campaign.DailyRoleAssignment;
import com.jadecaravan.domain.campaign.Traveller;
import com.jadecaravan.domain.catalog.CatalogRegistry;
import com.jadecaravan.domain.catalog.CartTypeCatalogEntry;
import com.jadecaravan.domain.catalog.FeatCatalogEntry;
import com.jadecaravan.domain.catalog.RoleCatalogEntry;
import com.jadecaravan.domain.catalog.UpgradeCatalogEntry;
import com.jadecaravan.domain.rules.CampaignRuleState;
import com.jadecaravan.domain.rules.RuleDecisionKey;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pure orchestration layer for caravan calculations.
 *
 * <p>The existing {@link CaravanBusinessRules} class contains the low-level
 * deterministic primitives. This service composes them into higher-level
 * results that match the calculation-engine specification: effective cart
 * derivation, contextual consumption and speed, role-based modifiers, event
 * discontent, and a single snapshot that can be reused by the API or UI.</p>
 */
public final class CaravanCalculationService {

    private static final Pattern LEADING_INTEGER_PAIR = Pattern.compile("(\\d+)\\D+(\\d+)");
    private static final BigDecimal TWENTY_FIVE_PERCENT = new BigDecimal("0.25");

    public CalculationResult<EffectiveCart> calculateEffectiveCart(
            Cart cart,
            CatalogRegistry catalogRegistry,
            CampaignRuleState ruleState) {
        return calculateEffectiveCart(cart, catalogRegistry, ruleState, CaravanCalculationContext.empty());
    }

    public CalculationResult<EffectiveCart> calculateEffectiveCart(
            Cart cart,
            CatalogRegistry catalogRegistry,
            CampaignRuleState ruleState,
            CaravanCalculationContext context) {
        Objects.requireNonNull(cart, "cart must not be null");
        Objects.requireNonNull(catalogRegistry, "catalogRegistry must not be null");
        CaravanCalculationContext calculationContext = context == null ? CaravanCalculationContext.empty() : context;

        List<CalculationBreakdownItem> breakdown = new ArrayList<>();
        List<CalculationIssue> warnings = new ArrayList<>();
        List<CalculationIssue> blockers = new ArrayList<>();

        CartTypeCatalogEntry cartType = cart.cartType();
        int maxHitPoints = cartType.hitPoints();
        int hardness = cartType.hardness();
        int propulsionRequirement = cartType.propulsionRequirement();
        int maxLargeTowedCreatures = parseTowingCreatureLimit(cartType.towingCreatureLimit()).map(TowingLimit::large).orElse(0);
        int maxMediumTowedCreatures = parseTowingCreatureLimit(cartType.towingCreatureLimit()).map(TowingLimit::medium).orElse(0);
        BigDecimal consumption = cartType.consumption();
        BigDecimal passengerCapacity = cartType.passengerCapacity();
        BigDecimal cargoCapacity = cartType.cargoCapacity();
        boolean passengerCapacityLockedToZero = false;

        breakdown.add(new CalculationBreakdownItem(
                "Base cart statistics",
                BigDecimal.ZERO,
                cartType.source(),
                cartType.name()));

        for (CartUpgradeInstance upgradeInstance : cart.activeUpgrades()) {
            UpgradeCatalogEntry upgrade = catalogRegistry.upgrade(upgradeInstance.upgrade().key());
            String normalizedKey = normalized(upgrade.key());

            if (hasIncompatibility(cart, upgrade)) {
                blockers.add(issue(
                        BusinessRuleCode.INVALID_FEAT_USAGE,
                        "Upgrade is incompatible with the cart's current configuration",
                        cart.id().toString(),
                        Map.of(
                                "cart", cart.name(),
                                "upgrade", upgrade.key())));
                continue;
            }

            switch (normalizedKey) {
                case "armoured", "armored" -> {
                    int nextHitPoints = floorMultiply(maxHitPoints, 1.5d);
                    breakdown.add(new CalculationBreakdownItem(
                            "Acorazado",
                            BigDecimal.valueOf(nextHitPoints - maxHitPoints),
                            upgrade.source(),
                            upgrade.effect()));
                    maxHitPoints = nextHitPoints;
                    hardness += 5;
                    propulsionRequirement *= 2;
                }
                case "reinforcement", "refuerzo" -> {
                    maxHitPoints += 10;
                    cargoCapacity = cargoCapacity.subtract(BigDecimal.ONE);
                    breakdown.add(new CalculationBreakdownItem(
                            "Refuerzo",
                            BigDecimal.TEN,
                            upgrade.source(),
                            upgrade.effect()));
                }
                case "extended_space_travellers" -> {
                    if (passengerCapacityLockedToZero) {
                        breakdown.add(new CalculationBreakdownItem(
                                "Espacio extendido - viajeros",
                                BigDecimal.ZERO,
                                upgrade.source(),
                                upgrade.effect() + " (ignored by fridge)"));
                        break;
                    }
                    BigDecimal bonus = percentageBonus(passengerCapacity, TWENTY_FIVE_PERCENT, 1, resolveExtendedSpaceRounding(ruleState));
                    passengerCapacity = passengerCapacity.add(bonus);
                    breakdown.add(new CalculationBreakdownItem(
                            "Espacio extendido - viajeros",
                            bonus,
                            upgrade.source(),
                            upgrade.effect()));
                }
                case "extended_space_cargo" -> {
                    BigDecimal bonus = percentageBonus(cargoCapacity, TWENTY_FIVE_PERCENT, 2, resolveExtendedSpaceRounding(ruleState));
                    cargoCapacity = cargoCapacity.add(bonus);
                    breakdown.add(new CalculationBreakdownItem(
                            "Espacio extendido - carga",
                            bonus,
                            upgrade.source(),
                            upgrade.effect()));
                }
                case "fridge" -> {
                    passengerCapacity = BigDecimal.ZERO;
                    passengerCapacityLockedToZero = true;
                    breakdown.add(new CalculationBreakdownItem(
                            "Nevera",
                            BigDecimal.ZERO,
                            upgrade.source(),
                            upgrade.effect()));
                }
                case "cold_insulation", "heat_insulation" -> {
                    hardness += 2;
                    breakdown.add(new CalculationBreakdownItem(
                            upgrade.name(),
                            BigDecimal.valueOf(2),
                            upgrade.source(),
                            upgrade.effect()));
                }
                case "two_horse_train", "four_horse_train", "six_horse_train", "eight_horse_train" -> {
                    TowingTrainEffect effect = towingTrainEffect(normalizedKey);
                    maxLargeTowedCreatures = Math.max(maxLargeTowedCreatures, effect.maxLargeTowedCreatures());
                    maxMediumTowedCreatures = Math.max(maxMediumTowedCreatures, effect.maxMediumTowedCreatures());
                    consumption = consumption.add(effect.consumptionIncrease());
                    breakdown.add(new CalculationBreakdownItem(
                            upgrade.name(),
                            effect.breakdownValue(),
                            upgrade.source(),
                            upgrade.effect()));
                }
                default -> breakdown.add(new CalculationBreakdownItem(
                        upgrade.name(),
                        BigDecimal.ZERO,
                        upgrade.source(),
                        upgrade.effect()));
            }
        }

        EffectiveCart effectiveCart = new EffectiveCart(
                cart.id(),
                cart.name(),
                maxHitPoints,
                hardness,
                propulsionRequirement,
                maxLargeTowedCreatures,
                maxMediumTowedCreatures,
                consumption,
                passengerCapacity,
                cargoCapacity,
                breakdown,
                warnings,
                blockers,
                cart.cartType().key());

        return new CalculationResult<>(effectiveCart, breakdown, warnings, blockers, cart.cartType().key());
    }

    public CalculationResult<BigDecimal> calculateDailyConsumption(
            Caravan caravan,
            CampaignDay campaignDay,
            CaravanCalculationContext context) {
        Objects.requireNonNull(caravan, "caravan must not be null");
        Objects.requireNonNull(campaignDay, "campaignDay must not be null");
        CaravanCalculationContext calculationContext = context == null ? CaravanCalculationContext.empty() : context;

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
                "Scouts are excluded"));

        BigDecimal cartConsumption = caravan.operativeCarts().stream()
                .map(CaravanBusinessRules::effectiveCartConsumption)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        breakdown.add(new CalculationBreakdownItem(
                "Operative cart consumption",
                cartConsumption,
                "caravan.carts",
                "Sum of operative cart consumption including active upgrades"));

        BigDecimal adjustedTravellerConsumption = travellerConsumption;
        if (hasFeat(calculationContext, "INTERMITTENT_FAST")) {
            adjustedTravellerConsumption = adjustedTravellerConsumption.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
            breakdown.add(new CalculationBreakdownItem(
                    "Ayuno intermitente",
                    adjustedTravellerConsumption.subtract(travellerConsumption),
                    "docs/04-catalogos.md#5",
                    "Halves traveller consumption"));
        }

        BigDecimal total = adjustedTravellerConsumption.add(cartConsumption);

        int efficientConsumptionInstances = countFeatInstances(calculationContext, "EFFICIENT_CONSUMPTION");
        for (int i = 0; i < efficientConsumptionInstances; i++) {
            BigDecimal nextTotal = total.subtract(BigDecimal.valueOf(2));
            if (nextTotal.compareTo(cartConsumption) < 0) {
                nextTotal = cartConsumption;
            }
            breakdown.add(new CalculationBreakdownItem(
                    "Consumo eficiente",
                    nextTotal.subtract(total),
                    "docs/04-catalogos.md#5",
                    "Reduces consumption but never below operative cart consumption"));
            total = nextTotal;
        }

        if (hasFeat(calculationContext, "CELEBRATION")) {
            BigDecimal beforeCelebration = total;
            total = total.multiply(BigDecimal.valueOf(2));
            breakdown.add(new CalculationBreakdownItem(
                    "Celebración",
                    total.subtract(beforeCelebration),
                    "docs/04-catalogos.md#5",
                    "Doubles total consumption"));
        }

        return new CalculationResult<>(total, breakdown, List.of(), List.of(), caravan.ruleSetVersionId());
    }

    public CalculationResult<BigDecimal> calculateSpeedMilesPerDay(
            Caravan caravan,
            CatalogRegistry catalogRegistry,
            CampaignRuleState ruleState,
            CaravanCalculationContext context,
            UUID campaignDayId) {
        Objects.requireNonNull(caravan, "caravan must not be null");
        Objects.requireNonNull(catalogRegistry, "catalogRegistry must not be null");
        Objects.requireNonNull(campaignDayId, "campaignDayId must not be null");
        CaravanCalculationContext calculationContext = context == null ? CaravanCalculationContext.empty() : context;
        TravelContext travelContext = calculationContext.travelContext();

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

        int universalTrainBonuses = 0;
        if (allOperativeCartsHaveUpgrade(caravan, "TWO_HORSE_TRAIN")) {
            universalTrainBonuses++;
        }
        if (allOperativeCartsHaveUpgrade(caravan, "FOUR_HORSE_TRAIN")) {
            universalTrainBonuses++;
        }
        if (allOperativeCartsHaveUpgrade(caravan, "SIX_HORSE_TRAIN")) {
            universalTrainBonuses++;
        }
        if (allOperativeCartsHaveUpgrade(caravan, "EIGHT_HORSE_TRAIN")) {
            universalTrainBonuses++;
        }
        if (universalTrainBonuses > 0) {
            BigDecimal bonus = BigDecimal.valueOf(universalTrainBonuses * 4L);
            speed = speed.add(bonus);
            breakdown.add(new CalculationBreakdownItem(
                    "Tiro universal encadenado",
                    bonus,
                    "docs/04-catalogos.md#5",
                    "Applies +4 miles/day per universal train tier installed"));
        }

        if (allOperativeCartsHaveUpgrade(caravan, "IMPROVED_WHEELS")) {
            speed = speed.add(BigDecimal.valueOf(8));
            breakdown.add(new CalculationBreakdownItem(
                    "Ruedas mejoradas",
                    BigDecimal.valueOf(8),
                    "docs/04-catalogos.md#2",
                    "+8 miles/day when all operative carts have improved wheels"));
        }

        long coldInsulationCount = caravan.operativeCarts().stream()
                .filter(cart -> cart.hasActiveUpgrade("COLD_INSULATION"))
                .count();
        if (coldInsulationCount > 0) {
            speed = speed.subtract(BigDecimal.valueOf(4));
            breakdown.add(new CalculationBreakdownItem(
                    "Aislamiento frío",
                    BigDecimal.valueOf(-4),
                    "docs/04-catalogos.md#2",
                    "Applied once if at least one operative cart has cold insulation"));
        }

        long heatInsulationCount = caravan.operativeCarts().stream()
                .filter(cart -> cart.hasActiveUpgrade("HEAT_INSULATION"))
                .count();
        if (heatInsulationCount > 0) {
            BigDecimal penalty = BigDecimal.valueOf(-1L * heatInsulationCount);
            speed = speed.add(penalty);
            breakdown.add(new CalculationBreakdownItem(
                    "Aislamiento calor",
                    penalty,
                    "docs/04-catalogos.md#2",
                    "Applied once per operative cart with heat insulation"));
        }

        if (travelContext.flatSpeedBonusMilesPerDay().signum() != 0) {
            speed = speed.add(travelContext.flatSpeedBonusMilesPerDay());
            breakdown.add(new CalculationBreakdownItem(
                    "Context speed bonus",
                    travelContext.flatSpeedBonusMilesPerDay(),
                    "travel-context",
                    "Custom campaign or terrain bonus"));
        }

        if (travelContext.flatSpeedPenaltyMilesPerDay().signum() != 0) {
            speed = speed.subtract(travelContext.flatSpeedPenaltyMilesPerDay());
            breakdown.add(new CalculationBreakdownItem(
                    "Context speed penalty",
                    travelContext.flatSpeedPenaltyMilesPerDay().negate(),
                    "travel-context",
                    "Custom campaign or terrain penalty"));
        }

        if (travelContext.nightTravel()) {
            BigDecimal beforeNightPenalty = speed;
            speed = speed.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
            breakdown.add(new CalculationBreakdownItem(
                    "Night travel",
                    speed.subtract(beforeNightPenalty),
                    "travel-context",
                    "Night travel halves speed"));
        }

        if (calculationContext.inSettlement() && calculationContext.settlementType() != null) {
            breakdown.add(new CalculationBreakdownItem(
                    "Settlement context",
                    BigDecimal.ZERO,
                    calculationContext.settlementType(),
                    "Settlement context acknowledged"));
        }

        if (speed.signum() < 0) {
            speed = BigDecimal.ZERO;
        }

        return new CalculationResult<>(speed, breakdown, warnings, blockers, caravan.ruleSetVersionId());
    }

    public CalculationResult<Integer> calculateRoleModifier(
            Caravan caravan,
            CampaignDay campaignDay,
            CheckType checkType,
            CatalogRegistry catalogRegistry,
            CaravanCalculationContext context) {
        Objects.requireNonNull(caravan, "caravan must not be null");
        Objects.requireNonNull(campaignDay, "campaignDay must not be null");
        Objects.requireNonNull(checkType, "checkType must not be null");
        Objects.requireNonNull(catalogRegistry, "catalogRegistry must not be null");
        CaravanCalculationContext calculationContext = context == null ? CaravanCalculationContext.empty() : context;

        List<CalculationBreakdownItem> breakdown = new ArrayList<>();
        List<CalculationIssue> warnings = new ArrayList<>();

        int baseStat = switch (checkType) {
            case ATTACK -> caravan.baseStats().offense();
            case ARMOR_CLASS -> caravan.baseStats().defense();
            case SECURITY -> caravan.baseStats().mobility();
            case DETERMINATION -> caravan.baseStats().morale();
            default -> 0;
        };
        breakdown.add(new CalculationBreakdownItem(
                "Base stat",
                BigDecimal.valueOf(baseStat),
                "caravan.baseStats",
                "Base stat selected from the caravan"));

        Map<String, RoleTally> roleTallies = new LinkedHashMap<>();
        int heroCount = 0;
        for (Traveller traveller : caravan.travellers()) {
            for (DailyRoleAssignment assignment : traveller.dailyRoleAssignments()) {
                if (!campaignDay.id().equals(assignment.campaignDayId())) {
                    continue;
                }
                String roleKey = normalized(assignment.role().key());
                if (roleKey.equals("hero")) {
                    heroCount++;
                    continue;
                }
                int contribution = roleContribution(checkType, roleKey);
                if (contribution == 0) {
                    continue;
                }
                roleTallies.computeIfAbsent(roleKey, ignored -> new RoleTally()).add(contribution);
            }
        }

        int roleBonusLimit = 5 + countFeatInstances(calculationContext, "EXPERT_TRAVELLERS");
        int limitedRoleBonus = 0;
        for (Map.Entry<String, RoleTally> entry : roleTallies.entrySet()) {
            int contribution = entry.getValue().bonus();
            int applied = Math.min(contribution, Math.max(0, roleBonusLimit - limitedRoleBonus));
            limitedRoleBonus += applied;
            if (applied != 0) {
                RoleCatalogEntry role = catalogRegistry.role(entry.getKey());
                breakdown.add(new CalculationBreakdownItem(
                        "Role " + role.name(),
                        BigDecimal.valueOf(applied),
                        role.source(),
                        role.benefitSummary()));
            }
            if (applied < contribution) {
                warnings.add(issue(
                        BusinessRuleCode.UNKNOWN,
                        "Role bonus exceeded the configured limit and was clamped",
                        checkType.name(),
                        Map.of(
                                "role", entry.getKey(),
                                "bonus", Integer.toString(contribution),
                                "applied", Integer.toString(applied))));
            }
        }

        int heroBonus = (checkType == CheckType.SECURITY || checkType == CheckType.DETERMINATION)
                ? Math.min(4, heroCount)
                : 0;
        if (heroBonus != 0) {
            RoleCatalogEntry hero = catalogRegistry.role("HERO");
            breakdown.add(new CalculationBreakdownItem(
                    "Héroe",
                    BigDecimal.valueOf(heroBonus),
                    hero.source(),
                    hero.benefitSummary()));
        }

        int teamworkBonus = teamworkBonus(checkType, roleTallies, calculationContext, catalogRegistry, breakdown);
        int servantBonus = servantBonus(caravan, campaignDay, checkType, calculationContext, catalogRegistry, breakdown);

        int totalModifier = baseStat + limitedRoleBonus + heroBonus + teamworkBonus + servantBonus;
        return new CalculationResult<>(totalModifier, breakdown, warnings, List.of(), caravan.ruleSetVersionId());
    }

    public CalculationResult<BigDecimal> calculateEventDiscontentGain(
            CaravanEventSeverity severity,
            int additionalEvents,
            CampaignRuleState ruleState) {
        Objects.requireNonNull(severity, "severity must not be null");
        if (additionalEvents < 0) {
            throw new IllegalArgumentException("additionalEvents must not be negative");
        }

        int baseGain = switch (severity) {
            case INFO -> 0;
            case WARNING, MINOR -> 1;
            case MAJOR -> 2;
            case CRITICAL -> 3;
        };

        if (ruleState != null) {
            String choice = resolveDecision(ruleState, RuleDecisionKey.D_03_DISCONTENT_GAIN_ON_FAILURE_TABLE);
            if (choice != null && normalized(choice).contains("always 1")) {
                baseGain = 1;
            }
        }

        BigDecimal totalGain = BigDecimal.valueOf(baseGain + additionalEvents);
        List<CalculationBreakdownItem> breakdown = List.of(new CalculationBreakdownItem(
                "Event-based discontent gain",
                totalGain,
                "docs/10-decidir-antes-de-automatizar.md#D-03",
                "Severity " + severity + ", additional events " + additionalEvents));
        return new CalculationResult<>(
                totalGain,
                breakdown,
                List.of(),
                List.of(),
                ruleState == null ? "rule-set-unknown" : ruleState.ruleSetVersionId());
    }

    public CalculationResult<CaravanCalculationSummary> calculateCaravanSummary(
            Caravan caravan,
            CatalogRegistry catalogRegistry,
            CampaignRuleState ruleState,
            CampaignDay campaignDay,
            CaravanCalculationContext context) {
        Objects.requireNonNull(caravan, "caravan must not be null");
        Objects.requireNonNull(catalogRegistry, "catalogRegistry must not be null");
        Objects.requireNonNull(ruleState, "ruleState must not be null");
        Objects.requireNonNull(campaignDay, "campaignDay must not be null");
        CaravanCalculationContext calculationContext = context == null ? CaravanCalculationContext.empty() : context;

        List<EffectiveCart> effectiveCarts = new ArrayList<>();
        List<CalculationBreakdownItem> breakdown = new ArrayList<>();
        List<CalculationIssue> warnings = new ArrayList<>();
        List<CalculationIssue> blockers = new ArrayList<>();

        for (Cart cart : caravan.operativeCarts()) {
            CalculationResult<EffectiveCart> result = calculateEffectiveCart(cart, catalogRegistry, ruleState, calculationContext);
            effectiveCarts.add(result.value());
            breakdown.addAll(result.breakdown());
            warnings.addAll(result.warnings());
            blockers.addAll(result.blockers());
        }

        CalculationResult<BigDecimal> passengerCapacity = CaravanBusinessRules.calculatePassengerCapacity(caravan, catalogRegistry, ruleState);
        CalculationResult<BigDecimal> cargoCapacity = CaravanBusinessRules.calculateCargoCapacity(caravan, catalogRegistry, ruleState);
        CalculationResult<BigDecimal> towingStrength = CaravanBusinessRules.calculateTowingStrength(caravan, catalogRegistry, campaignDay.id());
        CalculationResult<BigDecimal> requiredTowingStrength = CaravanBusinessRules.calculateRequiredTowingStrength(
                caravan,
                catalogRegistry,
                calculationContext.travelContext());
        CalculationResult<BigDecimal> speed = calculateSpeedMilesPerDay(caravan, catalogRegistry, ruleState, calculationContext, campaignDay.id());
        CalculationResult<BigDecimal> consumption = calculateDailyConsumption(caravan, campaignDay, calculationContext);
        CalculationResult<BigDecimal> mutiny = CaravanBusinessRules.calculateMutinyPenalty(caravan);

        breakdown.addAll(passengerCapacity.breakdown());
        breakdown.addAll(cargoCapacity.breakdown());
        breakdown.addAll(towingStrength.breakdown());
        breakdown.addAll(requiredTowingStrength.breakdown());
        breakdown.addAll(speed.breakdown());
        breakdown.addAll(consumption.breakdown());
        breakdown.addAll(mutiny.breakdown());
        warnings.addAll(passengerCapacity.warnings());
        warnings.addAll(cargoCapacity.warnings());
        warnings.addAll(towingStrength.warnings());
        warnings.addAll(requiredTowingStrength.warnings());
        warnings.addAll(speed.warnings());
        warnings.addAll(consumption.warnings());
        warnings.addAll(mutiny.warnings());
        blockers.addAll(passengerCapacity.blockers());
        blockers.addAll(cargoCapacity.blockers());
        blockers.addAll(towingStrength.blockers());
        blockers.addAll(requiredTowingStrength.blockers());
        blockers.addAll(speed.blockers());
        blockers.addAll(consumption.blockers());
        blockers.addAll(mutiny.blockers());

        CaravanCalculationSummary summary = new CaravanCalculationSummary(
                effectiveCarts,
                passengerCapacity.value(),
                cargoCapacity.value(),
                caravan.totalTravellerOccupancy(),
                caravan.totalCargoOccupancy(),
                towingStrength.value(),
                requiredTowingStrength.value(),
                speed.value(),
                consumption.value(),
                mutiny.value(),
                breakdown,
                warnings,
                blockers,
                caravan.ruleSetVersionId());

        return new CalculationResult<>(summary, breakdown, warnings, blockers, caravan.ruleSetVersionId());
    }

    private static int teamworkBonus(
            CheckType checkType,
            Map<String, RoleTally> roleTallies,
            CaravanCalculationContext context,
            CatalogRegistry catalogRegistry,
            List<CalculationBreakdownItem> breakdown) {
        if (!hasFeat(context, "TEAMWORK")) {
            return 0;
        }
        int total = 0;
        for (Map.Entry<String, RoleTally> entry : roleTallies.entrySet()) {
            int sameRoleCount = entry.getValue().count();
            if (sameRoleCount < 2) {
                continue;
            }
            int companions = Math.min(3, sameRoleCount) - 1;
            BigDecimal teamContributionValue = BigDecimal.valueOf(entry.getValue().bonus())
                    .multiply(BigDecimal.valueOf(companions))
                    .multiply(TWENTY_FIVE_PERCENT)
                    .setScale(0, RoundingMode.CEILING);
            int teamContribution = teamContributionValue.intValueExact();
            if (teamContribution > 0) {
                total += teamContribution;
                FeatCatalogEntry teamwork = catalogRegistry.feat("TEAMWORK");
                breakdown.add(new CalculationBreakdownItem(
                        "Trabajo en equipo",
                        BigDecimal.valueOf(teamContribution),
                        teamwork.source(),
                        teamwork.effect()));
            }
        }
        return total;
    }

    private static int servantBonus(
            Caravan caravan,
            CampaignDay campaignDay,
            CheckType checkType,
            CaravanCalculationContext context,
            CatalogRegistry catalogRegistry,
            List<CalculationBreakdownItem> breakdown) {
        if (!hasFeat(context, "SERVANT")) {
            return 0;
        }
        boolean hasHelpfulRole = caravan.travellers().stream()
                .flatMap(traveller -> traveller.dailyRoleAssignments().stream())
                .anyMatch(assignment -> campaignDay.id().equals(assignment.campaignDayId())
                        && roleContribution(checkType, normalized(assignment.role().key())) > 0);
        if (!hasHelpfulRole) {
            return 0;
        }
        breakdown.add(new CalculationBreakdownItem(
                "Sirviente",
                BigDecimal.ONE,
                "docs/04-catalogos.md#4",
                catalogRegistry.feat("SERVANT").effect()));
        return 1;
    }

    private static int roleContribution(CheckType checkType, String roleKey) {
        return switch (checkType) {
            case ATTACK -> roleKey.equals("guard") || roleKey.equals("leader") ? 1 : 0;
            case ARMOR_CLASS -> roleKey.equals("guard") ? 1 : 0;
            case SECURITY -> switch (roleKey) {
                case "guard", "guide", "scout", "night_scout", "meteorologist" -> 1;
                default -> 0;
            };
            case DETERMINATION -> switch (roleKey) {
                case "comedian", "leader", "prisoner" -> 1;
                default -> 0;
            };
            case REPAIR -> roleKey.equals("carter") ? 2 : 0;
            case TRADE -> roleKey.equals("merchant") ? 2 : 0;
            case LEADER_SPEECH -> roleKey.equals("leader") ? 1 : 0;
            default -> 0;
        };
    }

    private static boolean hasFeat(CaravanCalculationContext context, String featKey) {
        return context.activeFeatKeys().stream().anyMatch(activeFeat -> normalized(activeFeat).equals(normalized(featKey)));
    }

    private static int countFeatInstances(CaravanCalculationContext context, String featKey) {
        return (int) context.activeFeatKeys().stream()
                .filter(activeFeat -> normalized(activeFeat).equals(normalized(featKey)))
                .count();
    }

    private static boolean hasIncompatibility(Cart cart, UpgradeCatalogEntry upgrade) {
        if (upgrade.incompatibilities().isEmpty()) {
            return false;
        }
        for (CartUpgradeInstance activeUpgrade : cart.activeUpgrades()) {
            String activeKey = normalized(activeUpgrade.upgrade().key());
            for (String incompatibleKey : upgrade.incompatibilities()) {
                if (activeKey.equals(normalized(incompatibleKey))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean allOperativeCartsHaveUpgrade(Caravan caravan, String upgradeKey) {
        List<Cart> operativeCarts = caravan.operativeCarts();
        if (operativeCarts.isEmpty()) {
            return false;
        }
        String normalizedUpgradeKey = normalized(upgradeKey);
        return operativeCarts.stream().allMatch(cart -> cart.hasActiveUpgrade(normalizedUpgradeKey));
    }

    private static BigDecimal slowestTowingCreatureSpeedMiles(Caravan caravan, UUID campaignDayId) {
        List<BigDecimal> speeds = new ArrayList<>();
        for (Cart cart : caravan.operativeCarts()) {
            for (var assignment : cart.towingAssignments()) {
                if (!campaignDayId.equals(assignment.campaignDayId())) {
                    continue;
                }
                caravan.findBeast(assignment.beastId())
                        .filter(Beast::activeAsTowing)
                        .ifPresent(beast -> speeds.add(CaravanBusinessRules.baseSpeedMilesPerDay(beast.speedFeet())));
            }
        }
        if (speeds.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return speeds.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
    }

    private static int floorMultiply(int value, double factor) {
        return BigDecimal.valueOf(value)
                .multiply(BigDecimal.valueOf(factor))
                .setScale(0, RoundingMode.FLOOR)
                .intValueExact();
    }

    private static BigDecimal percentageBonus(BigDecimal baseCapacity, BigDecimal percentage, int minimumBonus, RoundingMode roundingMode) {
        if (baseCapacity == null) {
            return BigDecimal.valueOf(minimumBonus);
        }
        BigDecimal calculated = baseCapacity.multiply(percentage);
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

    private static Optional<TowingLimit> parseTowingCreatureLimit(String towingCreatureLimit) {
        if (towingCreatureLimit == null) {
            return Optional.empty();
        }
        Matcher matcher = LEADING_INTEGER_PAIR.matcher(towingCreatureLimit);
        if (!matcher.find()) {
            return Optional.empty();
        }
        return Optional.of(new TowingLimit(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2))));
    }

    private static TowingTrainEffect towingTrainEffect(String normalizedUpgradeKey) {
        return switch (normalizedUpgradeKey) {
            case "two_horse_train" -> new TowingTrainEffect(BigDecimal.valueOf(5), 1, 4, BigDecimal.ONE);
            case "four_horse_train" -> new TowingTrainEffect(BigDecimal.TEN, 2, 8, BigDecimal.valueOf(2));
            case "six_horse_train" -> new TowingTrainEffect(BigDecimal.TEN, 2, 8, BigDecimal.valueOf(2));
            case "eight_horse_train" -> new TowingTrainEffect(BigDecimal.TEN, 2, 8, BigDecimal.valueOf(2));
            default -> new TowingTrainEffect(BigDecimal.ZERO, 0, 0, BigDecimal.ZERO);
        };
    }

    private static CalculationIssue issue(BusinessRuleCode code, String message, String subject, Map<String, String> details) {
        return new CalculationIssue(code, message, subject, details, "calculation-engine");
    }

    private static String normalized(String value) {
        if (value == null) {
            return "";
        }
        String decomposed = Normalizer.normalize(value, Normalizer.Form.NFD);
        String stripped = decomposed.replaceAll("\\p{M}+", "");
        return stripped.trim().toLowerCase(Locale.ROOT);
    }

    private record TowingLimit(int large, int medium) {
    }

    private static final class RoleTally {
        private int bonus;
        private int count;

        private void add(int contribution) {
            bonus += contribution;
            count++;
        }

        private int bonus() {
            return bonus;
        }

        private int count() {
            return count;
        }
    }

    private record TowingTrainEffect(BigDecimal breakdownValue, int maxLargeTowedCreatures, int maxMediumTowedCreatures, BigDecimal consumptionIncrease) {
    }
}
