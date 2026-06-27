<template>
  <AppShell>
    <section class="page daily-cycle-page">
      <header class="daily-cycle-hero">
        <p class="eyebrow">Campaña {{ campaignId }}</p>
        <h2>Ciclo diario</h2>
        <p class="muted">
          Planifica, simula y cierra el día con desglose reproducible, bloqueos visibles y
          auditoría de cada decisión.
        </p>
      </header>

      <p v-if="store.status === 'loading'" class="banner banner--info" role="status">
        Cargando el ciclo diario...
      </p>
      <p v-else-if="store.status === 'saving'" class="banner banner--info" role="status">
        Guardando cambios del día...
      </p>
      <p v-else-if="store.errorMessage" class="banner banner--danger" role="alert">
        {{ store.errorMessage }}
      </p>

      <section v-if="store.cycleState" class="daily-cycle-grid">
        <article class="panel">
          <div class="panel__header">
            <div>
              <h3>Día activo</h3>
              <p class="muted">
                Estado: <strong>{{ activeDay?.status ?? 'Sin día activo' }}</strong> · Versión
                {{ activeDay?.dayNumber ?? '—' }}
              </p>
            </div>
            <button class="button button--secondary" type="button" @click="reloadState">
              Recargar
            </button>
          </div>

          <dl class="facts-grid">
            <div>
              <dt>Actividad</dt>
              <dd>{{ activeDay?.activityType ?? '—' }}</dd>
            </div>
            <div>
              <dt>Terreno</dt>
              <dd>{{ activeDay?.terrainType ?? '—' }}</dd>
            </div>
            <div>
              <dt>Ubicación</dt>
              <dd>{{ activeDay?.location ?? '—' }}</dd>
            </div>
            <div>
              <dt>Descontento</dt>
              <dd>{{ currentDiscontent }}</dd>
            </div>
            <div>
              <dt>Viajeros</dt>
              <dd>{{ store.travellers.length }}</dd>
            </div>
            <div>
              <dt>Carros</dt>
              <dd>{{ store.carts.length }}</dd>
            </div>
          </dl>
        </article>

        <article class="panel">
          <div class="panel__header">
            <div>
              <h3>Planificación del día</h3>
              <p class="muted">Crea o actualiza el contexto del día antes de resolverlo.</p>
            </div>
            <button class="button" type="button" @click="submitCreateDay">Guardar día</button>
          </div>

          <div class="form-grid">
            <label class="field">
              <span>Número de día</span>
              <input v-model="dayDraft.dayNumber" type="number" min="1" />
            </label>
            <label class="field">
              <span>Actividad</span>
              <select v-model="dayDraft.activityType">
                <option value="TRAVEL">Viaje</option>
                <option value="REST">Descanso</option>
                <option value="CIVILIZATION_PAUSE">Pausa civilizada</option>
                <option value="WORK">Trabajo</option>
                <option value="CAMP">Campamento</option>
                <option value="COMBAT">Combate</option>
              </select>
            </label>
            <label class="field">
              <span>Terreno</span>
              <input v-model="dayDraft.terrainType" type="text" />
            </label>
            <label class="field">
              <span>Ubicación</span>
              <input v-model="dayDraft.location" type="text" />
            </label>
            <label class="field">
              <span>Asentamiento</span>
              <input v-model="dayDraft.settlementType" type="text" placeholder="Opcional" />
            </label>
            <label class="field">
              <span>Temperatura (°F)</span>
              <input v-model="dayDraft.temperatureF" type="number" />
            </label>
            <label class="field">
              <span>Clima</span>
              <input v-model="dayDraft.weatherSeverity" type="text" />
            </label>
            <label class="field">
              <span>Horas de viaje</span>
              <input v-model="dayDraft.travelHours" type="text" />
            </label>
            <label class="field">
              <span>Distancia planificada</span>
              <input v-model="dayDraft.plannedDistanceMiles" type="text" />
            </label>
          </div>
        </article>

        <article class="panel">
          <div class="panel__header">
            <div>
              <h3>Asignaciones</h3>
              <p class="muted">Carga roles, alojamiento y tiro de forma explícita.</p>
            </div>
            <button class="button button--secondary" type="button" @click="submitPlanDay">
              Guardar planificación
            </button>
          </div>

          <section class="assignment-block">
            <h4>Rol diario</h4>
            <div class="form-grid form-grid--compact">
              <label class="field">
                <span>Viajero</span>
                <select v-model="planDraft.roleTravellerId">
                  <option value="">Selecciona...</option>
                  <option v-for="traveller in store.travellers" :key="traveller.id" :value="traveller.id">
                    {{ traveller.name }}
                  </option>
                </select>
              </label>
              <label class="field">
                <span>Rol</span>
                <select v-model="planDraft.roleKey">
                  <option v-for="role in roleOptions" :key="role.key" :value="role.key">
                    {{ role.name }} · {{ role.key }}
                  </option>
                </select>
              </label>
              <label class="field">
                <span>Carro objetivo</span>
                <select v-model="planDraft.roleCartId">
                  <option value="">Sin carro</option>
                  <option v-for="cart in store.carts" :key="cart.id" :value="cart.id">
                    {{ cart.name }}
                  </option>
                </select>
              </label>
              <div class="field field--inline">
                <span>&nbsp;</span>
                <button class="button button--secondary" type="button" @click="addRoleAssignment">
                  Añadir rol
                </button>
              </div>
            </div>
          </section>

          <section class="assignment-block">
            <h4>Alojamiento</h4>
            <div class="form-grid form-grid--compact">
              <label class="field">
                <span>Viajero</span>
                <select v-model="planDraft.passengerTravellerId">
                  <option value="">Selecciona...</option>
                  <option v-for="traveller in store.travellers" :key="traveller.id" :value="traveller.id">
                    {{ traveller.name }}
                  </option>
                </select>
              </label>
              <label class="field">
                <span>Carro</span>
                <select v-model="planDraft.passengerCartId">
                  <option value="">Selecciona...</option>
                  <option v-for="cart in store.carts" :key="cart.id" :value="cart.id">
                    {{ cart.name }}
                  </option>
                </select>
              </label>
              <label class="field">
                <span>Ocupación</span>
                <input v-model="planDraft.passengerOccupancy" type="text" />
              </label>
              <div class="field field--inline">
                <span>&nbsp;</span>
                <button class="button button--secondary" type="button" @click="addPassengerAssignment">
                  Añadir alojamiento
                </button>
              </div>
            </div>
          </section>

          <section class="assignment-block">
            <h4>Tiro</h4>
            <div class="form-grid form-grid--compact">
              <label class="field">
                <span>Bestia</span>
                <select v-model="planDraft.towingBeastId">
                  <option value="">Selecciona...</option>
                  <option v-for="beast in store.beasts" :key="beast.id" :value="beast.id">
                    {{ beast.name }}
                  </option>
                </select>
              </label>
              <label class="field">
                <span>Carro</span>
                <select v-model="planDraft.towingCartId">
                  <option value="">Selecciona...</option>
                  <option v-for="cart in store.carts" :key="cart.id" :value="cart.id">
                    {{ cart.name }}
                  </option>
                </select>
              </label>
              <div class="field field--inline">
                <span>&nbsp;</span>
                <button class="button button--secondary" type="button" @click="addTowingAssignment">
                  Añadir tiro
                </button>
              </div>
            </div>
          </section>

          <section class="assignment-block">
            <h4>Acciones especiales</h4>
            <div class="form-grid form-grid--compact">
              <label class="field">
                <span>Tipo</span>
                <select v-model="planDraft.operationType">
                  <option value="TRAVEL">Viaje</option>
                  <option value="REST">Descanso</option>
                  <option value="CIVILISED_PAUSE">Pausa civilizada</option>
                  <option value="REPAIR">Reparación</option>
                  <option value="EATING">Comer</option>
                  <option value="CELEBRATION">Celebración</option>
                  <option value="GIFTING">Regalo</option>
                  <option value="FASTING">Ayuno</option>
                  <option value="COMMERCE">Comercio</option>
                </select>
              </label>
              <label class="field">
                <span>Título</span>
                <input v-model="planDraft.operationTitle" type="text" />
              </label>
              <label class="field">
                <span>Cantidad</span>
                <input v-model="planDraft.operationQuantity" type="text" />
              </label>
              <label class="field">
                <span>Recurso</span>
                <input v-model="planDraft.operationResourceType" type="text" />
              </label>
              <label class="field field--full">
                <span>Notas</span>
                <input v-model="planDraft.operationNotes" type="text" />
              </label>
              <div class="field field--inline">
                <span>&nbsp;</span>
                <button class="button button--secondary" type="button" @click="addDailyOperation">
                  Añadir acción
                </button>
              </div>
            </div>
          </section>
        </article>

        <article class="panel">
          <div class="panel__header">
            <div>
              <h3>Resolución</h3>
              <p class="muted">Registra checks, eventos, comercio y cualquier resolución manual.</p>
            </div>
            <button class="button button--secondary" type="button" @click="submitResolveDay">
              Guardar resolución
            </button>
          </div>

          <div class="form-grid">
            <label class="field field--full">
              <span>Checks (JSON)</span>
              <textarea v-model="resolutionDraft.checksJson" rows="6" />
            </label>
            <label class="field field--full">
              <span>Eventos (JSON)</span>
              <textarea v-model="resolutionDraft.eventsJson" rows="6" />
            </label>
            <label class="field field--full">
              <span>Comercio (JSON)</span>
              <textarea v-model="resolutionDraft.tradesJson" rows="6" />
            </label>
          </div>
        </article>

        <article class="panel">
          <div class="panel__header">
            <div>
              <h3>Preview</h3>
              <p class="muted">La vista previa no aplica cambios: solo calcula y explica.</p>
            </div>
            <div class="actions-inline">
              <button class="button button--secondary" type="button" @click="reloadPreview">
                Recalcular preview
              </button>
              <button class="button" type="button" @click="submitCloseDay">Cerrar día</button>
              <button class="button button--secondary" type="button" @click="submitReopenDay">
                Reabrir
              </button>
            </div>
          </div>

          <div v-if="store.preview" class="preview-grid">
            <article class="summary-card">
              <p class="summary-card__label">Velocidad</p>
              <strong class="summary-card__value">{{ store.preview.calculationSummary.speedMilesPerDay }}</strong>
            </article>
            <article class="summary-card">
              <p class="summary-card__label">Consumo</p>
              <strong class="summary-card__value">{{ store.preview.calculationSummary.dailyConsumption }}</strong>
            </article>
            <article class="summary-card">
              <p class="summary-card__label">Mutiny</p>
              <strong class="summary-card__value">{{ store.preview.calculationSummary.mutinyPenalty }}</strong>
            </article>
            <article class="summary-card">
              <p class="summary-card__label">Bloqueos</p>
              <strong class="summary-card__value">{{ store.preview.travelValidation.blockers.length }}</strong>
            </article>
          </div>

          <div v-if="store.preview?.alerts.length" class="alerts-list">
            <h4>Alertas</h4>
            <ul>
              <li v-for="alert in store.preview.alerts" :key="alert">{{ alert }}</li>
            </ul>
          </div>
        </article>
      </section>
    </section>
  </AppShell>
