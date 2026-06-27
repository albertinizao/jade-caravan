export interface CampaignDashboardSummary {
  campaignId: string;
  campaignName: string;
  status: 'draft' | 'planned' | 'resolving' | 'closed';
  cartCount: number;
  travellerCount: number;
  notes: string;
}

export interface CartTypeSummary {
  key: string;
  name: string;
  category?: string | null;
  cost?: string | null;
  costCp?: string | null;
  hitPoints?: number | null;
  hardness?: number | null;
  propulsionRequirement?: number | null;
  towingCreatureLimit?: string | null;
  consumption?: string | null;
  passengerCapacity?: string | null;
  cargoCapacity?: string | null;
  restrictions?: string[];
  effects?: string[];
  campaignSpecific?: boolean;
  source?: string | null;
  note?: string | null;
}

export interface UpgradeSummary {
  key: string;
  name: string;
  cost?: string | null;
  costCp?: string | null;
  restriction?: string | null;
  stackingRule?: string | null;
  incompatibilities?: string[];
  effect?: string | null;
  campaignSpecific?: boolean;
  source?: string | null;
  note?: string | null;
}

export interface RoleSummary {
  key: string;
  name: string;
  hardLimit?: string | null;
  requirement?: string | null;
  benefitSummary?: string | null;
  optionalSubsystem?: boolean;
  campaignSpecific?: boolean;
  source?: string | null;
  note?: string | null;
}

export interface BeastTypeSummary {
  key: string;
  name: string;
  priceBase?: string | null;
  priceBaseCp?: string | null;
  priceTrained?: string | null;
  priceTrainedCp?: string | null;
  strength?: number | null;
  size?: string | null;
  speedFeet?: number | null;
  temperatureAdaptation?: number | null;
  adaptationNotes?: string | null;
  campaignSpecific?: boolean;
  source?: string | null;
  note?: string | null;
}

export interface CartUpgradeInstanceSummary {
  cartId: string;
  upgrade: UpgradeSummary;
  active: boolean;
  notes?: string | null;
}

export interface CartPassengerAssignmentSummary {
  cartId: string;
  travellerId: string;
  occupancyUnits: string;
  notes?: string | null;
}

export interface CartCargoAllocationSummary {
  cartId: string;
  inventoryLotId: string;
  quantity: string;
  notes?: string | null;
}

export interface TowingAssignmentSummary {
  beastId: string;
  cartId: string;
  campaignDayId: string;
  consecutiveTowingDays: number;
}

export interface TravellerContractSummary {
  contractType: string;
  monthlyCostCp: number;
  active: boolean;
  notes?: string | null;
  signedAt?: string | null;
}

export interface TravellerRelationSummary {
  sourceTravellerId: string;
  targetTravellerId: string;
  relationType: string;
  notes?: string | null;
}

export interface RoleCapabilitySummary {
  role: RoleSummary;
  source: string;
  notes?: string | null;
}

export interface DailyRoleAssignmentSummary {
  travellerId: string;
  campaignDayId: string;
  role: RoleSummary;
  targetCartId?: string | null;
  targetTravellerId?: string | null;
  targetSkill?: string | null;
  targetLanguage?: string | null;
  optionJson?: string | null;
}

export interface CheckResolutionSummary {
  id: string;
  campaignDayId: string;
  checkType: string;
  modifiers: Array<Record<string, unknown>>;
  dc: number;
  naturalRoll?: number | null;
  total?: number | null;
  outcome: string;
  notes?: string | null;
}

export interface CaravanEventSummary {
  id: string;
  campaignDayId: string;
  source: string;
  severity: string;
  narrativeSummary: string;
  requiresCheck: boolean;
  checkResolutionId?: string | null;
  resolved: boolean;
  effectsApplied: boolean;
}

export interface TradeTransactionSummary {
  id: string;
  campaignDayId: string;
  transactionType: string;
  cargoTypeId: string;
  quantity: string;
  unitValueCp: number;
  totalValueCp: number;
  inventoryLotId?: string | null;
  notes?: string | null;
}

export interface LedgerEntrySummary {
  id: string;
  campaignDayId: string;
  operationType: string;
  resourceType: string;
  resourceId?: string | null;
  delta: string;
  reason: string;
  relatedEventId?: string | null;
  createdAt: string;
}

