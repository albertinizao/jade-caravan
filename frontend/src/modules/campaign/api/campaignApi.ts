import { httpClient } from '@/api/httpClient';
import type { CampaignDashboardSummary } from '../types';

export function getCampaignDashboard(campaignId: string): Promise<CampaignDashboardSummary> {
  return httpClient.get<CampaignDashboardSummary>(`/campaigns/${campaignId}/dashboard`);
}
