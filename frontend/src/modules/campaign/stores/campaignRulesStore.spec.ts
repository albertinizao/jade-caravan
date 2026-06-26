import { createPinia, setActivePinia } from 'pinia';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { getCampaignRulesOverview, resolveCampaignDecision } from '../api';
import type { CampaignRulesOverview } from '../types';
import { useCampaignRulesStore } from './campaignRulesStore';

vi.mock('../api', () => ({
  getCampaignRulesOverview: vi.fn(),
  resolveCampaignDecision: vi.fn(),
}));

describe('useCampaignRulesStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
  });

  it('loads the overview and exposes unresolved blockers', async () => {
    const overview: CampaignRulesOverview = {
      summary: {
        ruleSetVersionId: 'decision-gate-v1',
        automationBlocked: true,
        unresolvedBlockers: [
          {
            key: 'D-05',
            decisionKey: 'D_05_STOVE_HEAT_SCOPE',
            title: 'Estufa y ámbito de su calor',
            description: 'La estufa duplica el beneficio del carbón para hasta 20 criaturas y no se acumula con carros aislados, pero no indica si calienta un conjunto de carros, un campamento o viajeros concretos.',
            defaultProposal: 'Acción de campamento sobre viajeros',
            currentResolution: null,
            configurationValue: null,
            reason: null,
          },
        ],
      },
      decisionGateItems: [
        {
          key: 'D-05',
          decisionKey: 'D_05_STOVE_HEAT_SCOPE',
          title: 'Estufa y ámbito de su calor',
          description: 'La estufa duplica el beneficio del carbón para hasta 20 criaturas y no se acumula con carros aislados, pero no indica si calienta un conjunto de carros, un campamento o viajeros concretos.',
          resolutionState: 'unresolved',
          defaultProposal: 'Acción de campamento sobre viajeros',
          blocksAutomation: true,
          currentResolution: null,
          reason: null,
        },
      ],
      auditTrail: [
        {
          ruleSetVersionId: 'decision-gate-v1',
          entryType: 'RULE',
          subjectType: 'RULE_DECISION',
          subjectId: 'D_05_STOVE_HEAT_SCOPE',
          operationType: 'RESOLVE_DECISION',
          decisionKey: 'D_05_STOVE_HEAT_SCOPE',
          decisionTitle: 'Estufa y ámbito de su calor',
          currentResolution: 'Acción de campamento sobre viajeros',
          configurationValue: null,
          reason: 'Se mantiene como una resolución manual auditable.',
          actor: 'Director de juego',
          source: 'Decisión manual de campaña',
          resolvedAt: '2026-06-26T18:00:00Z',
        },
      ],
    };

    vi.mocked(getCampaignRulesOverview).mockResolvedValue(overview);

    const store = useCampaignRulesStore();

    await store.loadCampaignRules('demo');

    expect(store.status).toBe('ready');
    expect(store.summary?.automationBlocked).toBe(true);
    expect(store.unresolvedBlockers).toHaveLength(1);
  });

  it('resolves a decision and reloads the overview', async () => {
    const initialOverview: CampaignRulesOverview = {
      summary: {
        ruleSetVersionId: 'decision-gate-v1',
        automationBlocked: true,
        unresolvedBlockers: [],
      },
      decisionGateItems: [
        {
          key: 'D-07',
          decisionKey: 'D_07_COLD_INSULATION_PENALTY',
          title: 'Aislamiento para el calor',
          description: 'La actualización cambia explícitamente el aislamiento de frío a -4 millas/día una vez. No modifica el de calor.',
          resolutionState: 'unresolved',
          defaultProposal: 'Aplicar -1 milla/día',
          blocksAutomation: true,
          currentResolution: null,
          reason: null,
        },
      ],
      auditTrail: [],
    };

    const resolvedOverview: CampaignRulesOverview = {
      summary: {
        ruleSetVersionId: 'decision-gate-v1',
        automationBlocked: false,
        unresolvedBlockers: [],
      },
      decisionGateItems: [
        {
          key: 'D-07',
          decisionKey: 'D_07_COLD_INSULATION_PENALTY',
          title: 'Aislamiento para el calor',
          description: 'La actualización cambia explícitamente el aislamiento de frío a -4 millas/día una vez. No modifica el de calor.',
          resolutionState: 'resolved',
          defaultProposal: 'Aplicar -1 milla/día',
          blocksAutomation: false,
          currentResolution: '-1',
          reason: 'Se adopta la penalización configurada para esta campaña.',
          configurationValue: '-1',
        },
      ],
      auditTrail: [],
    };

    vi.mocked(resolveCampaignDecision).mockResolvedValue(undefined);
    vi.mocked(getCampaignRulesOverview)
      .mockResolvedValueOnce(initialOverview)
      .mockResolvedValueOnce(resolvedOverview);

    const store = useCampaignRulesStore();

    await store.loadCampaignRules('demo');
    await store.resolveDecision('demo', {
      decisionKey: 'D-07',
      reason: 'Se adopta la penalización configurada para esta campaña.',
      configurationValue: '-1',
      actor: 'Director de juego',
      source: 'Decisión manual de campaña',
    });

    expect(resolveCampaignDecision).toHaveBeenCalledWith('demo', {
      decisionKey: 'D-07',
      reason: 'Se adopta la penalización configurada para esta campaña.',
      configurationValue: '-1',
      actor: 'Director de juego',
      source: 'Decisión manual de campaña',
    });
    expect(store.decisionGateItems[0]?.resolutionState).toBe('resolved');
    expect(store.decisionGateItems[0]?.reason).toContain('penalización configurada');
  });
});
