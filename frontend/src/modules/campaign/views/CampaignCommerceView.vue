<template>
  <AppShell>
    <section class="page detail-page">
      <header class="hero-card">
        <p class="eyebrow">Campaña {{ campaignId }}</p>
        <h2>Comercio</h2>
        <p class="muted">
          Transacciones comerciales, ingresos y operaciones de libro mayor registradas por día.
        </p>
      </header>

      <p v-if="loadingState === 'loading'" class="banner banner--info" role="status">
        Cargando comercio...
      </p>
      <p v-else-if="loadingState === 'error'" class="banner banner--danger" role="alert">
        {{ errorMessage }}
      </p>

      <section v-if="cycleState" class="panel">
        <div class="panel__header">
          <div>
            <h3>Actividad económica</h3>
            <p class="muted">
              {{ tradeTransactions.length }} transacciones comerciales · {{ ledgerEntries.length }} entradas de libro mayor
            </p>
          </div>
          <RouterLink class="button button--secondary" :to="dashboardRoute">Volver al tablero</RouterLink>
        </div>

        <div class="split-grid">
          <section>
            <h4>Transacciones</h4>
            <ul class="compact-list">
              <li v-for="transaction in orderedTransactions" :key="transaction.id">
                <strong>{{ transaction.transactionType }}</strong>
                <span class="muted">
                  {{ transaction.cargoTypeId }} · {{ transaction.quantity }} · {{ transaction.totalValueCp }} cp
                </span>
              </li>
              <li v-if="orderedTransactions.length === 0" class="muted">Sin transacciones registradas.</li>
            </ul>
          </section>

          <section>
            <h4>Libro mayor</h4>
            <ul class="compact-list">
              <li v-for="entry in orderedLedgerEntries" :key="entry.id">
                <strong>{{ entry.operationType }}</strong>
                <span class="muted">
                  {{ entry.resourceType }} · {{ entry.delta }} · {{ entry.reason }}
                </span>
              </li>
              <li v-if="orderedLedgerEntries.length === 0" class="muted">Sin entradas en el libro mayor.</li>
            </ul>
          </section>
        </div>
      </section>
    </section>
  </AppShell>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue';
import { RouterLink } from 'vue-router';
import AppShell from '@/layouts/AppShell.vue';
import { useCampaignDailyCycleStore, useCampaignStore } from '../stores';

interface CampaignCommerceViewProps {
  campaignId?: string;
}

const props = defineProps<CampaignCommerceViewProps>();
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

const errorMessage = computed(() => cycleStore.errorMessage ?? 'No se ha podido cargar el comercio.');
const cycleState = computed(() => cycleStore.cycleState);
const dashboardRoute = computed(() => ({ name: 'campaign-dashboard', params: { campaignId: campaignId.value } }));
const orderedTransactions = computed(() => [...(cycleState.value?.caravan.tradeTransactions ?? [])].sort((left, right) => left.cargoTypeId.localeCompare(right.cargoTypeId)));
const orderedLedgerEntries = computed(() => [...(cycleState.value?.caravan.ledgerEntries ?? [])].sort((left, right) => left.createdAt.localeCompare(right.createdAt)));

const tradeTransactions = computed(() => cycleState.value?.caravan.tradeTransactions ?? []);
const ledgerEntries = computed(() => cycleState.value?.caravan.ledgerEntries ?? []);

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
