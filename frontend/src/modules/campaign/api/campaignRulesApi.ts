import { httpClient } from '@/api/httpClient';
import type {
  CampaignRulesOverview,
  DecisionGateBlockerSummary,
  CampaignRuleAuditEntry,
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
  actor: string | null;
  source: string | null;
  resolvedAt: string | null;
  blockingAutomation: boolean;
}

interface CampaignRuleGateSummaryResponseDto {
  ruleSetVersionId: string;
  automationBlocked: boolean;
  unresolvedDecisions: CampaignRuleDecisionResponseDto[];
}

interface CampaignRuleAuditEntryResponseDto {
  ruleSetVersionId: string;
  entryType: string;
  subjectType: string;
  subjectId: string | null;
  operationType: string;
  decisionKey: string;
  title: string;
  currentResolution: string;
  configurationValue: string | null;
  reason: string;
  actor: string;
  source: string;
  resolvedAt: string;
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
    actor: dto.actor,
    source: dto.source,
    resolvedAt: dto.resolvedAt,
  };
}

function mapAuditEntry(dto: CampaignRuleAuditEntryResponseDto): CampaignRuleAuditEntry {
  return {
    ruleSetVersionId: dto.ruleSetVersionId,
    entryType: dto.entryType,
    subjectType: dto.subjectType,
    subjectId: dto.subjectId,
    operationType: dto.operationType,
    decisionKey: dto.decisionKey,
    decisionTitle: dto.title,
    currentResolution: dto.currentResolution,
    configurationValue: dto.configurationValue,
    reason: dto.reason,
    actor: dto.actor,
    source: dto.source,
    resolvedAt: dto.resolvedAt,
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
  const [summary, decisions, auditTrail] = await Promise.all([
    httpClient.get<CampaignRuleGateSummaryResponseDto>(`/campaigns/${campaignId}/rules/active`),
    httpClient.get<CampaignRuleDecisionResponseDto[]>(`/campaigns/${campaignId}/rules/decisions`),
    httpClient.get<CampaignRuleAuditEntryResponseDto[]>(`/campaigns/${campaignId}/audit`),
  ]);

  return {
    summary: {
      ruleSetVersionId: summary.ruleSetVersionId,
      automationBlocked: summary.automationBlocked,
      unresolvedBlockers: summary.unresolvedDecisions.map(mapBlocker),
    },
    decisionGateItems: decisions.map(mapDecision),
    auditTrail: auditTrail.map(mapAuditEntry),
  };
}

export function resolveCampaignDecision(
  campaignId: string,
  request: ResolveDecisionGateRequest,
): Promise<void> {
  return httpClient.post<void>(`/campaigns/${campaignId}/rules/decisions`, request);
}
