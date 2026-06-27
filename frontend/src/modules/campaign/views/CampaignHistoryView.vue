<template>
  <AppShell>
    <section class="page detail-page">
      <header class="hero-card">
        <p class="eyebrow">Campaña {{ campaignId }}</p>
        <h2>Historial y auditoría</h2>
        <p class="muted">
          Consulta el rastro de días, comprobaciones, eventos, transacciones y decisiones
          registradas.
        </p>
      </header>

      <p v-if="loadingState === 'loading'" class="banner banner--info" role="status">
        Cargando historial...
      </p>
      <p v-else-if="loadingState === 'error'" class="banner banner--danger" role="alert">
        {{ errorMessage }}
      </p>

      <section v-if="cycleState" class="panel">
        <div class="panel__header">
          <div>
            <h3>Días de campaña</h3>
            <p class="muted">
              {{ cycleState.caravan.campaignDays.length }} días · día actual {{ cycleState.caravan.currentDayNumber }}
            </p>
          </div>
          <RouterLink class="button button--secondary" :to="dashboardRoute">Volver al tablero</RouterLink>
        </div>

        <div class="detail-grid">
          <article v-for="day in orderedDays" :key="day.id" class="summary-card">
            <p class="summary-card__label">Día {{ day.dayNumber }}</p>
            <strong class="summary-card__value">{{ day.activityType }}</strong>
            <p class="summary-card__meta">
              {{ day.status }} · {{ day.location }} · {{ day.plannedDistanceMiles ?? '—' }} mi planificadas
            </p>
            <ul class="compact-list">
              <li>
                <strong>Checks</strong>
                <span class="muted">{{ day.checkResolutions.length }}</span>
              </li>
              <li>
                <strong>Eventos</strong>
                <span class="muted">{{ day.caravanEvents.length }}</span>
              </li>
              <li>
                <strong>Comercio</strong>
                <span class="muted">{{ day.tradeTransactions.length }}</span>
              </li>
            </ul>
          </article>
        </div>
      </section>

      <section v-if="cycleState" class="split-grid">
        <article class="panel">
          <h3>Comprobaciones</h3>
          <ul class="compact-list">
            <li v-for="resolution in cycleState.caravan.checkResolutions" :key="resolution.id">
              <strong>{{ resolution.checkType }}</strong>
              <span class="muted">
                CD {{ resolution.dc }} · total {{ resolution.total ?? '—' }} · {{ resolution.outcome }}
              </span>
            </li>
            <li v-if="cycleState.caravan.checkResolutions.length === 0" class="muted">Sin comprobaciones registradas.</li>
          </ul>
        </article>

        <article class="panel">
          <h3>Eventos de campaña</h3>
          <ul class="compact-list">
            <li v-for="event in cycleState.caravan.caravanEvents" :key="event.id">
              <strong>{{ event.severity }}</strong>
              <span class="muted">{{ event.source }} · {{ event.narrativeSummary }}</span>
            </li>
            <li v-if="cycleState.caravan.caravanEvents.length === 0" class="muted">Sin eventos registrados.</li>
          </ul>
        </article>
      </section>

      <section v-if="auditTrail.length > 0" class="panel">
        <div class="panel__header">
          <div>
            <h3>Auditoría de reglas</h3>
            <p class="muted">Resúmenes de decisiones con motivo y resolución actual.</p>
          </div>
          <RouterLink class="button button--secondary" :to="rulesRoute">Abrir reglas</RouterLink>
        </div>

        <ul class="compact-list">
          <li v-for="entry in auditTrail" :key="`${entry.decisionKey}-${entry.resolvedAt}`">
            <strong>{{ entry.decisionKey }} · {{ entry.decisionTitle }}</strong>
            <span class="muted">
              {{ entry.currentResolution }} · {{ entry.reason }} · {{ entry.actor }} · {{ entry.resolvedAt }}
            </span>
          </li>
        </ul>
      </section>
    </section>
  </AppShell>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue';
import { RouterLink } from 'vue-router';
import AppShell from '@/layouts/AppShell.vue';
import { useCampaignDailyCycleStore, useCampaignRulesStore, useCampaignStore } from '../stores';

interface CampaignHistoryViewProps {
  campaignId?: string;
}

const props = defineProps<CampaignHistoryViewProps>();
const campaignId = computed(() => props.campaignId ?? 'demo');

const campaignStore = useCampaignStore();
const cycleStore = useCampaignDailyCycleStore();
const rulesStore = useCampaignRulesStore();

const loadingState = computed(() => {
  if (cycleStore.status === 'loading' || rulesStore.status === 'loading') {
    return 'loading';
  }

  if (cycleStore.status === 'error' || rulesStore.status === 'error') {
    return 'error';
  }

  return 'ready';
});

const errorMessage = computed(() => cycleStore.errorMessage ?? rulesStore.errorMessage ?? 'No se ha podido cargar el historial.');
const cycleState = computed(() => cycleStore.cycleState);
const orderedDays = computed(() => [...(cycleState.value?.caravan.campaignDays ?? [])].sort((left, right) => left.dayNumber - right.dayNumber));
const auditTrail = computed(() => rulesStore.auditTrail);
const dashboardRoute = computed(() => ({ name: 'campaign-dashboard', params: { campaignId: campaignId.value } }));
const rulesRoute = computed(() => ({ name: 'campaign-rules', params: { campaignId: campaignId.value } }));

watch(
  campaignId,
  async (nextCampaignId) => {
    campaignStore.selectCampaign(nextCampaignId);

    try {
      await Promise.all([cycleStore.loadCampaignDailyCycle(nextCampaignId), rulesStore.loadCampaignRules(nextCampaignId)]);
    } catch {
      // Error state already captured by the stores.
    }
  },
  { immediate: true },
);
</script>
