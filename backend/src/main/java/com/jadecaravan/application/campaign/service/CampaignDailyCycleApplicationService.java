package com.jadecaravan.application.campaign.service;

import com.jadecaravan.application.campaign.port.in.CampaignDailyCycleUseCase;
import com.jadecaravan.application.campaign.initialstate.ObservedInitialStateSeed;
import com.jadecaravan.application.campaign.port.out.CampaignAuditRepository;
import com.jadecaravan.application.campaign.port.out.CampaignDailyCycleRepository;
import com.jadecaravan.application.campaign.port.out.CampaignRulesRepository;
import com.jadecaravan.domain.calculation.CaravanBusinessRules;
import com.jadecaravan.domain.calculation.CaravanCalculationContext;
import com.jadecaravan.domain.calculation.CaravanCalculationService;
import com.jadecaravan.domain.calculation.CaravanCalculationSummary;
import com.jadecaravan.domain.calculation.CalculationIssue;
import com.jadecaravan.domain.calculation.CalculationResult;
import com.jadecaravan.domain.calculation.TravelContext;
import com.jadecaravan.domain.calculation.TravelValidationResult;
import com.jadecaravan.domain.campaign.Beast;
import com.jadecaravan.domain.campaign.CampaignAuditEntry;
import com.jadecaravan.domain.campaign.CampaignDay;
import com.jadecaravan.domain.campaign.CampaignDayActivityType;
import com.jadecaravan.domain.campaign.CampaignDayPreview;
import com.jadecaravan.domain.campaign.CampaignDayStatus;
import com.jadecaravan.domain.campaign.CampaignDaySummary;
import com.jadecaravan.domain.campaign.CampaignDailyCycleState;
import com.jadecaravan.domain.campaign.Caravan;
import com.jadecaravan.domain.campaign.CaravanEvent;
import com.jadecaravan.domain.campaign.CaravanEventSeverity;
import com.jadecaravan.domain.campaign.Cart;
import com.jadecaravan.domain.campaign.CartPassengerAssignment;
import com.jadecaravan.domain.campaign.CheckResolution;
import com.jadecaravan.domain.campaign.DailyOperation;
import com.jadecaravan.domain.campaign.DailyOperationType;
import com.jadecaravan.domain.campaign.DailyRoleAssignment;
import com.jadecaravan.domain.campaign.InventoryLot;
import com.jadecaravan.domain.campaign.LedgerEntry;
import com.jadecaravan.domain.campaign.LedgerOperationType;
import com.jadecaravan.domain.campaign.LedgerResourceType;
import com.jadecaravan.domain.campaign.TowingAssignment;
import com.jadecaravan.domain.campaign.TradeTransaction;
import com.jadecaravan.domain.campaign.TradeTransactionType;
import com.jadecaravan.domain.campaign.Traveller;
import com.jadecaravan.domain.catalog.BeastCatalogEntry;
import com.jadecaravan.domain.catalog.CatalogRegistry;
import com.jadecaravan.domain.catalog.CartTypeCatalogEntry;
import com.jadecaravan.domain.catalog.UpgradeCatalogEntry;
import com.jadecaravan.domain.rules.CampaignRuleState;
import com.jadecaravan.domain.rules.RuleDecisionKey;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
public class CampaignDailyCycleApplicationService implements CampaignDailyCycleUseCase {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final CampaignDailyCycleRepository dailyCycleRepository;
    private final CampaignRulesRepository campaignRulesRepository;
    private final CampaignAuditRepository campaignAuditRepository;
    private final CatalogRegistry catalogRegistry;
    private final CaravanCalculationService caravanCalculationService;
    private final Clock clock;

