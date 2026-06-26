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