</template>

<script setup lang="ts">
import { computed, reactive, watch } from 'vue';
import AppShell from '@/layouts/AppShell.vue';
import { useCatalogStore } from '@/stores';
import { useCampaignDailyCycleStore, useCampaignStore } from '../stores';
import type {
  CampaignDay,
  CampaignDayCloseRequest,
  CampaignDayCreateRequest,
  CampaignDayPlanRequest,
  CampaignDayResolveRequest,
  DailyOperation,
} from '../types';

interface CampaignDailyCycleViewProps {
  campaignId?: string;
}

interface AssignmentDraft {
  roleTravellerId: string;
  roleKey: string;
  roleCartId: string;
  passengerTravellerId: string;
  passengerCartId: string;
  passengerOccupancy: string;
  towingBeastId: string;
  towingCartId: string;
  operationType: DailyOperation['operationType'];
  operationTitle: string;
  operationQuantity: string;
  operationResourceType: string;
  operationNotes: string;
}

interface DayDraft {
  dayNumber: string;
  activityType: CampaignDay['activityType'];
  terrainType: string;
  location: string;
  settlementType: string;
  temperatureF: string;
  weatherSeverity: string;
  travelHours: string;
  plannedDistanceMiles: string;
  actor: string;
  source: string;
  reason: string;
}

