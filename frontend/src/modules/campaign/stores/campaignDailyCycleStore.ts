import { defineStore } from 'pinia';
import type {
  CampaignDayCloseRequest,
  CampaignDayCreateRequest,
  CampaignDayPlanRequest,
  CampaignDayPreview,
  CampaignDayResolveRequest,
  CampaignDailyCycleState,
} from '../types';
import {
  closeCampaignDay,
  createCampaignDay,
  getCampaignDailyCycleState,
  planCampaignDay,
  previewCampaignDay,
  reopenCampaignDay,
  resolveCampaignDay,
} from '../api';

interface CampaignDailyCycleStateStore {
  campaignId: string | null;
  cycleState: CampaignDailyCycleState | null;
  preview: CampaignDayPreview | null;
  status: 'idle' | 'loading' | 'ready' | 'saving' | 'error';
  errorMessage: string | null;
}

function toErrorMessage(error: unknown): string {
  if (error instanceof Error && error.message) {
    return error.message;
  }

  return 'No se ha podido cargar o guardar el ciclo diario.';
}

export const useCampaignDailyCycleStore = defineStore('campaignDailyCycle', {
  state: (): CampaignDailyCycleStateStore => ({
    campaignId: null,
    cycleState: null,
    preview: null,
    status: 'idle',
    errorMessage: null,
  }),
  getters: {
    activeDay: (state) => {
      const activeDayId = state.cycleState?.activeDayId;
      if (!state.cycleState || !activeDayId) {
        return null;
      }

      return state.cycleState.caravan.campaignDays.find((day) => day.id === activeDayId) ?? null;
    },
    summary: (state) => state.cycleState?.lastSummary ?? null,
    travellers: (state) => state.cycleState?.caravan.travellers ?? [],
    carts: (state) => state.cycleState?.caravan.carts ?? [],
    beasts: (state) => state.cycleState?.caravan.beasts ?? [],
    inventoryLots: (state) => state.cycleState?.caravan.inventoryLots ?? [],
    operations: (state) => state.cycleState?.operations ?? [],
    currentDiscontent: (state) => state.cycleState?.caravan.currentDiscontent ?? '0',
  },
  actions: {
    async loadCampaignDailyCycle(campaignId: string) {
      this.campaignId = campaignId;
      this.status = 'loading';
      this.errorMessage = null;

      try {
        this.cycleState = await getCampaignDailyCycleState(campaignId);
        this.preview = await previewCampaignDay(campaignId, this.cycleState.activeDayId);
        this.status = 'ready';
      } catch (error) {
        this.cycleState = null;
        this.preview = null;
        this.status = 'error';
        this.errorMessage = toErrorMessage(error);
        throw error;
      }
    },
    async createDay(campaignId: string, request: CampaignDayCreateRequest) {
      this.status = 'saving';
      this.errorMessage = null;

      try {
        this.cycleState = await createCampaignDay(campaignId, request);
        this.preview = await previewCampaignDay(campaignId, this.cycleState.activeDayId);
        this.status = 'ready';
      } catch (error) {
        this.status = 'error';
        this.errorMessage = toErrorMessage(error);
        throw error;
      }
    },
    async planDay(campaignId: string, request: CampaignDayPlanRequest) {
      if (!this.cycleState) {
        throw new Error('No hay ciclo diario cargado.');
      }

      this.status = 'saving';
      this.errorMessage = null;

      try {
        this.cycleState = await planCampaignDay(campaignId, this.cycleState.activeDayId, request);
        this.preview = await previewCampaignDay(campaignId, this.cycleState.activeDayId);
        this.status = 'ready';
      } catch (error) {
        this.status = 'error';
        this.errorMessage = toErrorMessage(error);
        throw error;
      }
    },
    async resolveDay(campaignId: string, request: CampaignDayResolveRequest) {
      if (!this.cycleState) {
        throw new Error('No hay ciclo diario cargado.');
      }

      this.status = 'saving';
      this.errorMessage = null;

      try {
        this.cycleState = await resolveCampaignDay(campaignId, this.cycleState.activeDayId, request);
        this.preview = await previewCampaignDay(campaignId, this.cycleState.activeDayId);
        this.status = 'ready';
      } catch (error) {
        this.status = 'error';
        this.errorMessage = toErrorMessage(error);
        throw error;
      }
    },
    async closeDay(campaignId: string, request?: CampaignDayCloseRequest) {
      if (!this.cycleState) {
        throw new Error('No hay ciclo diario cargado.');
      }

      this.status = 'saving';
      this.errorMessage = null;

      try {
        const summary = await closeCampaignDay(campaignId, this.cycleState.activeDayId, request);
        this.cycleState = await getCampaignDailyCycleState(campaignId);
        this.preview = await previewCampaignDay(campaignId, this.cycleState.activeDayId);
        if (this.cycleState) {
          this.cycleState = {
            ...this.cycleState,
            lastSummary: summary ?? this.cycleState.lastSummary,
          };
        }
        this.status = 'ready';
      } catch (error) {
        this.status = 'error';
        this.errorMessage = toErrorMessage(error);
        throw error;
      }
    },
    async reopenDay(campaignId: string, request: { actor?: string; source?: string; reason?: string }) {
      if (!this.cycleState) {
        throw new Error('No hay ciclo diario cargado.');
      }

      this.status = 'saving';
      this.errorMessage = null;

      try {
        this.cycleState = await reopenCampaignDay(campaignId, this.cycleState.activeDayId, request);
        this.preview = await previewCampaignDay(campaignId, this.cycleState.activeDayId);
        this.status = 'ready';
      } catch (error) {
        this.status = 'error';
        this.errorMessage = toErrorMessage(error);
        throw error;
      }
    },
  },
});
