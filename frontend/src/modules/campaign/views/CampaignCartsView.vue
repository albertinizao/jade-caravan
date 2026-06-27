<template>
  <AppShell>
    <section class="page detail-page">
      <header class="hero-card">
        <p class="eyebrow">Campaña {{ campaignId }}</p>
        <h2>Carros</h2>
        <p class="muted">
          Estado de cada carro con tipo, puntos de golpe, ocupación, carga, tiro y mejoras
          activas.
        </p>
      </header>

      <p v-if="loadingState === 'loading'" class="banner banner--info" role="status">
        Cargando carros...
      </p>
      <p v-else-if="loadingState === 'error'" class="banner banner--danger" role="alert">
        {{ errorMessage }}
      </p>

      <section v-if="cycleState" class="panel">
        <div class="panel__header">
          <div>
            <h3>Resumen de carros</h3>
            <p class="muted">
              {{ operativeCarts.length }} operativos de {{ cycleState.caravan.carts.length }} totales ·
              {{ cargoOccupancy }} de carga frente a {{ cargoCapacity }} de capacidad
            </p>
          </div>
          <RouterLink class="button button--secondary" :to="dashboardRoute">Volver al tablero</RouterLink>
        </div>

        <div class="table-wrapper">
          <table>
            <thead>
              <tr>
                <th>Carro</th>
                <th>Tipo</th>
                <th>PG</th>
                <th>HA</th>
                <th>Tiro</th>
                <th>Pasajeros</th>
                <th>Carga</th>
                <th>Mejoras</th>
                <th>Alertas</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="cart in orderedCarts" :key="cart.id">
                <td>
                  <strong>{{ cart.name }}</strong>
                  <div class="muted">{{ cartNotes(cart) }}</div>
                </td>
                <td>
                  <strong>{{ cart.cartType?.name ?? '—' }}</strong>
                  <div class="muted">{{ cart.cartType?.category ?? '—' }}</div>
                </td>
                <td>{{ cart.currentHitPoints }}</td>
                <td>{{ cart.cartType?.hardness ?? '—' }}</td>
                <td>{{ cart.towingAssignments?.length ?? 0 }}</td>
                <td>{{ cart.passengerAssignments?.length ?? 0 }}</td>
                <td>{{ cart.cargoAllocations?.length ?? 0 }}</td>
                <td>{{ cart.upgradeInstances?.filter((upgrade) => upgrade.active).length ?? 0 }}</td>
                <td>
                  <span :class="cart.destroyed ? 'badge badge--danger' : 'badge badge--neutral'">
                    {{ cart.destroyed ? 'Destruido' : 'Operativo' }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section v-if="cycleState" class="detail-grid">
        <article v-for="cart in orderedCarts" :key="`${cart.id}-detail`" class="panel">
          <div class="panel__header">
            <div>
              <h3>{{ cart.name }}</h3>
              <p class="muted">
                {{ cart.cartType?.name ?? 'Tipo no catalogado' }} · {{ cart.currentHitPoints }} PG
              </p>
            </div>
          </div>

          <div class="facts-grid">
            <div>
              <dt>Capacidad de viajeros</dt>
              <dd>{{ cart.cartType?.passengerCapacity ?? '—' }}</dd>
            </div>
            <div>
              <dt>Capacidad de carga</dt>
              <dd>{{ cart.cartType?.cargoCapacity ?? '—' }}</dd>
            </div>
            <div>
              <dt>Consumo</dt>
              <dd>{{ cart.cartType?.consumption ?? '—' }}</dd>
            </div>
            <div>
              <dt>Requisito de propulsión</dt>
              <dd>{{ cart.cartType?.propulsionRequirement ?? '—' }}</dd>
            </div>
            <div>
              <dt>Tiro máximo</dt>
              <dd>{{ cart.cartType?.towingCreatureLimit ?? '—' }}</dd>
            </div>
            <div>
              <dt>Notas</dt>
              <dd>{{ cart.cartType?.note ?? cart.notes ?? '—' }}</dd>
            </div>
          </div>

          <div class="split-grid">
            <section>
              <h4>Mejoras activas</h4>
              <ul class="compact-list">
                <li v-for="upgrade in cart.upgradeInstances?.filter((instance) => instance.active) ?? []" :key="upgrade.upgrade.key">
                  <strong>{{ upgrade.upgrade.name }}</strong>
                  <span class="muted">{{ upgrade.upgrade.effect }}</span>
                </li>
                <li v-if="(cart.upgradeInstances?.filter((instance) => instance.active).length ?? 0) === 0" class="muted">
                  Sin mejoras activas.
                </li>
              </ul>
            </section>

            <section>
              <h4>Pasajeros y carga</h4>
              <ul class="compact-list">
                <li v-for="assignment in cart.passengerAssignments ?? []" :key="`${cart.id}-${assignment.travellerId}`">
                  <strong>{{ travellerName(assignment.travellerId) }}</strong>
                  <span class="muted">{{ assignment.occupancyUnits }} unidades de ocupación</span>
                </li>
                <li v-for="allocation in cart.cargoAllocations ?? []" :key="`${cart.id}-${allocation.inventoryLotId}`">
                  <strong>{{ inventoryName(allocation.inventoryLotId) }}</strong>
                  <span class="muted">{{ allocation.quantity }} asignadas</span>
                </li>
              </ul>
            </section>
          </div>

          <div class="split-grid">
            <section>
              <h4>Tiro</h4>
              <ul class="compact-list">
                <li v-for="assignment in cart.towingAssignments ?? []" :key="`${assignment.beastId}-${assignment.cartId}`">
                  <strong>{{ beastName(assignment.beastId) }}</strong>
                  <span class="muted">{{ assignment.consecutiveTowingDays }} días seguidos</span>
                </li>
                <li v-if="(cart.towingAssignments?.length ?? 0) === 0" class="muted">Sin tiro asignado.</li>
              </ul>
            </section>

            <section>
              <h4>Alertas del carro</h4>
              <ul class="compact-list">
                <li v-if="cart.destroyed" class="muted">Carro destruido: no puede salir sin reparación.</li>
                <li v-else-if="cart.currentHitPoints <= 0" class="muted">Carro sin puntos de golpe operativos.</li>
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
import { useCatalogStore } from '@/stores';
import { useCampaignDailyCycleStore, useCampaignStore } from '../stores';
import type { CartSummary } from '../types';
import { formatDecimal, formatText } from '../utils/presentation';

interface CampaignCartsViewProps {
  campaignId?: string;
}

const props = defineProps<CampaignCartsViewProps>();
const campaignId = computed(() => props.campaignId ?? 'demo');

const campaignStore = useCampaignStore();
const catalogStore = useCatalogStore();
const cycleStore = useCampaignDailyCycleStore();

const loadingState = computed(() => {
  if (catalogStore.status === 'loading' || cycleStore.status === 'loading') {
    return 'loading';
  }

  if (catalogStore.status === 'error' || cycleStore.status === 'error') {
    return 'error';
  }

  return 'ready';
});

const errorMessage = computed(() => cycleStore.errorMessage ?? catalogStore.errorMessage ?? 'No se han podido cargar los carros.');

const cycleState = computed(() => cycleStore.cycleState);
const dashboardRoute = computed(() => ({ name: 'campaign-dashboard', params: { campaignId: campaignId.value } }));
const orderedCarts = computed(() => [...(cycleState.value?.caravan.carts ?? [])].sort((left, right) => {
  if (left.destroyed !== right.destroyed) {
    return Number(left.destroyed) - Number(right.destroyed);
  }

  return left.name.localeCompare(right.name);
}));
const operativeCarts = computed(() => orderedCarts.value.filter((cart) => !cart.destroyed && cart.currentHitPoints > 0));
const cargoCapacity = computed(() => cycleStore.preview?.calculationSummary.cargoCapacity ?? '—');
const cargoOccupancy = computed(() => cycleStore.preview?.calculationSummary.cargoOccupancy ?? '—');

function travellerName(travellerId: string) {
  return cycleState.value?.caravan.travellers.find((traveller) => traveller.id === travellerId)?.name ?? travellerId;
}

function beastName(beastId: string) {
  return cycleState.value?.caravan.beasts.find((beast) => beast.id === beastId)?.name ?? beastId;
}

function inventoryName(inventoryLotId: string) {
  const lot = cycleState.value?.caravan.inventoryLots.find((inventoryLot) => inventoryLot.id === inventoryLotId);
  return lot ? `${lot.cargoTypeId} · ${formatDecimal(lot.quantity)}` : inventoryLotId;
}

function cartNotes(cart: CartSummary) {
  return `${cart.passengerAssignments?.length ?? 0} pasajeros · ${cart.cargoAllocations?.length ?? 0} cargas`;
}

watch(
  campaignId,
  async (nextCampaignId) => {
    campaignStore.selectCampaign(nextCampaignId);

    try {
      await Promise.all([catalogStore.loadAllCatalogs(), cycleStore.loadCampaignDailyCycle(nextCampaignId)]);
    } catch {
      // Stores already expose their own error states.
    }
  },
  { immediate: true },
);
</script>
