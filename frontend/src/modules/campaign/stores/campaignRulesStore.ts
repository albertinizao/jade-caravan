import { defineStore } from 'pinia';
import type { CampaignRulesOverview, ResolveDecisionGateRequest } from '../types';
import { getCampaignRulesOverview, resolveCampaignDecision } from '../api';

interface CampaignRulesState {
  campaignId: string | null;
  overview: CampaignRulesOverview | null;
  status: 'idle' | 'loading' | 'ready' | 'saving' | 'error';
  errorMessage: string | null;
  savingDecisionKey: string | null;
}

function toErrorMessage(error: unknown): string {
  if (error instanceof Error && error.message) {
    return error.message;
  }

  return 'No se han podido cargar o guardar las decisiones de reglas.';
}

export const useCampaignRulesStore = defineStore('campaignRules', {
  state: (): CampaignRulesState => ({
    campaignId: null,
    overview: null,
    status: 'idle',
    errorMessage: null,
    savingDecisionKey: null,
  }),
  getters: {
    decisionGateItems: (state) => state.overview?.decisionGateItems ?? [],
    summary: (state) => state.overview?.summary ?? null,
    unresolvedBlockers: (state) =>
      state.overview?.summary.unresolvedBlockers ??
      (state.overview?.decisionGateItems ?? [])
        .filter((item) => item.resolutionState === 'unresolved' && item.blocksAutomation)
        .map((item) => ({
          key: item.key,
          decisionKey: item.decisionKey,
          title: item.title,
          description: item.description,
          defaultProposal: item.defaultProposal,
          currentResolution: item.currentResolution,
          configurationValue: item.configurationValue,
          reason: item.reason ?? item.resolvedReason ?? null,
        })),
  },
  actions: {
    async loadCampaignRules(campaignId: string) {
      this.campaignId = campaignId;
      this.status = 'loading';
      this.errorMessage = null;

      try {
        this.overview = await getCampaignRulesOverview(campaignId);
        this.status = 'ready';
      } catch (error) {
        this.overview = null;
        this.status = 'error';
        this.errorMessage = toErrorMessage(error);
        throw error;
      }
    },
    async resolveDecision(campaignId: string, request: ResolveDecisionGateRequest) {
      this.savingDecisionKey = request.decisionKey;
      this.status = 'saving';
      this.errorMessage = null;

      try {
        await resolveCampaignDecision(campaignId, request);
        await this.loadCampaignRules(campaignId);
      } catch (error) {
        this.status = 'error';
        this.errorMessage = toErrorMessage(error);
        throw error;
      } finally {
        this.savingDecisionKey = null;
      }
    },
  },
});
