import { defineStore } from 'pinia';

interface CampaignState {
  campaignId: string;
  status: 'idle' | 'loading' | 'ready';
  recentCampaignIds: string[];
}

const RECENT_CAMPAIGNS_STORAGE_KEY = 'jade-caravan.recent-campaigns';
const CURRENT_CAMPAIGN_STORAGE_KEY = 'jade-caravan.current-campaign';

function readStoredCampaignIds(): string[] {
  if (typeof window === 'undefined') {
    return [];
  }

  try {
    const rawValue = window.localStorage.getItem(RECENT_CAMPAIGNS_STORAGE_KEY);
    if (!rawValue) {
      return [];
    }

    const parsed = JSON.parse(rawValue) as unknown;
    return Array.isArray(parsed) ? parsed.filter((value): value is string => typeof value === 'string') : [];
  } catch {
    return [];
  }
}

function persistCampaignIds(campaignIds: string[]) {
  if (typeof window === 'undefined') {
    return;
  }

  window.localStorage.setItem(RECENT_CAMPAIGNS_STORAGE_KEY, JSON.stringify(campaignIds.slice(0, 8)));
}

export const useCampaignStore = defineStore('campaign', {
  state: (): CampaignState => ({
    campaignId: 'demo',
    status: 'idle',
    recentCampaignIds: [],
  }),
  actions: {
    selectCampaign(campaignId: string) {
      const normalizedCampaignId = campaignId.trim() || 'demo';
      this.campaignId = normalizedCampaignId;
      this.rememberCampaign(normalizedCampaignId);
      if (typeof window !== 'undefined') {
        window.localStorage.setItem(CURRENT_CAMPAIGN_STORAGE_KEY, normalizedCampaignId);
      }
    },
    markReady() {
      this.status = 'ready';
    },
    rememberCampaign(campaignId: string) {
      const normalizedCampaignId = campaignId.trim();
      if (!normalizedCampaignId) {
        return;
      }

      const nextCampaignIds = [
        normalizedCampaignId,
        ...this.recentCampaignIds.filter((recentCampaignId) => recentCampaignId !== normalizedCampaignId),
      ].slice(0, 8);

      this.recentCampaignIds = nextCampaignIds;
      persistCampaignIds(nextCampaignIds);
    },
    restoreCampaignSession() {
      if (typeof window === 'undefined') {
        return;
      }

      const storedCampaignId = window.localStorage.getItem(CURRENT_CAMPAIGN_STORAGE_KEY);
      if (storedCampaignId) {
        this.campaignId = storedCampaignId;
      }

      const storedRecentCampaignIds = readStoredCampaignIds();
      if (storedRecentCampaignIds.length > 0) {
        this.recentCampaignIds = storedRecentCampaignIds;
      }
    },
  },
});
