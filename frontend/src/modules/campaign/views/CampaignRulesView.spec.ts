import { createPinia } from 'pinia';
import { flushPromises, mount } from '@vue/test-utils';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import CampaignRulesView from './CampaignRulesView.vue';
import { getCampaignRulesOverview, resolveCampaignDecision } from '../api';
import type { CampaignRulesOverview } from '../types';

vi.mock('../api', () => ({
  getCampaignRulesOverview: vi.fn(),
  resolveCampaignDecision: vi.fn(),
}));

const unresolvedOverview: CampaignRulesOverview = {
  summary: {
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
};

const resolvedOverview: CampaignRulesOverview = {
  summary: {
    automationBlocked: false,
    unresolvedBlockers: [],
  },
  decisionGateItems: [
    {
      key: 'D-05',
      decisionKey: 'D_05_STOVE_HEAT_SCOPE',
      title: 'Estufa y ámbito de su calor',
      description: 'La estufa duplica el beneficio del carbón para hasta 20 criaturas y no se acumula con carros aislados, pero no indica si calienta un conjunto de carros, un campamento o viajeros concretos.',
      resolutionState: 'resolved',
      defaultProposal: 'Acción de campamento sobre viajeros',
      blocksAutomation: false,
      currentResolution: '20',
      reason: 'Se usa la opción configurable definida por campaña.',
      configurationValue: '20',
    },
  ],
};

describe('CampaignRulesView', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders the active rules summary and unresolved blockers', async () => {
    vi.mocked(getCampaignRulesOverview).mockResolvedValue(unresolvedOverview);

    const wrapper = mount(CampaignRulesView, {
      props: {
        campaignId: 'demo',
      },
      global: {
        plugins: [createPinia()],
        stubs: {
          RouterLink: true,
        },
      },
    });

    await flushPromises();

    expect(wrapper.text()).toContain('Decisiones de reglas');
    expect(wrapper.text()).toContain('Resumen de reglas activas');
    expect(wrapper.text()).toContain('Bloquea automatización');
    expect(wrapper.text()).toContain('D-05');
    expect(wrapper.text()).toContain('Estufa y ámbito de su calor');
  });

  it('submits a decision resolution and refreshes the view', async () => {
    vi.mocked(getCampaignRulesOverview)
      .mockResolvedValueOnce(unresolvedOverview)
      .mockResolvedValueOnce(resolvedOverview);
    vi.mocked(resolveCampaignDecision).mockResolvedValue(undefined);

    const wrapper = mount(CampaignRulesView, {
      props: {
        campaignId: 'demo',
      },
      global: {
        plugins: [createPinia()],
        stubs: {
          RouterLink: true,
        },
      },
    });

    await flushPromises();

    await wrapper.get('textarea').setValue('Se usa la opción configurable definida por campaña.');
    await wrapper.get('input[type="text"]').setValue('20');
    await wrapper.get('form').trigger('submit.prevent');

    await flushPromises();

    expect(resolveCampaignDecision).toHaveBeenCalledWith('demo', {
      decisionKey: 'D_05_STOVE_HEAT_SCOPE',
      reason: 'Se usa la opción configurable definida por campaña.',
      configurationValue: '20',
    });
    expect(wrapper.text()).toContain('Resolución registrada');
    expect(wrapper.text()).toContain('Resuelta');
  });
});
