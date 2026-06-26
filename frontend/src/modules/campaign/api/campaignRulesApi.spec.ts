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
        automationBlocked: true,
        unresolvedDecisions: [],
      })
      .mockResolvedValueOnce([]);

    await getCampaignRulesOverview('demo');

    expect(httpClient.get).toHaveBeenNthCalledWith(1, '/campaigns/demo/rules/active');
    expect(httpClient.get).toHaveBeenNthCalledWith(2, '/campaigns/demo/rules/decisions');
  });

  it('posts the resolution payload to the decision endpoint', async () => {
    vi.mocked(httpClient.post).mockResolvedValue(undefined);

    await resolveCampaignDecision('demo', {
      decisionKey: 'D-05',
      reason: 'Se ha confirmado con el director.',
      configurationValue: '20',
    });

    expect(httpClient.post).toHaveBeenCalledWith('/campaigns/demo/rules/decisions', {
      decisionKey: 'D-05',
      reason: 'Se ha confirmado con el director.',
      configurationValue: '20',
    });
  });
});