export type DecisionGateResolutionState = 'unresolved' | 'resolved';

export interface DecisionGateBlockerSummary {
  key: string;
  decisionKey: string;
  title: string;
  description: string;
  defaultProposal: string;
  currentResolution?: string | null;
  configurationValue?: string | null;
  reason?: string | null;
}

export interface CampaignRulesSummary {
  ruleSetVersionId: string;
  automationBlocked: boolean;
  unresolvedBlockers: DecisionGateBlockerSummary[];
}

export interface DecisionGateItem {
  key: string;
  decisionKey: string;
  title: string;
  description: string;
  resolutionState: DecisionGateResolutionState;
  defaultProposal: string;
  blocksAutomation: boolean;
  currentResolution?: string | null;
  resolvedReason?: string;
  configurationValue?: string | null;
  reason?: string | null;
  actor?: string | null;
  source?: string | null;
  resolvedAt?: string | null;
}

export interface CampaignRuleAuditEntry {
  ruleSetVersionId: string;
  entryType: string;
  subjectType: string;
  subjectId?: string | null;
  operationType: string;
  decisionKey: string;
  decisionTitle: string;
  currentResolution: string;
  configurationValue?: string | null;
  reason: string;
  actor: string;
  source: string;
  resolvedAt: string;
}

export interface CampaignRulesOverview {
  summary: CampaignRulesSummary;
  decisionGateItems: DecisionGateItem[];
  auditTrail: CampaignRuleAuditEntry[];
}

export interface ResolveDecisionGateRequest {
  decisionKey: string;
  reason: string;
  configurationValue?: string;
  actor?: string;
  source?: string;
}

export interface CalculationBreakdownItem {
  concept: string;
  value: string;
  source: string;
  notes?: string | null;
}

export interface CalculationIssue {
  code: string;
  message: string;
  subject: string;
  details: Record<string, string>;
  source: string;
}

export interface TravelValidationResult {
  travelAllowed: boolean;
  passengerOccupancy: string;
  passengerCapacity: string;
  cargoOccupancy: string;
  cargoCapacity: string;
  towingStrength: string;
  requiredTowingStrength: string;
  breakdown: CalculationBreakdownItem[];
  warnings: CalculationIssue[];
  blockers: CalculationIssue[];
  ruleSetVersionId: string;
}

export interface CaravanCalculationSummary {
  effectiveCarts: Array<Record<string, unknown>>;
  passengerCapacity: string;
  cargoCapacity: string;
  passengerOccupancy: string;
  cargoOccupancy: string;
  towingStrength: string;
  requiredTowingStrength: string;
  speedMilesPerDay: string;
  dailyConsumption: string;
  mutinyPenalty: string;
  breakdown: CalculationBreakdownItem[];
  warnings: CalculationIssue[];
  blockers: CalculationIssue[];
  ruleSetVersionId: string;
}

export interface DailyOperation {
  id: string;
  campaignDayId: string;
  operationType: 'TRAVEL' | 'REST' | 'CIVILISED_PAUSE' | 'REPAIR' | 'EATING' | 'CELEBRATION' | 'GIFTING' | 'FASTING' | 'COMMERCE';
  title: string;
  quantity?: string | null;
  resourceType?: string | null;
  notes?: string | null;
}

export interface CampaignDay {
  id: string;
  caravanId: string;
  dayNumber: number;
  status: 'DRAFT' | 'PLANNED' | 'RESOLVING' | 'CLOSED' | 'CANCELLED';
  activityType: 'TRAVEL' | 'REST' | 'CIVILIZATION_PAUSE' | 'WORK' | 'CAMP' | 'COMBAT';
  terrainType: string;
  location: string;
  settlementType?: string | null;
  temperatureF?: number | null;
  weatherSeverity?: string | null;
  travelHours?: string | null;
  plannedDistanceMiles?: string | null;
  resolvedDistanceMiles?: string | null;
  checkResolutions: CheckResolutionSummary[];
  caravanEvents: CaravanEventSummary[];
  tradeTransactions: TradeTransactionSummary[];
}

export interface CaravanStats {
  offense: number;
  defense: number;
  mobility: number;
  morale: number;
}

