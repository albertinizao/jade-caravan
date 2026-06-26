import { defineStore } from 'pinia';

interface CampaignState {
  campaignId: string;
  status: 'idle' | 'loading' | 'ready';
}

export const useCampaignStore = defineStore('campaign', {
  state: (): CampaignState => ({
    campaignId: 'demo',
    status: 'idle',
  }),
  actions: {
    selectCampaign(campaignId: string) {
      this.campaignId = campaignId;
    },
    markReady() {
      this.status = 'ready';
    },
  },
});