    public CampaignDailyCycleApplicationService(
            CampaignDailyCycleRepository dailyCycleRepository,
            CampaignRulesRepository campaignRulesRepository,
            CampaignAuditRepository campaignAuditRepository,
            CatalogRegistry catalogRegistry,
            CaravanCalculationService caravanCalculationService,
            Clock clock) {
        this.dailyCycleRepository = Objects.requireNonNull(dailyCycleRepository, "dailyCycleRepository must not be null");
        this.campaignRulesRepository = Objects.requireNonNull(campaignRulesRepository, "campaignRulesRepository must not be null");
        this.campaignAuditRepository = Objects.requireNonNull(campaignAuditRepository, "campaignAuditRepository must not be null");
        this.catalogRegistry = Objects.requireNonNull(catalogRegistry, "catalogRegistry must not be null");
        this.caravanCalculationService = Objects.requireNonNull(caravanCalculationService, "caravanCalculationService must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public CampaignDailyCycleState getState(UUID campaignId) {
        return loadOrCreateState(campaignId);
    }

    @Override
    public CampaignDailyCycleState createDay(UUID campaignId, CampaignDay campaignDay, String actor, String source, String reason) {
        CampaignDailyCycleState state = loadOrCreateState(campaignId);
        CampaignRuleState ruleState = campaignRulesRepository.loadOrCreate(campaignId);
        CampaignDay normalizedDay = Objects.requireNonNull(campaignDay, "campaignDay must not be null");
        Caravan caravan = state.caravan();
        if (state.activeDayId() != null && caravan.findCampaignDay(state.activeDayId()).isPresent()) {
            caravan = caravan.replaceCampaignDay(normalizedDay);
        } else {
            caravan = caravan.withCampaignDay(normalizedDay);
        }
        caravan = caravan.withCurrentDayNumber(normalizedDay.dayNumber());
        CampaignDailyCycleState updatedState = state.withCaravan(caravan).withActiveDayId(normalizedDay.id()).withOperations(List.of());
        dailyCycleRepository.save(updatedState);
        appendAuditEntry(campaignId, ruleState.ruleSetVersionId(), "DAY", normalizedDay.id().toString(), "CREATE_DAY", normalizedDay.activityType().name(), normalizedDay.location(), normalizedDay.location(), null, normalizeReason(reason), normalizeActor(actor), normalizeSource(source), now());
        return updatedState;
    }

    @Override
    public CampaignDailyCycleState planDay(
            UUID campaignId,
            UUID dayId,
            List<DailyRoleAssignment> roleAssignments,
            List<CartPassengerAssignment> passengerAssignments,
            List<TowingAssignment> towingAssignments,
            List<DailyOperation> dailyOperations,
            boolean overrideBlockers,
            String overrideReason,
            String actor,
            String source) {
        CampaignDailyCycleState state = loadOrCreateState(campaignId);
        CampaignRuleState ruleState = campaignRulesRepository.loadOrCreate(campaignId);
        CampaignDay activeDay = requireActiveDay(state, dayId);
        Caravan updatedCaravan = applyPlan(state.caravan(), roleAssignments, passengerAssignments, towingAssignments, dailyOperations, activeDay.id());
        CampaignDay updatedDay = activeDay.status() == CampaignDayStatus.PLANNED ? activeDay : activeDay.withStatus(CampaignDayStatus.PLANNED);
        updatedCaravan = updatedCaravan.replaceCampaignDay(updatedDay);

        TravelValidationResult validation = CaravanBusinessRules.validateTravel(updatedCaravan, catalogRegistry, ruleState, updatedDay, buildTravelContext(updatedDay, dailyOperations));
        if (!validation.travelAllowed() && !overrideBlockers) {
            throw new IllegalStateException("Cannot plan day because blockers remain: " + validation.blockers().stream().map(issue -> issue.code().name()).toList());
        }
        if (!validation.travelAllowed() && overrideBlockers && normalizeReason(overrideReason) == null) {
            throw new IllegalArgumentException("overrideReason is required when overriding blockers");
        }

        CampaignDailyCycleState updatedState = state.withCaravan(updatedCaravan).withActiveDayId(updatedDay.id()).withOperations(mergeOperations(state.operations(), dailyOperations));
        dailyCycleRepository.save(updatedState);

        appendAuditEntry(
                campaignId,
                ruleState.ruleSetVersionId(),
                "DAY",
                dayId.toString(),
                "PLAN_DAY",
                updatedDay.activityType().name(),
                validation.travelAllowed() ? "PLANNED" : "OVERRIDDEN",
                validation.travelAllowed() ? "PLANNED" : "BLOCKERS_OVERRIDDEN",
                overrideBlockers ? overrideReason : null,
                normalizeReason(overrideReason),
                normalizeActor(actor),
                normalizeSource(source),
                now());

        return updatedState;
    }

    @Override
    public CampaignDayPreview previewDay(UUID campaignId) {
        CampaignDailyCycleState state = loadOrCreateState(campaignId);
        CampaignRuleState ruleState = campaignRulesRepository.loadOrCreate(campaignId);
        CampaignDay activeDay = state.activeDay();
        TravelValidationResult travelValidation = CaravanBusinessRules.validateTravel(
                state.caravan(),
                catalogRegistry,
                ruleState,
                activeDay,
                buildTravelContext(activeDay, state.operations()));
        CalculationResult<CaravanCalculationSummary> summary = caravanCalculationService.calculateCaravanSummary(
                state.caravan(),
                catalogRegistry,
                ruleState,
                activeDay,
                buildCalculationContext(state.caravan(), activeDay, state.operations()));
        List<String> alerts = buildAlerts(travelValidation, summary);
        return new CampaignDayPreview(travelValidation, summary.value(), alerts);
    }

    @Override
    public CampaignDailyCycleState resolveDay(
            UUID campaignId,
            UUID dayId,
            List<CheckResolution> checkResolutions,
            List<CaravanEvent> caravanEvents,
            List<TradeTransaction> tradeTransactions,
            String actor,
            String source,
            String reason) {
        CampaignDailyCycleState state = loadOrCreateState(campaignId);
        CampaignRuleState ruleState = campaignRulesRepository.loadOrCreate(campaignId);
        CampaignDay activeDay = requireActiveDay(state, dayId);
        if (activeDay.status() == CampaignDayStatus.DRAFT) {
            activeDay = activeDay.withStatus(CampaignDayStatus.PLANNED);
        }
        if (activeDay.status() == CampaignDayStatus.PLANNED) {
            activeDay = activeDay.withStatus(CampaignDayStatus.RESOLVING);
        }

        Caravan caravan = state.caravan().replaceCampaignDay(activeDay);
        for (CheckResolution checkResolution : safeList(checkResolutions)) {
            caravan = caravan.withCheckResolution(checkResolution);
            activeDay = activeDay.addCheckResolution(checkResolution);
        }
        for (CaravanEvent caravanEvent : safeList(caravanEvents)) {
            caravan = caravan.withCaravanEvent(caravanEvent);
            activeDay = activeDay.addCaravanEvent(caravanEvent);
        }
        for (TradeTransaction tradeTransaction : safeList(tradeTransactions)) {
            caravan = caravan.withTradeTransaction(tradeTransaction);
            activeDay = activeDay.addTradeTransaction(tradeTransaction);
        }
        caravan = caravan.replaceCampaignDay(activeDay);

        CampaignDailyCycleState updatedState = state.withCaravan(caravan).withActiveDayId(activeDay.id());
        dailyCycleRepository.save(updatedState);
        appendAuditEntry(campaignId, ruleState.ruleSetVersionId(), "DAY", dayId.toString(), "RESOLVE_DAY", activeDay.activityType().name(), activeDay.location(), activeDay.location(), null, normalizeReason(reason), normalizeActor(actor), normalizeSource(source), now());
        return updatedState;
    }

    @Override
    public CampaignDaySummary closeDay(UUID campaignId, UUID dayId, String actor, String source, String reason) {
        CampaignDailyCycleState state = loadOrCreateState(campaignId);
        CampaignRuleState ruleState = campaignRulesRepository.loadOrCreate(campaignId);
        CampaignDay activeDay = requireActiveDay(state, dayId);

        TravelValidationResult validation = CaravanBusinessRules.validateTravel(state.caravan(), catalogRegistry, ruleState, activeDay, buildTravelContext(activeDay, state.operations()));
        CalculationResult<CaravanCalculationSummary> summaryResult = caravanCalculationService.calculateCaravanSummary(
                state.caravan(),
                catalogRegistry,
                ruleState,
                activeDay,
                buildCalculationContext(state.caravan(), activeDay, state.operations()));

        BigDecimal plannedDistance = activeDay.plannedDistanceMiles() == null ? ZERO : activeDay.plannedDistanceMiles();
        BigDecimal actualDistance = decideActualDistance(activeDay, summaryResult.value());
        BigDecimal plannedConsumption = summaryResult.value().dailyConsumption();
        BigDecimal availableSupplies = totalSupplyUnits(state.caravan());
        BigDecimal actualConsumption = plannedConsumption.min(availableSupplies);
        BigDecimal consumptionDeficit = plannedConsumption.subtract(actualConsumption);
        BigDecimal production = calculateProduction(state.caravan(), activeDay);
        BigDecimal discontentBefore = state.caravan().currentDiscontent();
        GiftAdjustmentResult giftAdjustment = applyGiftAdjustments(state.caravan(), state.operations());
        BigDecimal discontentAfter = applyDailyDiscontent(
                giftAdjustment.caravan(),
                activeDay,
                validation,
                summaryResult.value(),
                giftAdjustment.caravan().currentDiscontent());

        Caravan caravan = giftAdjustment.caravan();
        caravan = applyConsumption(caravan, actualConsumption);
        caravan = applyPerishableAdvance(caravan, activeDay);
        caravan = applyBeastFatigue(caravan, activeDay, summaryResult.value());
        caravan = caravan.withCurrentDiscontent(discontentAfter);

        List<String> alerts = buildAlerts(validation, summaryResult);
        CampaignDaySummary summary = new CampaignDaySummary(
                activeDay.id(),
                CampaignDayStatus.CLOSED,
                plannedDistance,
                actualDistance,
                plannedConsumption,
                actualConsumption,
                consumptionDeficit,
                production,
                discontentBefore,
                discontentAfter,
                validation.warnings(),
                validation.blockers(),
                alerts,
                now(),
                ruleState.ruleSetVersionId());

        CampaignDay closedDay = activeDay;
        if (closedDay.status() == CampaignDayStatus.DRAFT) {
            closedDay = closedDay.withStatus(CampaignDayStatus.PLANNED);
        }
        if (closedDay.status() == CampaignDayStatus.PLANNED) {
            closedDay = closedDay.withStatus(CampaignDayStatus.RESOLVING);
        }
        if (closedDay.status() != CampaignDayStatus.CLOSED) {
            closedDay = closedDay.withResolvedDistanceMiles(actualDistance).close();
        }
        caravan = caravan.replaceCampaignDay(closedDay);
        caravan = caravan.withLedgerEntry(new LedgerEntry(
                UUID.randomUUID(),
                closedDay.id(),
                LedgerOperationType.OTHER,
                LedgerResourceType.OTHER,
                closedDay.id(),
                actualConsumption.negate(),
                normalizeReason(reason),
                null,
                now()));

        CampaignDailyCycleState updatedState = state.withCaravan(caravan).withActiveDayId(closedDay.id()).withLastSummary(summary);
        dailyCycleRepository.save(updatedState);
        appendAuditEntry(campaignId, ruleState.ruleSetVersionId(), "DAY", dayId.toString(), "CLOSE_DAY", closedDay.activityType().name(), summary.actualDistanceMiles().toPlainString(), summary.consumptionDeficit().toPlainString(), reason, normalizeReason(reason), normalizeActor(actor), normalizeSource(source), now());
        return summary;
    }

    @Override
    public CampaignDailyCycleState reopenDay(UUID campaignId, UUID dayId, String actor, String source, String reason) {
        CampaignDailyCycleState state = loadOrCreateState(campaignId);
        CampaignRuleState ruleState = campaignRulesRepository.loadOrCreate(campaignId);
        CampaignDay activeDay = requireActiveDay(state, dayId);
        if (!activeDay.isClosed()) {
            throw new IllegalStateException("Only closed days can be reopened");
        }
        CampaignDay reopened = activeDay.reopen(CampaignDayStatus.RESOLVING);
        CampaignDailyCycleState updatedState = state.withCaravan(state.caravan().replaceCampaignDay(reopened)).withActiveDayId(reopened.id()).withLastSummary(null);
        dailyCycleRepository.save(updatedState);
        appendAuditEntry(campaignId, ruleState.ruleSetVersionId(), "DAY", dayId.toString(), "REOPEN_DAY", reopened.activityType().name(), reopened.location(), reopened.location(), null, normalizeReason(reason), normalizeActor(actor), normalizeSource(source), now());
        return updatedState;
    }

    private CampaignDailyCycleState loadOrCreateState(UUID campaignId) {
        return dailyCycleRepository.findByCampaignId(campaignId)
                .orElseGet(() -> {
                    CampaignRuleState ruleState = campaignRulesRepository.loadOrCreate(campaignId);
                    Caravan seededCaravan = seedCaravan(campaignId, ruleState.ruleSetVersionId(), UUID.randomUUID());
                    UUID dayId = seededCaravan.campaignDays().stream()
                            .findFirst()
                            .map(CampaignDay::id)
                            .orElseThrow(() -> new IllegalStateException("Seeded caravan must include at least one campaign day"));
                    CampaignDailyCycleState seeded = CampaignDailyCycleState.seeded(
                            campaignId,
                            seededCaravan,
                            dayId,
                            null);
                    dailyCycleRepository.save(seeded);
                    appendAuditEntry(campaignId, ruleState.ruleSetVersionId(), "DAY", campaignId.toString(), "SEED_DAY_CYCLE", "DRAFT", "Seeded daily cycle state", "Seeded daily cycle state", null, "Seeded default planner state", "System", "seed", now());
                    return seeded;
                });
    }

    private CampaignDay requireActiveDay(CampaignDailyCycleState state, UUID dayId) {
        CampaignDay activeDay = state.activeDay();
        if (!activeDay.id().equals(dayId)) {
            throw new IllegalArgumentException("dayId does not match the active campaign day");
        }
        return activeDay;
    }

    private Caravan applyPlan(
            Caravan caravan,
            List<DailyRoleAssignment> roleAssignments,
            List<CartPassengerAssignment> passengerAssignments,
            List<TowingAssignment> towingAssignments,
            List<DailyOperation> dailyOperations,
            UUID campaignDayId) {
        Caravan current = caravan;
        for (DailyRoleAssignment roleAssignment : safeList(roleAssignments)) {
            Traveller traveller = current.findTraveller(roleAssignment.travellerId()).orElseThrow(() -> new IllegalArgumentException("Unknown traveller " + roleAssignment.travellerId()));
            current = current.replaceTraveller(traveller.withDailyRoleAssignment(roleAssignment));
        }
        for (CartPassengerAssignment passengerAssignment : safeList(passengerAssignments)) {
            Cart cart = current.findCart(passengerAssignment.cartId()).orElseThrow(() -> new IllegalArgumentException("Unknown cart " + passengerAssignment.cartId()));
            current = current.replaceCart(cart.withPassengerAssignment(passengerAssignment));
        }
        for (TowingAssignment towingAssignment : safeList(towingAssignments)) {
            Beast beast = current.findBeast(towingAssignment.beastId()).orElseThrow(() -> new IllegalArgumentException("Unknown beast " + towingAssignment.beastId()));
            Cart cart = current.findCart(towingAssignment.cartId()).orElseThrow(() -> new IllegalArgumentException("Unknown cart " + towingAssignment.cartId()));
            current = current.replaceBeast(beast.assignToTowing(towingAssignment));
            current = current.replaceCart(cart.withTowingAssignment(towingAssignment));
        }
        if (!safeList(dailyOperations).isEmpty()) {
            current = appendDailyOperations(current, dailyOperations, campaignDayId);
        }
        return current;
    }

    private Caravan appendDailyOperations(Caravan caravan, List<DailyOperation> dailyOperations, UUID campaignDayId) {
        Caravan current = caravan;
        for (DailyOperation operation : dailyOperations) {
            LedgerResourceType resourceType = resolveResourceType(operation.resourceType());
            BigDecimal delta = resolveOperationDelta(operation);
            current = current.withLedgerEntry(new LedgerEntry(
                    UUID.randomUUID(),
                    campaignDayId,
                    resolveLedgerOperationType(operation.operationType()),
                    resourceType,
                    operation.id(),
                    delta,
                    operation.title(),
                    null,
                    now()));
        }
        return current;
    }

    private TravelContext buildTravelContext(CampaignDay activeDay, List<DailyOperation> dailyOperations) {
        boolean fasting = hasOperation(dailyOperations, DailyOperationType.FASTING);
        boolean celebration = hasOperation(dailyOperations, DailyOperationType.CELEBRATION);
        boolean frozenTerrain = activeDay.weatherSeverity() != null && activeDay.weatherSeverity().toLowerCase().contains("frozen");
        boolean nightTravel = activeDay.activityType() == CampaignDayActivityType.TRAVEL && activeDay.travelHours() != null && activeDay.travelHours().compareTo(BigDecimal.valueOf(8)) > 0;
        return new TravelContext(
                activeDay.terrainType(),
                frozenTerrain,
                nightTravel,
                activeDay.temperatureF(),
                ZERO,
                ZERO);
    }

    private CaravanCalculationContext buildCalculationContext(Caravan caravan, CampaignDay activeDay, List<DailyOperation> dailyOperations) {
        return new CaravanCalculationContext(
                buildTravelContext(activeDay, dailyOperations),
                caravan.activeFeatKeys(),
                hasOperation(dailyOperations, DailyOperationType.FASTING),
                hasOperation(dailyOperations, DailyOperationType.CELEBRATION),
                activeDay.settlementType() != null,
                activeDay.settlementType());
    }

    private List<String> buildAlerts(TravelValidationResult travelValidation, CalculationResult<CaravanCalculationSummary> summary) {
        List<String> alerts = new ArrayList<>();
        travelValidation.blockers().forEach(issue -> alerts.add(issue.code().name() + ": " + issue.message()));
        travelValidation.warnings().forEach(issue -> alerts.add(issue.code().name() + ": " + issue.message()));
        summary.blockers().forEach(issue -> alerts.add(issue.code().name() + ": " + issue.message()));
        summary.warnings().forEach(issue -> alerts.add(issue.code().name() + ": " + issue.message()));
        return alerts;
    }

    private BigDecimal decideActualDistance(CampaignDay activeDay, CaravanCalculationSummary summary) {
        if (activeDay.activityType() != CampaignDayActivityType.TRAVEL) {
            return ZERO;
        }
        BigDecimal plannedDistance = activeDay.plannedDistanceMiles() == null ? ZERO : activeDay.plannedDistanceMiles();
        BigDecimal speed = summary.speedMilesPerDay();
        if (plannedDistance.signum() <= 0) {
            return speed;
        }
        return plannedDistance.min(speed);
    }

    private BigDecimal totalSupplyUnits(Caravan caravan) {
        return caravan.inventoryLots().stream()
                .filter(lot -> "SUPPLIES".equalsIgnoreCase(lot.cargoTypeId()))
                .map(InventoryLot::quantity)
                .reduce(ZERO, BigDecimal::add);
    }

    private BigDecimal calculateProduction(Caravan caravan, CampaignDay campaignDay) {
        int cooks = CaravanBusinessRules.activeRoleCount(caravan, campaignDay.id(), "COOK");
        return BigDecimal.valueOf(cooks);
    }

    private GiftAdjustmentResult applyGiftAdjustments(Caravan caravan, List<DailyOperation> dailyOperations) {
        Caravan current = caravan;
        BigDecimal reduction = ZERO;
        for (DailyOperation operation : safeList(dailyOperations)) {
            if (operation.operationType() != DailyOperationType.GIFTING) {
                continue;
            }
            GiftAdjustment giftAdjustment = giftAdjustmentFor(current, operation);
            current = giftAdjustment.caravan();
            reduction = reduction.add(giftAdjustment.discontentReduction());
        }
        return new GiftAdjustmentResult(current, reduction);
    }

    private BigDecimal applyDailyDiscontent(Caravan caravan, CampaignDay activeDay, TravelValidationResult validation, CaravanCalculationSummary summary, BigDecimal initialDiscontent) {
        BigDecimal discontent = initialDiscontent;
        for (CaravanEvent caravanEvent : activeDay.caravanEvents()) {
            CalculationResult<BigDecimal> gain = caravanCalculationService.calculateEventDiscontentGain(caravanEvent.severity(), 0, campaignRulesRepository.loadOrCreate(caravan.campaignId()));
            discontent = discontent.add(gain.value());
        }
        if (CaravanBusinessRules.isMutinyTriggered(caravan)) {
            discontent = discontent.add(summary.mutinyPenalty().abs());
        }
        return discontent;
    }

    private GiftAdjustment giftAdjustmentFor(Caravan caravan, DailyOperation operation) {
        BigDecimal quantity = operation.quantity() == null ? ZERO : operation.quantity();
        if (quantity.signum() <= 0) {
            return new GiftAdjustment(caravan, ZERO);
        }
        String resourceType = operation.resourceType() == null ? "" : operation.resourceType().trim().toUpperCase();
        if (!"TREASURE".equals(resourceType) && !"SUPPLIES".equals(resourceType) && !"CARGO".equals(resourceType)) {
            return new GiftAdjustment(caravan, ZERO);
        }

        BigDecimal discontentReduction = BigDecimal.ONE;
        Caravan updatedCaravan = caravan;
        if ("TREASURE".equals(resourceType)) {
            if (quantity.compareTo(caravan.lastTreasureGiftQuantity()) > 0) {
                discontentReduction = BigDecimal.valueOf(3);
            }
            updatedCaravan = caravan.withLastTreasureGiftQuantity(quantity);
        } else {
            updatedCaravan = caravan.withLastCargoGiftQuantity(quantity);
        }

        BigDecimal adjustedDiscontent = updatedCaravan.currentDiscontent().subtract(discontentReduction);
        if (adjustedDiscontent.signum() < 0) {
            adjustedDiscontent = BigDecimal.ZERO;
        }
        updatedCaravan = updatedCaravan.withCurrentDiscontent(adjustedDiscontent);
        return new GiftAdjustment(updatedCaravan, discontentReduction);
    }

    private Caravan applyConsumption(Caravan caravan, BigDecimal actualConsumption) {
        BigDecimal remaining = actualConsumption;
        Caravan current = caravan;
        for (InventoryLot lot : current.inventoryLots()) {
            if (!"SUPPLIES".equalsIgnoreCase(lot.cargoTypeId()) || remaining.signum() <= 0) {
                continue;
            }
            BigDecimal consumed = lot.quantity().min(remaining);
            remaining = remaining.subtract(consumed);
            current = current.replaceInventoryLot(lot.withQuantity(lot.quantity().subtract(consumed)));
        }
        return current;
    }

    private Caravan applyPerishableAdvance(Caravan caravan, CampaignDay activeDay) {
        Caravan current = caravan;
        boolean protectedByFridge = current.operativeCarts().stream().anyMatch(cart -> cart.hasActiveUpgrade("FRIDGE"));
        for (InventoryLot lot : current.inventoryLots()) {
            InventoryLot advanced = CaravanBusinessRules.advancePerishableLot(lot, 1, protectedByFridge);
            current = current.replaceInventoryLot(advanced);
        }
        return current;
    }

    private Caravan applyBeastFatigue(Caravan caravan, CampaignDay activeDay, CaravanCalculationSummary summary) {
        Caravan current = caravan;
        for (Cart cart : current.operativeCarts()) {
            BigDecimal cartTowing = CaravanBusinessRules.calculateTowingStrength(current, catalogRegistry, activeDay.id()).value();
            BigDecimal required = CaravanBusinessRules.calculateRequiredTowingStrength(current, catalogRegistry).value();
            int threshold = CaravanBusinessRules.towingFatigueThresholdDays(cartTowing, required);
            for (Beast beast : current.beasts()) {
                if (beast.towingAssignment() == null || !activeDay.id().equals(beast.towingAssignment().campaignDayId()) || !beast.activeAsTowing()) {
                    continue;
                }
                int consecutiveDays = beast.towingAssignment().consecutiveTowingDays() + 1;
                Beast updated = beast.assignToTowing(beast.towingAssignment().withConsecutiveTowingDays(consecutiveDays));
                if (threshold > 0 && consecutiveDays >= threshold) {
                    updated = updated.withFatigued(true);
                }
                current = current.replaceBeast(updated);
            }
        }
        return current;
    }

    private LedgerOperationType resolveLedgerOperationType(DailyOperationType operationType) {
        return switch (operationType) {
            case TRAVEL -> LedgerOperationType.CONSUME_SUPPLIES;
            case REST -> LedgerOperationType.OTHER;
            case CIVILISED_PAUSE -> LedgerOperationType.OTHER;
            case REPAIR -> LedgerOperationType.REPAIR_CART;
            case EATING -> LedgerOperationType.CONSUME_SUPPLIES;
            case CELEBRATION -> LedgerOperationType.CONSUME_SUPPLIES;
            case GIFTING -> LedgerOperationType.ADJUST_INVENTORY;
            case FASTING -> LedgerOperationType.CONSUME_SUPPLIES;
            case COMMERCE -> LedgerOperationType.SELL_CARGO;
        };
    }

    private LedgerResourceType resolveResourceType(String resourceType) {
        if (resourceType == null) {
            return LedgerResourceType.OTHER;
        }
        return switch (resourceType.trim().toUpperCase()) {
            case "SUPPLIES" -> LedgerResourceType.SUPPLIES;
            case "TREASURE" -> LedgerResourceType.CARGO;
            case "REPAIR_MATERIALS" -> LedgerResourceType.INVENTORY_LOT;
            case "PROVISIONS" -> LedgerResourceType.SUPPLIES;
            default -> LedgerResourceType.OTHER;
        };
    }

    private BigDecimal resolveOperationDelta(DailyOperation operation) {
        return operation.quantity() == null ? ZERO : operation.quantity();
    }

    private boolean hasOperation(List<DailyOperation> operations, DailyOperationType operationType) {
        return safeList(operations).stream().anyMatch(operation -> operation.operationType() == operationType);
    }

    private List<DailyOperation> mergeOperations(List<DailyOperation> existing, List<DailyOperation> additions) {
        ArrayList<DailyOperation> merged = new ArrayList<>(safeList(existing));
        merged.addAll(safeList(additions));
        return List.copyOf(merged);
    }

    private static <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : values;
    }