export interface TravellerSummary {
  id: string;
  name: string;
  playerCharacter?: boolean;
  humanoid?: boolean;
  size?: string | null;
  foodConsumption?: number | null;
  occupancyUnits?: string | null;
  countsAsTraveller?: boolean;
  needsRest?: boolean;
  needsFood?: boolean;
  baseAttackBonus?: number | null;
  hitDice?: number | null;
  alive?: boolean;
  conscious?: boolean;
  status?: string | null;
  contract?: TravellerContractSummary | null;
  relations?: TravellerRelationSummary[];
  roleCapabilities?: RoleCapabilitySummary[];
  dailyRoleAssignments: DailyRoleAssignmentSummary[];
}

export interface CartSummary {
  id: string;
  name: string;
  cartType?: CartTypeSummary | null;
  destroyed: boolean;
  currentHitPoints: number;
  notes?: string | null;
  upgradeInstances?: CartUpgradeInstanceSummary[];
  towingAssignments: TowingAssignmentSummary[];
  passengerAssignments: CartPassengerAssignmentSummary[];
  cargoAllocations?: CartCargoAllocationSummary[];
}

export interface BeastSummary {
  id: string;
  name: string;
  beastType?: BeastTypeSummary | null;
  currentHitPoints?: number | null;
  trainedForCombat?: boolean;
  activeAsTowing: boolean;
  fatigued: boolean;
  towingAssignment?: TowingAssignmentSummary | null;
  notes?: string | null;
}

export interface InventoryLotSummary {
  id: string;
  cargoTypeId: string;
  quantity: string;
  unitCapacity?: string | null;
  unitValueCp?: number | null;
  cartId?: string | null;
  originSettlementId?: string | null;
  remainingProvisions?: string | null;
  perishableDecayProgress?: string | null;
  metadata?: Record<string, string>;
}

export interface CampaignDaySummary {
  campaignDayId: string;
  status: 'DRAFT' | 'PLANNED' | 'RESOLVING' | 'CLOSED' | 'CANCELLED';
  plannedDistanceMiles: string;
  actualDistanceMiles: string;
  plannedConsumption: string;
  actualConsumption: string;
  consumptionDeficit: string;
  production: string;
  discontentBefore: string;
  discontentAfter: string;
  warnings: CalculationIssue[];
  blockers: CalculationIssue[];
  continuingAlerts: string[];
  closedAt: string;
  ruleSetVersionId: string;
}

export interface CampaignDailyCycleState {
  campaignId: string;
  caravan: {
    id: string;
    campaignId: string;
    name: string;
    level: number;
    ruleSetVersionId: string;
    baseStats: CaravanStats;
    currentDiscontent: string;
    currentDayNumber: number;
    travellers: TravellerSummary[];
    carts: CartSummary[];
    beasts: BeastSummary[];
    inventoryLots: InventoryLotSummary[];
    campaignDays: CampaignDay[];
    checkResolutions: CheckResolutionSummary[];
    caravanEvents: CaravanEventSummary[];
    tradeTransactions: TradeTransactionSummary[];
    ledgerEntries: LedgerEntrySummary[];
  };
  activeDayId: string;
  lastSummary?: CampaignDaySummary | null;
  operations: DailyOperation[];
}

export interface CampaignDayPreview {
  travelValidation: TravelValidationResult;
  calculationSummary: CaravanCalculationSummary;
  alerts: string[];
}

export interface CampaignDayCreateRequest {
  campaignDay: CampaignDay;
  actor?: string;
  source?: string;
  reason?: string;
}

export interface CampaignDayPlanRequest {
  roleAssignments: Array<Record<string, unknown>>;
  passengerAssignments: Array<Record<string, unknown>>;
  towingAssignments: Array<Record<string, unknown>>;
  dailyOperations: DailyOperation[];
  overrideBlockers: boolean;
  overrideReason?: string;
  actor?: string;
  source?: string;
}

export interface CampaignDayResolveRequest {
  checkResolutions: Array<Record<string, unknown>>;
  caravanEvents: Array<Record<string, unknown>>;
  tradeTransactions: Array<Record<string, unknown>>;
  actor?: string;
  source?: string;
  reason?: string;
}

export interface CampaignDayCloseRequest {
  actor?: string;
  source?: string;
  reason?: string;
}

export interface CampaignDayReopenRequest {
  actor?: string;
  source?: string;
  reason?: string;
}
