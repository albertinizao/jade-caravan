import { createPinia, setActivePinia } from 'pinia';
import { beforeEach, describe, expect, it } from 'vitest';
import { useCampaignStore } from './campaignStore';

describe('useCampaignStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    window.localStorage.clear();
  });

  it('persists the selected campaign and remembers recent campaigns', () => {
    const store = useCampaignStore();

    store.selectCampaign('campaign-alpha');
    store.selectCampaign('campaign-beta');

    expect(store.campaignId).toBe('campaign-beta');
    expect(store.recentCampaignIds).toEqual(['campaign-beta', 'campaign-alpha']);
    expect(window.localStorage.getItem('jade-caravan.current-campaign')).toBe('campaign-beta');
    expect(window.localStorage.getItem('jade-caravan.recent-campaigns')).toContain('campaign-alpha');
  });

  it('restores the persisted campaign session', () => {
    window.localStorage.setItem('jade-caravan.current-campaign', 'campaign-gamma');
    window.localStorage.setItem('jade-caravan.recent-campaigns', JSON.stringify(['campaign-gamma', 'campaign-beta']));

    const store = useCampaignStore();

    store.restoreCampaignSession();

    expect(store.campaignId).toBe('campaign-gamma');
    expect(store.recentCampaignIds).toEqual(['campaign-gamma', 'campaign-beta']);
  });
});