interface ResolutionDraft {
  checksJson: string;
  eventsJson: string;
  tradesJson: string;
}

const props = defineProps<CampaignDailyCycleViewProps>();
const campaignId = computed(() => props.campaignId ?? 'demo');
const store = useCampaignDailyCycleStore();
const catalogStore = useCatalogStore();
const campaignStore = useCampaignStore();

const dayDraft = reactive<DayDraft>({
  dayNumber: '1',
  activityType: 'TRAVEL',
  terrainType: 'Road',
  location: 'Jade Road',
  settlementType: 'Village',
  temperatureF: '70',
  weatherSeverity: 'clear',
  travelHours: '8',
  plannedDistanceMiles: '12',
  actor: 'Director de juego',
  source: 'daily-cycle',
  reason: 'Preparación del día',
});

const planDraft = reactive<AssignmentDraft>({
  roleTravellerId: '',
  roleKey: 'WAGONER',
  roleCartId: '',
  passengerTravellerId: '',
  passengerCartId: '',
  passengerOccupancy: '1',
  towingBeastId: '',
  towingCartId: '',
  operationType: 'TRAVEL',
  operationTitle: 'Viaje',
  operationQuantity: '1',
  operationResourceType: 'SUPPLIES',
  operationNotes: '',
});

const resolutionDraft = reactive<ResolutionDraft>({
  checksJson: '[]',
  eventsJson: '[]',
  tradesJson: '[]',
});

