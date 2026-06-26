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
}

export interface CampaignRulesOverview {
  summary: CampaignRulesSummary;
  decisionGateItems: DecisionGateItem[];
}

export interface ResolveDecisionGateRequest {
  decisionKey: string;
  reason: string;
  configurationValue?: string;
}
