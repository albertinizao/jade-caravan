<template>
  <AppShell>
    <section class="page detail-page">
      <header class="hero-card">
        <p class="eyebrow">Campaña {{ campaignId }}</p>
        <h2>Bestias</h2>
        <p class="muted">
          Bestias disponibles, en tiro, fatigadas y con su tipo de catálogo asociado.
        </p>
      </header>

      <p v-if="loadingState === 'loading'" class="banner banner--info" role="status">
        Cargando bestias...
      </p>
      <p v-else-if="loadingState === 'error'" class="banner banner--danger" role="alert">
        {{ errorMessage }}
      </p>

      <section v-if="cycleState" class="panel">
        <div class="panel__header">
          <div>
            <h3>Resumen de tiro</h3>
            <p class="muted">
              {{ cycleState.caravan.beasts.length }} bestias · {{ towingBeasts.length }} en tiro ·
              {{ fatiguedBeasts.length }} fatigadas
            </p>
          </div>
          <RouterLink class="button button--secondary" :to="dashboardRoute">Volver al tablero</RouterLink>
        </div>

        <div class="table-wrapper">
          <table>
            <thead>
              <tr>
                <th>Bestia</th>
                <th>Tipo</th>
                <th>FUE</th>
                <th>Tamaño</th>
                <th>Velocidad</th>
                <th>Temperatura</th>
                <th>PG</th>
                <th>Estado</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="beast in orderedBeasts" :key="beast.id">
                <td><strong>{{ beast.name }}</strong></td>
                <td>{{ beast.beastType?.name ?? '—' }}</td>
                <td>{{ beast.beastType?.strength ?? '—' }}</td>
                <td>{{ beast.beastType?.size ?? '—' }}</td>
                <td>{{ beast.beastType?.speedFeet ?? '—' }} ft</td>
                <td>{{ beast.beastType?.temperatureAdaptation ?? '—' }}</td>
                <td>{{ beast.currentHitPoints ?? '—' }}</td>
                <td>{{ beastState(beast) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section v-if="cycleState" class="detail-grid">
        <article v-for="beast in orderedBeasts" :key="`${beast.id}-detail`" class="panel">
          <div class="panel__header">
            <div>
              <h3>{{ beast.name }}</h3>
              <p class="muted">{{ beast.beastType?.name ?? 'Tipo no catalogado' }}</p>
            </div>
          </div>

          <div class="facts-grid">
            <div>
              <dt>Estado</dt>
              <dd>{{ beastState(beast) }}</dd>
            </div>
            <div>
              <dt>Fuerza</dt>
              <dd>{{ beast.beastType?.strength ?? '—' }}</dd>
            </div>
            <div>
              <dt>Tamaño</dt>
              <dd>{{ beast.beastType?.size ?? '—' }}</dd>
            </div>
            <div>
              <dt>Velocidad</dt>
              <dd>{{ beast.beastType?.speedFeet ?? '—' }} ft</dd>
            </div>
            <div>
              <dt>Ajuste térmico</dt>
              <dd>{{ beast.beastType?.temperatureAdaptation ?? '—' }}</dd>
            </div>
            <div>
              <dt>Notas</dt>
              <dd>{{ beast.beastType?.adaptationNotes ?? beast.notes ?? '—' }}</dd>
            </div>
          </div>

          <div class="split-grid">
            <section>
              <h4>Tiro</h4>
              <ul class="compact-list">
                <li v-if="beast.towingAssignment">
                  <strong>{{ cartName(beast.towingAssignment.cartId) }}</strong>
                  <span class="muted">{{ beast.towingAssignment.consecutiveTowingDays }} días seguidos</span>
                </li>
                <li v-else class="muted">Sin tiro asignado.</li>
              </ul>
            </section>

            <section>
              <h4>Alertas</h4>
              <ul class="compact-list">
                <li v-if="beast.fatigued" class="muted">Bestia fatigada; revisa el reparto de tiro.</li>
                <li v-else class="muted">Sin alertas específicas visibles en la API actual.</li>
              </ul>
            </section>
          </div>
        </article>
      </section>
    </section>
  </AppShell>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue';
import { RouterLink } from 'vue-router';
import AppShell from '@/layouts/AppShell.vue';
import { useCampaignDailyCycleStore, useCampaignStore } from '../stores';
import type { BeastSummary } from '../types';

interface CampaignBeastsViewProps {
  campaignId?: string;
}

const props = defineProps<CampaignBeastsViewProps>();
const campaignId = computed(() => props.campaignId ?? 'demo');

const campaignStore = useCampaignStore();
const cycleStore = useCampaignDailyCycleStore();

const loadingState = computed(() => {
  if (cycleStore.status === 'loading') {
    return 'loading';
  }

  if (cycleStore.status === 'error') {
    return 'error';
  }

  return 'ready';
});

const errorMessage = computed(() => cycleStore.errorMessage ?? 'No se han podido cargar las bestias.');
const cycleState = computed(() => cycleStore.cycleState);
const dashboardRoute = computed(() => ({ name: 'campaign-dashboard', params: { campaignId: campaignId.value } }));
const orderedBeasts = computed(() => [...(cycleState.value?.caravan.beasts ?? [])].sort((left, right) => left.name.localeCompare(right.name)));
const towingBeasts = computed(() => orderedBeasts.value.filter((beast) => beast.activeAsTowing));
const fatiguedBeasts = computed(() => orderedBeasts.value.filter((beast) => beast.fatigued));

function beastState(beast: BeastSummary) {
  const states = [];
  if (beast.activeAsTowing) {
    states.push('en tiro');
  }
  if (beast.fatigued) {
    states.push('fatigada');
  }
  if (states.length === 0) {
    states.push('libre');
  }

  return states.join(' · ');
}

function cartName(cartId: string) {
  return cycleState.value?.caravan.carts.find((cart) => cart.id === cartId)?.name ?? cartId;
}

watch(
  campaignId,
  async (nextCampaignId) => {
    campaignStore.selectCampaign(nextCampaignId);

    try {
      await cycleStore.loadCampaignDailyCycle(nextCampaignId);
    } catch {
      // Store handles the error state.
    }
  },
  { immediate: true },
);
</script>