    private record GiftAdjustmentResult(Caravan caravan, BigDecimal discontent) {
    }

    private record GiftAdjustment(Caravan caravan, BigDecimal discontentReduction) {
    }

    private Caravan seedCaravan(UUID campaignId, String ruleSetVersionId, UUID dayId) {
        return ObservedInitialStateSeed.create(catalogRegistry)
                .buildState(campaignId, ruleSetVersionId)
                .caravan();
    }

    private void appendAuditEntry(
            UUID campaignId,
            String ruleSetVersionId,
            String entryType,
            String subjectId,
            String operationType,
            String title,
            String currentResolution,
            String configurationValue,
            String reason,
            String normalizedReason,
            String actor,
            String source,
            Instant occurredAt) {
        String effectiveReason = normalizedReason != null ? normalizedReason : normalizeReason(reason);
        if (effectiveReason == null) {
            effectiveReason = "Daily cycle operation";
        }
        campaignAuditRepository.append(new CampaignAuditEntry(
                campaignId,
                ruleSetVersionId,
                entryType,
                "CAMPAIGN_DAY",
                subjectId,
                operationType,
                title,
                currentResolution,
                configurationValue,
                effectiveReason,
                actor,
                source,
                occurredAt));
    }

    private Instant now() {
        return Instant.now(clock);
    }

    private String normalizeReason(String reason) {
        if (reason == null) {
            return null;
        }
        String trimmed = reason.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeActor(String actor) {
        String normalized = normalizeReason(actor);
        return normalized == null ? "Director de juego" : normalized;
    }

    private String normalizeSource(String source) {
        String normalized = normalizeReason(source);
        return normalized == null ? "daily-cycle" : normalized;
    }
}
