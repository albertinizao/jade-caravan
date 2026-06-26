import { httpClient } from '@/api/httpClient';
import type {
  CampaignRulesOverview,
  DecisionGateBlockerSummary,
  DecisionGateItem,
  ResolveDecisionGateRequest,
} from '../types';

interface CampaignRuleDecisionResponseDto {
  decisionKey: string;
  title: string;
  description: string;
  defaultProposal: string;
  status: 'PENDING' | 'RESOLVED';
  currentResolution: string | null;
  configurationValue: string | null;
  reason: string | null;
  blockingAutomation: boolean;
}

interface CampaignRuleGateSummaryResponseDto {
  automationBlocked: boolean;
  unresolvedDecisions: CampaignRuleDecisionResponseDto[];
}

function formatDecisionKey(decisionKey: string): string {
  const match = decisionKey.match(/^D_(\d{2})_/);
  if (match) {
    return `D-${match[1]}`;
  }

  return decisionKey.replaceAll('_', ' ');
}

function mapDecision(dto: CampaignRuleDecisionResponseDto): DecisionGateItem {
  return {
    key: formatDecisionKey(dto.decisionKey),
    decisionKey: dto.decisionKey,
    title: dto.title,
    description: dto.description,
    resolutionState: dto.status === 'RESOLVED' ? 'resolved' : 'unresolved',
    defaultProposal: dto.defaultProposal,
    blocksAutomation: dto.blockingAutomation,
    currentResolution: dto.currentResolution,
    resolvedReason: dto.reason ?? undefined,
    configurationValue: dto.configurationValue,
    reason: dto.reason,
  };
}

function mapBlocker(dto: CampaignRuleDecisionResponseDto): DecisionGateBlockerSummary {
  return {
    key: formatDecisionKey(dto.decisionKey),
    decisionKey: dto.decisionKey,
    title: dto.title,
    description: dto.description,
    defaultProposal: dto.defaultProposal,
    currentResolution: dto.currentResolution,
    configurationValue: dto.configurationValue,
    reason: dto.reason,
  };
}

export async function getCampaignRulesOverview(campaignId: string): Promise<CampaignRulesOverview> {
  const [summary, decisions] = await Promise.all([
    httpClient.get<CampaignRuleGateSummaryResponseDto>(`/campaigns/${campaignId}/rules/active`),
    httpClient.get<CampaignRuleDecisionResponseDto[]>(`/campaigns/${campaignId}/rules/decisions`),
  ]);

  return {
    summary: {
      automationBlocked: summary.automationBlocked,
      unresolvedBlockers: summary.unresolvedDecisions.map(mapBlocker),
    },
    decisionGateItems: decisions.map(mapDecision),
  };
}

export function resolveCampaignDecision(
  campaignId: string,
  request: ResolveDecisionGateRequest,
): Promise<void> {
  return httpClient.post<void>(`/campaigns/${campaignId}/rules/decisions`, request);
}
