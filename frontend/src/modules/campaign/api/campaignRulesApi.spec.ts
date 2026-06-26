import { beforeEach, describe, expect, it, vi } from 'vitest';
import { httpClient } from '@/api/httpClient';
import { getCampaignRulesOverview, resolveCampaignDecision } from './campaignRulesApi';

vi.mock('@/api/httpClient', () => ({
  httpClient: {
    get: vi.fn(),
    post: vi.fn(),
  },
}));

describe('campaignRulesApi', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('loads the active rules overview from the campaign rules endpoint', async () => {
    vi.mocked(httpClient.get)
      .mockResolvedValueOnce({
        ruleSetVersionId: 'decision-gate-v1',
        automationBlocked: true,
        unresolvedDecisions: [],
      })
      .mockResolvedValueOnce([])
      .mockResolvedValueOnce([]);

    await getCampaignRulesOverview('demo');

    expect(httpClient.get).toHaveBeenNthCalledWith(1, '/campaigns/demo/rules/active');
    expect(httpClient.get).toHaveBeenNthCalledWith(2, '/campaigns/demo/rules/decisions');
    expect(httpClient.get).toHaveBeenNthCalledWith(3, '/campaigns/demo/audit');
  });

  it('posts the resolution payload to the decision endpoint', async () => {
    vi.mocked(httpClient.post).mockResolvedValue(undefined);

    await resolveCampaignDecision('demo', {
      decisionKey: 'D-05',
      reason: 'Se ha confirmado con el director.',
      configurationValue: '20',
      actor: 'Director de juego',
      source: 'Decisión manual de campaña',
    });

    expect(httpClient.post).toHaveBeenCalledWith('/campaigns/demo/rules/decisions', {
      decisionKey: 'D-05',
      reason: 'Se ha confirmado con el director.',
      configurationValue: '20',
      actor: 'Director de juego',
      source: 'Decisión manual de campaña',
    });
  });
});