const roleOptions = computed(() => {
  const rolesCatalog = catalogStore.catalogs['roles'];
  return (rolesCatalog?.entries ?? []).map((entry) => ({
    key: entry.key,
    name: entry.name,
    hardLimit: String(entry.attributes.hardLimit ?? entry.attributes.hard_limit ?? ''),
    requirement: String(entry.attributes.requirement ?? ''),
    benefitSummary: String(entry.attributes.benefitSummary ?? ''),
    optionalSubsystem: Boolean(entry.attributes.optionalSubsystem ?? false),
    campaignSpecific: entry.campaignSpecific,
    source: entry.source,
    note: entry.note ?? null,
  }));
});

const activeDay = computed(() => store.activeDay);
const currentDiscontent = computed(() => store.currentDiscontent);

function buildRoleCatalogEntry(roleKey: string) {
  const match = roleOptions.value.find((role) => role.key === roleKey);
  return {
    key: match?.key ?? roleKey,
    name: match?.name ?? roleKey,
    hardLimit: match?.hardLimit ?? 'uno por día',
    requirement: match?.requirement ?? '',
    benefitSummary: match?.benefitSummary ?? '',
    optionalSubsystem: match?.optionalSubsystem ?? false,
    campaignSpecific: match?.campaignSpecific ?? false,
    source: match?.source ?? 'frontend',
    note: match?.note ?? null,
  };
}

function buildCampaignDay(): CampaignDay {
  return {
    id: crypto.randomUUID(),
    caravanId: store.cycleState?.caravan.id ?? crypto.randomUUID(),
    dayNumber: Number(dayDraft.dayNumber) || 1,
    status: 'DRAFT',
    activityType: dayDraft.activityType,
    terrainType: dayDraft.terrainType,
    location: dayDraft.location,
    settlementType: dayDraft.settlementType.trim() || null,
    temperatureF: dayDraft.temperatureF.trim() ? Number(dayDraft.temperatureF) : null,
    weatherSeverity: dayDraft.weatherSeverity.trim() || null,
    travelHours: dayDraft.travelHours.trim() || null,
    plannedDistanceMiles: dayDraft.plannedDistanceMiles.trim() || null,
    resolvedDistanceMiles: null,
    checkResolutions: [],
    caravanEvents: [],
    tradeTransactions: [],
  };
}

function parseJsonArray<T>(value: string): T[] {
  if (!value.trim()) {
    return [];
  }

  const parsed = JSON.parse(value) as T[];
  return Array.isArray(parsed) ? parsed : [];
}

function refreshDraftsFromState() {
  const day = activeDay.value;
  if (!day) {
    return;
  }

  dayDraft.dayNumber = String(day.dayNumber);
  dayDraft.activityType = day.activityType;
  dayDraft.terrainType = day.terrainType;
  dayDraft.location = day.location;
  dayDraft.settlementType = day.settlementType ?? '';
  dayDraft.temperatureF = day.temperatureF?.toString() ?? '';
  dayDraft.weatherSeverity = day.weatherSeverity ?? '';
  dayDraft.travelHours = day.travelHours ?? '';
  dayDraft.plannedDistanceMiles = day.plannedDistanceMiles ?? '';
}

