export interface CampaignDashboardSummary {
  campaignId: string;
  campaignName: string;
  status: 'draft' | 'planned' | 'resolving' | 'closed';
  cartCount: number;
  travellerCount: number;
  notes: string;
}
