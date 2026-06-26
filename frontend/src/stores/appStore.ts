import { defineStore } from 'pinia';

interface AppState {
  selectedCampaignId: string;
}

export const useAppStore = defineStore('app', {
  state: (): AppState => ({
    selectedCampaignId: 'demo',
  }),
  actions: {
    selectCampaign(campaignId: string) {
      this.selectedCampaignId = campaignId;
    },
  },
});