function toPlanRequest(): CampaignDayPlanRequest {
  return {
    roleAssignments: planDraft.roleTravellerId
      ? [
          {
            travellerId: planDraft.roleTravellerId,
            campaignDayId: store.activeDay?.id ?? store.cycleState?.activeDayId,
            role: buildRoleCatalogEntry(planDraft.roleKey),
            targetCartId: planDraft.roleCartId || null,
            targetTravellerId: null,
            targetSkill: null,
            targetLanguage: null,
            optionJson: null,
          },
        ]
      : [],
    passengerAssignments: planDraft.passengerTravellerId && planDraft.passengerCartId
      ? [
          {
            cartId: planDraft.passengerCartId,
            travellerId: planDraft.passengerTravellerId,
            occupancyUnits: planDraft.passengerOccupancy,
            notes: 'Asignación desde el planner diario',
          },
        ]
      : [],
    towingAssignments: planDraft.towingBeastId && planDraft.towingCartId
      ? [
          {
            beastId: planDraft.towingBeastId,
            cartId: planDraft.towingCartId,
            campaignDayId: store.activeDay?.id ?? store.cycleState?.activeDayId,
          },
        ]
      : [],
    dailyOperations: [
      {
        id: crypto.randomUUID(),
        campaignDayId: store.activeDay?.id ?? store.cycleState?.activeDayId ?? crypto.randomUUID(),
        operationType: planDraft.operationType,
        title: planDraft.operationTitle,
        quantity: planDraft.operationQuantity.trim() || null,
        resourceType: planDraft.operationResourceType.trim() || null,
        notes: planDraft.operationNotes.trim() || null,
      },
    ],
    overrideBlockers: false,
    overrideReason: undefined,
    actor: dayDraft.actor,
    source: dayDraft.source,
  };
}

async function reloadState() {
  await store.loadCampaignDailyCycle(campaignId.value);
  refreshDraftsFromState();
}

async function reloadPreview() {
  if (!store.cycleState) {
    await reloadState();
    return;
  }

  await store.loadCampaignDailyCycle(campaignId.value);
  refreshDraftsFromState();
}

async function submitCreateDay() {
  const request: CampaignDayCreateRequest = {
    campaignDay: buildCampaignDay(),
    actor: dayDraft.actor,
    source: dayDraft.source,
    reason: dayDraft.reason,
  };
  await store.createDay(campaignId.value, request);
  refreshDraftsFromState();
}

async function submitPlanDay() {
  await store.planDay(campaignId.value, toPlanRequest());
  refreshDraftsFromState();
}

async function submitResolveDay() {
  const request: CampaignDayResolveRequest = {
    checkResolutions: parseJsonArray(resolutionDraft.checksJson),
    caravanEvents: parseJsonArray(resolutionDraft.eventsJson),
    tradeTransactions: parseJsonArray(resolutionDraft.tradesJson),
    actor: dayDraft.actor,
    source: dayDraft.source,
    reason: dayDraft.reason,
  };
  await store.resolveDay(campaignId.value, request);
}

async function submitCloseDay() {
  const request: CampaignDayCloseRequest = {
    actor: dayDraft.actor,
    source: dayDraft.source,
    reason: 'Cierre del día',
  };
  await store.closeDay(campaignId.value, request);
}

async function submitReopenDay() {
  await store.reopenDay(campaignId.value, {
    actor: dayDraft.actor,
    source: dayDraft.source,
    reason: 'Reapertura auditable',
  });
}

function addRoleAssignment() {
  planDraft.roleTravellerId = planDraft.roleTravellerId || (store.travellers[0]?.id ?? '');
  planDraft.roleCartId = planDraft.roleCartId || (store.carts[0]?.id ?? '');
}

function addPassengerAssignment() {
  planDraft.passengerTravellerId = planDraft.passengerTravellerId || (store.travellers[0]?.id ?? '');
  planDraft.passengerCartId = planDraft.passengerCartId || (store.carts[0]?.id ?? '');
}

function addTowingAssignment() {
  planDraft.towingBeastId = planDraft.towingBeastId || (store.beasts[0]?.id ?? '');
  planDraft.towingCartId = planDraft.towingCartId || (store.carts[0]?.id ?? '');
}

function addDailyOperation() {
  planDraft.operationTitle = planDraft.operationTitle.trim() || 'Acción diaria';
}

watch(
  campaignId,
  async (nextCampaignId) => {
    try {
      campaignStore.selectCampaign(nextCampaignId);
      await catalogStore.loadAllCatalogs();
      await store.loadCampaignDailyCycle(nextCampaignId);
      refreshDraftsFromState();
    } catch {
      // The store already captured the error state.
    }
  },
  { immediate: true },
);
</script>
