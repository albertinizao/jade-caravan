import { httpClient } from '@/api/httpClient';
import type {
  CampaignDayCloseRequest,
  CampaignDayCreateRequest,
  CampaignDayPlanRequest,
  CampaignDayPreview,
  CampaignDayReopenRequest,
  CampaignDayResolveRequest,
  CampaignDailyCycleState,
} from '../types';

export function getCampaignDailyCycleState(campaignId: string): Promise<CampaignDailyCycleState> {
  return httpClient.get<CampaignDailyCycleState>(`/campaigns/${campaignId}/days`);
}

export function createCampaignDay(campaignId: string, request: CampaignDayCreateRequest): Promise<CampaignDailyCycleState> {
  return httpClient.post<CampaignDailyCycleState>(`/campaigns/${campaignId}/days`, request);
}

export function planCampaignDay(
  campaignId: string,
  dayId: string,
  request: CampaignDayPlanRequest,
): Promise<CampaignDailyCycleState> {
  return httpClient.post<CampaignDailyCycleState>(`/campaigns/${campaignId}/days/${dayId}/plan`, request);
}

export function previewCampaignDay(campaignId: string, dayId: string): Promise<CampaignDayPreview> {
  return httpClient.get<CampaignDayPreview>(`/campaigns/${campaignId}/days/${dayId}/preview`);
}

export function resolveCampaignDay(
  campaignId: string,
  dayId: string,
  request: CampaignDayResolveRequest,
): Promise<CampaignDailyCycleState> {
  return httpClient.post<CampaignDailyCycleState>(`/campaigns/${campaignId}/days/${dayId}/resolve`, request);
}

export function closeCampaignDay(
  campaignId: string,
  dayId: string,
  request?: CampaignDayCloseRequest,
): Promise<CampaignDailyCycleState['lastSummary']> {
  return httpClient.post<CampaignDailyCycleState['lastSummary']>(`/campaigns/${campaignId}/days/${dayId}/close`, request);
}

export function reopenCampaignDay(
  campaignId: string,
  dayId: string,
  request: CampaignDayReopenRequest,
): Promise<CampaignDailyCycleState> {
  return httpClient.post<CampaignDailyCycleState>(`/campaigns/${campaignId}/days/${dayId}/reopen`, request);
}
