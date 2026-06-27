<template>
  <AppShell>
    <section class="page detail-page">
      <header class="hero-card">
        <p class="eyebrow">Campaña {{ campaignId }}</p>
        <h2>Inventario</h2>
        <p class="muted">
          Lotes, capacidad, provisiones restantes y vínculos con carros o asentamientos.
        </p>
      </header>

      <p v-if="loadingState === 'loading'" class="banner banner--info" role="status">
        Cargando inventario...
      </p>
      <p v-else-if="loadingState === 'error'" class="banner banner--danger" role="alert">
        {{ errorMessage }}
      </p>

      <section v-if="cycleState" class="panel">
        <div class="panel__header">
          <div>
            <h3>Resumen de inventario</h3>
            <p class="muted">
              {{ cycleState.caravan.inventoryLots.length }} lotes ·
              {{ cargoOccupancy }} de carga ocupada
            </p>
          </div>
          <RouterLink class="button button--secondary" :to="dashboardRoute">Volver al tablero</RouterLink>
        </div>

        <div class="table-wrapper">
          <table>
            <thead>
              <tr>
                <th>Lote</th>
                <th>Tipo de carga</th>
                <th>Cantidad</th>
                <th>Capacidad unitaria</th>
                <th>Valor unitario</th>
                <th>Carro</th>
                <th>Provisiones</th>
                <th>Desgaste</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="lot in orderedInventoryLots" :key="lot.id">
                <td><strong>{{ lot.id }}</strong></td>
                <td>{{ lot.cargoTypeId }}</td>
                <td>{{ lot.quantity }}</td>
                <td>{{ lot.unitCapacity ?? '—' }}</td>
                <td>{{ lot.unitValueCp ?? '—' }} cp</td>
                <td>{{ cartName(lot.cartId) }}</td>
                <td>{{ lot.remainingProvisions ?? '—' }}</td>
                <td>{{ lot.perishableDecayProgress ?? '—' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section v-if="cycleState" class="detail-grid">
        <article v-for="lot in orderedInventoryLots" :key="`${lot.id}-detail`" class="panel">
          <div class="panel__header">
            <div>
              <h3>{{ lot.cargoTypeId }}</h3>
              <p class="muted">{{ lot.quantity }} unidades · valor {{ lot.unitValueCp ?? '—' }} cp</p>
            </div>
          </div>

          <div class="facts-grid">
            <div>
              <dt>Capacidad total</dt>
              <dd>{{ lotCapacity(lot) }}</dd>
            </div>
            <div>
              <dt>Carro</dt>
              <dd>{{ cartName(lot.cartId) }}</dd>
            </div>
            <div>
              <dt>Asentamiento origen</dt>
              <dd>{{ lot.originSettlementId ?? '—' }}</dd>
            </div>
            <div>
              <dt>Provisiones restantes</dt>
              <dd>{{ lot.remainingProvisions ?? '—' }}</dd>
            </div>
            <div>
              <dt>Desgaste</dt>
              <dd>{{ lot.perishableDecayProgress ?? '—' }}</dd>
            </div>
            <div>
              <dt>Metadatos</dt>
              <dd>{{ metadataSummary(lot.metadata) }}</dd>
            </div>
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
import type { InventoryLotSummary } from '../types';

interface CampaignInventoryViewProps {
  campaignId?: string;
}

const props = defineProps<CampaignInventoryViewProps>();
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

const errorMessage = computed(() => cycleStore.errorMessage ?? 'No se ha podido cargar el inventario.');
const cycleState = computed(() => cycleStore.cycleState);
const dashboardRoute = computed(() => ({ name: 'campaign-dashboard', params: { campaignId: campaignId.value } }));
const orderedInventoryLots = computed(() => [...(cycleState.value?.caravan.inventoryLots ?? [])].sort((left, right) => left.cargoTypeId.localeCompare(right.cargoTypeId)));
const cargoOccupancy = computed(() => cycleState.value?.caravan.inventoryLots.reduce((sum, lot) => sum + Number(lot.quantity) * Number(lot.unitCapacity ?? 0), 0).toLocaleString('es-ES') ?? '—');

function cartName(cartId: string | null | undefined) {
  if (!cartId) {
    return 'Sin carro';
  }

  return cycleState.value?.caravan.carts.find((cart) => cart.id === cartId)?.name ?? cartId;
}

function lotCapacity(lot: InventoryLotSummary) {
  if (!lot.unitCapacity) {
    return '—';
  }

  const total = Number(lot.quantity) * Number(lot.unitCapacity);
  return Number.isFinite(total) ? total.toLocaleString('es-ES') : '—';
}

function metadataSummary(metadata: Record<string, string> | undefined) {
  if (!metadata || Object.keys(metadata).length === 0) {
    return '—';
  }

  return Object.entries(metadata)
    .map(([key, value]) => `${key}: ${value}`)
    .join(' · ');
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
