export interface CampaignDashboardSummary {
  campaignId: string;
  campaignName: string;
  status: 'draft' | 'planned' | 'resolving' | 'closed';
  cartCount: number;
  travellerCount: number;
  notes: string;
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
  checkResolutions: Array<Record<string, unknown>>;
  caravanEvents: Array<Record<string, unknown>>;
  tradeTransactions: Array<Record<string, unknown>>;
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
  dailyRoleAssignments: Array<Record<string, unknown>>;
}

export interface CartSummary {
  id: string;
  name: string;
  destroyed: boolean;
  currentHitPoints: number;
  towingAssignments: Array<Record<string, unknown>>;
  passengerAssignments: Array<Record<string, unknown>>;
}

export interface BeastSummary {
  id: string;
  name: string;
  activeAsTowing: boolean;
  fatigued: boolean;
  towingAssignment?: Record<string, unknown> | null;
}

export interface InventoryLotSummary {
  id: string;
  cargoTypeId: string;
  quantity: string;
  remainingProvisions?: string | null;
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
    checkResolutions: Array<Record<string, unknown>>;
    caravanEvents: Array<Record<string, unknown>>;
    tradeTransactions: Array<Record<string, unknown>>;
    ledgerEntries: Array<Record<string, unknown>>;
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
