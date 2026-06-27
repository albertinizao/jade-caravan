<template>
  <AppShell>
    <section class="page dashboard-page">
      <header class="hero-card">
        <p class="eyebrow">Campaña {{ campaignId }}</p>
        <h2>Tablero operativo</h2>
        <p class="muted">
          Una vista de situación para el director: estado del día, alertas críticas, carros,
          viajeros, bestias, inventario y reglas activas, todo con el desglose del backend.
        </p>
      </header>

      <p v-if="loadingState === 'loading'" class="banner banner--info" role="status">
        Cargando tablero operativo...
      </p>
      <p v-else-if="loadingState === 'error'" class="banner banner--danger" role="alert">
        {{ dashboardErrorMessage }}
      </p>

      <section v-if="cycleState" class="dashboard-grid-section">
        <article class="panel">
          <div class="panel__header">
            <div>
              <h3>Resumen de caravana</h3>
              <p class="muted">
                {{ cycleState.caravan.name }} · nivel {{ cycleState.caravan.level }} · reglas
                {{ cycleState.caravan.ruleSetVersionId }}
              </p>
            </div>
            <div class="actions-inline">
              <RouterLink class="button button--secondary" :to="cycleRoute">Abrir ciclo diario</RouterLink>
              <RouterLink class="button button--secondary" :to="rulesRoute">Revisar reglas</RouterLink>
            </div>
          </div>

          <div class="metric-grid">
            <article v-for="metric in dashboardMetrics" :key="metric.label" class="summary-card">
              <p class="summary-card__label">{{ metric.label }}</p>
              <strong class="summary-card__value" :class="metricToneClass(metric.tone)">
                {{ metric.value }}
              </strong>
              <p v-if="metric.detail" class="summary-card__meta">{{ metric.detail }}</p>
            </article>
          </div>
        </article>

        <article class="panel">
          <div class="panel__header">
            <div>
              <h3>Alertas ordenadas por severidad</h3>
              <p class="muted">Primero bloqueos, después avisos y por último observaciones.</p>
            </div>
          </div>

          <ul v-if="sortedIssues.length > 0" class="issue-list">
            <li v-for="issue in sortedIssues" :key="`${issue.severity}-${issue.code}-${issue.subject}`" :class="toneClass(issue.severity)">
              <div class="issue-list__header">
                <strong>{{ severityLabel(issue.severity) }} · {{ issue.code }}</strong>
                <span class="issue-list__subject">{{ issue.subject }}</span>
              </div>
              <p class="issue-list__message">{{ issue.message }}</p>
              <dl class="issue-list__details">
                <div>
                  <dt>Fuente</dt>
                  <dd>{{ issue.source }}</dd>
                </div>
                <div v-for="(value, key) in issue.details" :key="`${issue.code}-${key}`">
                  <dt>{{ key }}</dt>
                  <dd>{{ value }}</dd>
                </div>
              </dl>
            </li>
          </ul>
          <p v-else class="muted">No hay alertas activas para esta campaña.</p>
        </article>

        <article class="panel">
          <div class="panel__header">
            <div>
              <h3>Ciclo actual</h3>
              <p class="muted">
                Día {{ activeDay?.dayNumber ?? '—' }} · {{ activeDay?.status ?? 'Sin día activo' }}
              </p>
            </div>
            <RouterLink class="button button--secondary" :to="cycleRoute">Ver detalle del día</RouterLink>
          </div>

          <div class="preview-grid">
            <article class="summary-card">
              <p class="summary-card__label">Distancia</p>
              <strong class="summary-card__value">{{ activeDay?.plannedDistanceMiles ?? '—' }}</strong>
              <p class="summary-card__meta">Planificada hoy</p>
            </article>
            <article class="summary-card">
              <p class="summary-card__label">Consumo</p>
              <strong class="summary-card__value">{{ cycleSummary?.dailyConsumption ?? '—' }}</strong>
              <p class="summary-card__meta">Diario calculado por backend</p>
            </article>
            <article class="summary-card">
              <p class="summary-card__label">Velocidad</p>
              <strong class="summary-card__value">{{ cycleSummary?.speedMilesPerDay ?? '—' }}</strong>
              <p class="summary-card__meta">Millas por día</p>
            </article>
            <article class="summary-card">
              <p class="summary-card__label">Mutiny</p>
              <strong class="summary-card__value">{{ cycleSummary?.mutinyPenalty ?? '—' }}</strong>
              <p class="summary-card__meta">Penalización total</p>
            </article>
          </div>

          <div v-if="cycleBreakdown.length > 0" class="breakdown-table">
            <h4>Desglose del cálculo</h4>
            <table>
              <thead>
                <tr>
                  <th>Concepto</th>
                  <th>Valor</th>
                  <th>Fuente</th>
                  <th>Notas</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in cycleBreakdown.slice(0, 6)" :key="`${item.concept}-${item.source}`">
                  <td>{{ item.concept }}</td>
                  <td>{{ item.value }}</td>
                  <td>{{ item.source }}</td>
                  <td>{{ item.notes }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </article>

        <article class="panel">
          <div class="panel__header">
            <div>
              <h3>Carros operativos</h3>
              <p class="muted">{{ operativeCarts.length }} operativos de {{ cycleState.caravan.carts.length }} totales</p>
            </div>
            <RouterLink class="button button--secondary" :to="cartsRoute">Abrir carros</RouterLink>
          </div>

          <div class="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>Carro</th>
                  <th>Tipo</th>
                  <th>PG</th>
                  <th>Ocupación</th>
                  <th>Carga</th>
                  <th>Tiro</th>
                  <th>Mejoras</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="cart in operativeCarts" :key="cart.id">
                  <td>
                    <strong>{{ cart.name }}</strong>
                    <div class="muted">{{ cartNotes(cart) }}</div>
                  </td>
                  <td>{{ cart.cartType?.name ?? '—' }}</td>
                  <td>{{ cart.currentHitPoints }}</td>
                  <td>{{ cart.passengerAssignments?.length ?? 0 }}</td>
                  <td>{{ cart.cargoAllocations?.length ?? 0 }}</td>
                  <td>{{ cart.towingAssignments?.length ?? 0 }}</td>
                  <td>{{ cart.upgradeInstances?.filter((upgrade) => upgrade.active).length ?? 0 }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </article>

        <article class="panel">
          <div class="panel__header">
            <div>
              <h3>Viajeros</h3>
              <p class="muted">{{ travellers.length }} viajeros cargados</p>
            </div>
            <RouterLink class="button button--secondary" :to="travellersRoute">Abrir viajeros</RouterLink>
          </div>

          <div class="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>Nombre</th>
                  <th>Estado</th>
                  <th>Rol diario</th>
                  <th>Contrato</th>
                  <th>Relaciones</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="traveller in travellers" :key="traveller.id">
                  <td><strong>{{ traveller.name }}</strong></td>
                  <td>{{ traveller.status ?? '—' }}</td>
                  <td>{{ traveller.dailyRoleAssignments?.length ?? 0 }}</td>
                  <td>{{ formatTravellerContract(traveller) }}</td>
                  <td>{{ traveller.relations?.length ?? 0 }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </article>

        <article class="panel">
          <div class="panel__header">
            <div>
              <h3>Inventario y bestias</h3>
              <p class="muted">Capacidad, carga y propulsión con los datos actuales de la campaña.</p>
            </div>
            <div class="actions-inline">
              <RouterLink class="button button--secondary" :to="inventoryRoute">Inventario</RouterLink>
              <RouterLink class="button button--secondary" :to="beastsRoute">Bestias</RouterLink>
            </div>
          </div>

          <div class="split-grid">
            <section>
              <h4>Inventario</h4>
              <ul class="compact-list">
                <li v-for="lot in inventoryLots" :key="lot.id">
                  <strong>{{ lot.cargoTypeId }}</strong>
                  <span class="muted">{{ lot.quantity }} · capacidad {{ lot.unitCapacity ?? '—' }}</span>
                </li>
              </ul>
            </section>

            <section>
              <h4>Bestias</h4>
              <ul class="compact-list">
                <li v-for="beast in beasts" :key="beast.id">
                  <strong>{{ beast.name }}</strong>
                  <span class="muted">
                    {{ beastState(beast) }} · {{ beast.beastType?.name ?? 'tipo sin catalogar' }}
                  </span>
                </li>
              </ul>
            </section>
          </div>
        </article>

        <article class="panel">
          <div class="panel__header">
            <div>
              <h3>Reglas activas</h3>
              <p class="muted">Bloqueadores pendientes y resoluciones recientes.</p>
            </div>
            <RouterLink class="button button--secondary" :to="rulesRoute">Ir a reglas</RouterLink>
          </div>

          <div class="split-grid">
            <section>
              <h4>Bloqueadores</h4>
              <ul class="compact-list">
                <li v-for="blocker in unresolvedBlockers" :key="blocker.decisionKey">
                  <strong>{{ blocker.key }} · {{ blocker.title }}</strong>
                  <span class="muted">{{ blocker.defaultProposal }}</span>
                </li>
                <li v-if="unresolvedBlockers.length === 0" class="muted">Sin bloqueadores activos.</li>
              </ul>
            </section>

            <section>
              <h4>Historial reciente</h4>
              <ul class="compact-list">
                <li v-for="entry in recentAuditEntries" :key="`${entry.decisionKey}-${entry.resolvedAt}`">
                  <strong>{{ entry.decisionKey }}</strong>
                  <span class="muted">{{ entry.currentResolution }} · {{ entry.resolvedAt }}</span>
                </li>
                <li v-if="recentAuditEntries.length === 0" class="muted">Sin auditoría reciente.</li>
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
import { useCampaignDailyCycleStore, useCampaignRulesStore, useCampaignStore } from '../stores';
import {
  combineIssues,
  formatDecimal,
  formatText,
  severityLabel,
  sortIssues,
  toneClass,
  type PresentationIssue,
  type PresentationMetric,
} from '../utils/presentation';

interface CampaignDashboardViewProps {
  campaignId?: string;
}

const props = defineProps<CampaignDashboardViewProps>();
const campaignId = computed(() => props.campaignId ?? 'demo');

const campaignStore = useCampaignStore();
const catalogStore = useCatalogStore();
const cycleStore = useCampaignDailyCycleStore();
const rulesStore = useCampaignRulesStore();

const loadingState = computed(() => {
  if (cycleStore.status === 'loading' || rulesStore.status === 'loading' || catalogStore.status === 'loading') {
    return 'loading';
  }

  if (cycleStore.status === 'error' || rulesStore.status === 'error' || catalogStore.status === 'error') {
    return 'error';
  }

  return 'ready';
});

const dashboardErrorMessage = computed(
  () => cycleStore.errorMessage ?? rulesStore.errorMessage ?? catalogStore.errorMessage ?? 'No se ha podido cargar el tablero.',
);

const cycleState = computed(() => cycleStore.cycleState);
const cycleSummary = computed(() => cycleStore.preview?.calculationSummary ?? null);
const activeDay = computed(() => cycleStore.activeDay);
const unresolvedBlockers = computed(() => rulesStore.unresolvedBlockers);
const recentAuditEntries = computed(() => rulesStore.auditTrail.slice(0, 5));

const travellers = computed(() => cycleStore.travellers);
const inventoryLots = computed(() => cycleStore.inventoryLots);
const beasts = computed(() => cycleStore.beasts);
const sortedIssues = computed(() => {
  const issues: PresentationIssue[] = combineIssues(cycleSummary.value?.warnings ?? [], cycleSummary.value?.blockers ?? []);
  return sortIssues(issues);
});
const cycleBreakdown = computed(() => cycleSummary.value?.breakdown.map((item) => ({
  concept: item.concept,
  value: item.value,
  source: item.source,
  notes: item.notes ?? '—',
})) ?? []);

const operativeCarts = computed(() =>
  cycleState.value?.caravan.carts.filter((cart) => !cart.destroyed && cart.currentHitPoints > 0) ?? [],
);

const totalTravellerContracts = computed(() =>
  travellers.value.filter((traveller) => traveller.contract?.active && traveller.contract.monthlyCostCp > 0),
);

const dashboardMetrics = computed<PresentationMetric[]>(() => [
  {
    label: 'Carros operativos',
    value: formatText(`${operativeCarts.value.length} / ${cycleState.value?.caravan.carts.length ?? 0}`),
    detail: 'Operativos frente al total registrado',
    tone: operativeCarts.value.length === cycleState.value?.caravan.carts.length ? 'info' : 'warning',
  },
  {
    label: 'Viajeros alojados',
    value: formatText(`${cycleSummary.value?.passengerOccupancy ?? '—'} / ${cycleSummary.value?.passengerCapacity ?? '—'}`),
    detail: 'Ocupación de viajeros frente a capacidad',
    tone: undefined,
  },
  {
    label: 'Carga',
    value: formatText(`${cycleSummary.value?.cargoOccupancy ?? '—'} / ${cycleSummary.value?.cargoCapacity ?? '—'}`),
    detail: 'Carga total frente a capacidad de carga',
    tone: undefined,
  },
  {
    label: 'Velocidad',
    value: formatDecimal(cycleSummary.value?.speedMilesPerDay),
    detail: 'Millas por día calculadas',
    tone: undefined,
  },
  {
    label: 'Consumo diario',
    value: formatDecimal(cycleSummary.value?.dailyConsumption),
    detail: 'Consumo base del día',
    tone: undefined,
  },
  {
    label: 'Descontento',
    value: formatText(cycleState.value?.caravan.currentDiscontent),
    detail: `Mutiny ${formatDecimal(cycleSummary.value?.mutinyPenalty)}`,
    tone: undefined,
  },
  {
    label: 'Bestias',
    value: formatText(`${beasts.value.filter((beast) => beast.activeAsTowing).length} / ${beasts.value.length}`),
    detail: 'En tiro frente al total',
    tone: undefined,
  },
  {
    label: 'Contratos salariales',
    value: formatText(`${totalTravellerContracts.value.length} activos`),
    detail: 'El backend no expone el estado de pago individual; se muestra la obligación registrada.',
    tone: 'warning',
  },
]);

const cycleRoute = computed(() => ({ name: 'campaign-day-cycle', params: { campaignId: campaignId.value } }));
const cartsRoute = computed(() => ({ name: 'campaign-carts', params: { campaignId: campaignId.value } }));
const travellersRoute = computed(() => ({ name: 'campaign-travellers', params: { campaignId: campaignId.value } }));
const inventoryRoute = computed(() => ({ name: 'campaign-inventory', params: { campaignId: campaignId.value } }));
const beastsRoute = computed(() => ({ name: 'campaign-beasts', params: { campaignId: campaignId.value } }));
const rulesRoute = computed(() => ({ name: 'campaign-rules', params: { campaignId: campaignId.value } }));

function metricToneClass(tone: PresentationMetric['tone']): string | undefined {
  if (!tone) {
    return undefined;
  }

  if (tone === 'warning') {
    return 'summary-card__value--warning';
  }

  if (tone === 'danger') {
    return 'summary-card__value--danger';
  }

  return 'summary-card__value--info';
}

function cartNotes(cart: NonNullable<typeof cycleState.value>['caravan']['carts'][number]) {
  const activeUpgrades = cart.upgradeInstances?.filter((upgrade) => upgrade.active).length ?? 0;
  const towingAssignments = cart.towingAssignments?.length ?? 0;
  return `${activeUpgrades} mejoras · ${towingAssignments} tiros`;
}

function formatTravellerContract(traveller: NonNullable<typeof travellers.value>[number]) {
  if (!traveller.contract) {
    return 'Sin contrato';
  }

  return `${traveller.contract.contractType} · ${traveller.contract.monthlyCostCp} cp`;
}

function beastState(beast: NonNullable<typeof beasts.value>[number]) {
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

watch(
  campaignId,
  async (nextCampaignId) => {
    campaignStore.selectCampaign(nextCampaignId);

    try {
      await Promise.all([
        catalogStore.loadAllCatalogs(),
        cycleStore.loadCampaignDailyCycle(nextCampaignId),
        rulesStore.loadCampaignRules(nextCampaignId),
      ]);
    } catch {
      // Each store already exposes its own error state.
    }
  },
  { immediate: true },
);
</script>
