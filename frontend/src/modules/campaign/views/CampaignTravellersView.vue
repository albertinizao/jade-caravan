<template>
  <AppShell>
    <section class="page detail-page">
      <header class="hero-card">
        <p class="eyebrow">Campaña {{ campaignId }}</p>
        <h2>Viajeros</h2>
        <p class="muted">
          Información de cada viajero con contrato, capacidades, relaciones y asignaciones
          diarias.
        </p>
      </header>

      <p v-if="loadingState === 'loading'" class="banner banner--info" role="status">
        Cargando viajeros...
      </p>
      <p v-else-if="loadingState === 'error'" class="banner banner--danger" role="alert">
        {{ errorMessage }}
      </p>

      <section v-if="cycleState" class="panel">
        <div class="panel__header">
          <div>
            <h3>Resumen de viajeros</h3>
            <p class="muted">
              {{ cycleState.caravan.travellers.length }} viajeros cargados ·
              {{ countingTravellers }} cuentan para consumo y alojamiento
            </p>
          </div>
          <RouterLink class="button button--secondary" :to="dashboardRoute">Volver al tablero</RouterLink>
        </div>

        <div class="table-wrapper">
          <table>
            <thead>
              <tr>
                <th>Nombre</th>
                <th>Tipo</th>
                <th>Ocupación</th>
                <th>Consumo</th>
                <th>Estado</th>
                <th>Contrato</th>
                <th>Roles diarios</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="traveller in orderedTravellers" :key="traveller.id">
                <td><strong>{{ traveller.name }}</strong></td>
                <td>{{ traveller.playerCharacter ? 'PJ' : 'PNJ' }}</td>
                <td>{{ traveller.occupancyUnits ?? '—' }}</td>
                <td>{{ traveller.foodConsumption ?? '—' }}</td>
                <td>{{ traveller.status ?? '—' }}</td>
                <td>{{ contractLabel(traveller) }}</td>
                <td>{{ traveller.dailyRoleAssignments?.length ?? 0 }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section v-if="cycleState" class="detail-grid">
        <article v-for="traveller in orderedTravellers" :key="`${traveller.id}-detail`" class="panel">
          <div class="panel__header">
            <div>
              <h3>{{ traveller.name }}</h3>
              <p class="muted">
                {{ formatType(traveller) }} · {{ traveller.size ?? '—' }} ·
                {{ traveller.humanoid ? 'humanoide' : 'no humanoide' }}
              </p>
            </div>
          </div>

          <div class="facts-grid">
            <div>
              <dt>Consumo de comida</dt>
              <dd>{{ traveller.foodConsumption ?? '—' }}</dd>
            </div>
            <div>
              <dt>Ocupación</dt>
              <dd>{{ traveller.occupancyUnits ?? '—' }}</dd>
            </div>
            <div>
              <dt>BA</dt>
              <dd>{{ traveller.baseAttackBonus ?? '—' }}</dd>
            </div>
            <div>
              <dt>DG</dt>
              <dd>{{ traveller.hitDice ?? '—' }}</dd>
            </div>
            <div>
              <dt>Vivo / consciente</dt>
              <dd>{{ traveller.alive ? 'Vivo' : 'No vivo' }} · {{ traveller.conscious ? 'Consciente' : 'Inconsciente' }}</dd>
            </div>
            <div>
              <dt>Contrato</dt>
              <dd>{{ contractLabel(traveller) }}</dd>
            </div>
          </div>

          <div class="split-grid">
            <section>
              <h4>Asignaciones diarias</h4>
              <ul class="compact-list">
                <li v-for="assignment in traveller.dailyRoleAssignments ?? []" :key="`${traveller.id}-${assignment.campaignDayId}-${assignment.role.key}`">
                  <strong>{{ assignment.role.name }}</strong>
                  <span class="muted">
                    día {{ dayNumberFor(assignment.campaignDayId) }} ·
                    {{ assignment.targetCartId ? cartName(assignment.targetCartId) : 'sin carro objetivo' }}
                  </span>
                </li>
                <li v-if="(traveller.dailyRoleAssignments?.length ?? 0) === 0" class="muted">Sin asignaciones diarias.</li>
              </ul>
            </section>

            <section>
              <h4>Relaciones y capacidades</h4>
              <ul class="compact-list">
                <li v-for="relation in traveller.relations ?? []" :key="`${traveller.id}-${relation.sourceTravellerId}-${relation.targetTravellerId}`">
                  <strong>{{ relation.relationType }}</strong>
                  <span class="muted">{{ relation.notes ?? '—' }}</span>
                </li>
                <li v-for="capability in traveller.roleCapabilities ?? []" :key="`${traveller.id}-${capability.role.key}`">
                  <strong>{{ capability.role.name }}</strong>
                  <span class="muted">{{ capability.role.benefitSummary }}</span>
                </li>
                <li v-if="(traveller.relations?.length ?? 0) === 0 && (traveller.roleCapabilities?.length ?? 0) === 0" class="muted">
                  Sin relaciones ni capacidades registradas.
                </li>
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
import type { TravellerSummary } from '../types';

interface CampaignTravellersViewProps {
  campaignId?: string;
}

const props = defineProps<CampaignTravellersViewProps>();
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

const errorMessage = computed(() => cycleStore.errorMessage ?? 'No se han podido cargar los viajeros.');
const cycleState = computed(() => cycleStore.cycleState);
const dashboardRoute = computed(() => ({ name: 'campaign-dashboard', params: { campaignId: campaignId.value } }));
const orderedTravellers = computed(() => [...(cycleState.value?.caravan.travellers ?? [])].sort((left, right) => left.name.localeCompare(right.name)));
const countingTravellers = computed(() => cycleState.value?.caravan.travellers.filter((traveller) => traveller.countsAsTraveller !== false).length ?? 0);

function contractLabel(traveller: TravellerSummary) {
  if (!traveller.contract) {
    return 'Sin contrato';
  }

  return `${traveller.contract.contractType} · ${traveller.contract.monthlyCostCp} cp`;
}

function formatType(traveller: TravellerSummary) {
  const tags = [];
  if (traveller.playerCharacter) {
    tags.push('PJ');
  }
  if (traveller.countsAsTraveller === false) {
    tags.push('no cuenta como viajero');
  }
  if (traveller.needsRest) {
    tags.push('necesita descanso');
  }
  if (traveller.needsFood) {
    tags.push('necesita comida');
  }

  return tags.length > 0 ? tags.join(' · ') : 'Viajero normal';
}

function dayNumberFor(campaignDayId: string) {
  return cycleState.value?.caravan.campaignDays.find((day) => day.id === campaignDayId)?.dayNumber ?? '—';
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
      // Error state is already captured in the store.
    }
  },
  { immediate: true },
);
</script>
