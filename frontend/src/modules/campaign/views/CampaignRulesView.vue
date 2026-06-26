<template>
  <AppShell>
    <section class="page rules-page">
      <header class="rules-hero">
        <p class="eyebrow">Campaña {{ campaignId }}</p>
        <h2>Decisiones de reglas</h2>
        <p class="muted">
          Revisa los bloqueos de automatización, valida la propuesta por defecto y resuelve cada
          decisión con un motivo auditable.
        </p>
      </header>

      <p v-if="rulesStore.status === 'loading'" class="rules-banner rules-banner--info" role="status">
        Cargando decisiones activas de la campaña...
      </p>
      <p v-else-if="rulesStore.status === 'saving'" class="rules-banner rules-banner--info" role="status">
        Guardando la resolución de la decisión seleccionada...
      </p>
      <p v-else-if="rulesStore.errorMessage" class="rules-banner rules-banner--danger" role="alert">
        {{ rulesStore.errorMessage }}
      </p>

      <section v-if="rulesStore.summary" class="rules-summary" aria-labelledby="rules-summary-title">
        <div class="rules-summary__header">
          <h3 id="rules-summary-title">Resumen de reglas activas</h3>
          <p class="muted">
            El resumen procede del backend y muestra la versión activa de reglas para la campaña.
          </p>
        </div>

        <div class="rules-summary-grid">
          <article class="summary-card">
            <p class="summary-card__label">Automatización</p>
            <strong class="summary-card__value">
              {{ rulesStore.summary.automationBlocked ? 'Bloqueada' : 'Disponible' }}
            </strong>
            <p class="summary-card__meta">
              {{
                rulesStore.summary.automationBlocked
                  ? 'Hay decisiones pendientes que frenan la automatización.'
                  : 'La automatización puede continuar con las decisiones actuales.'
              }}
            </p>
          </article>
          <article class="summary-card">
            <p class="summary-card__label">Decisiones cargadas</p>
            <strong class="summary-card__value">{{ decisionGateItems.length }}</strong>
            <p class="summary-card__meta">Elementos visibles en la lista de revisión.</p>
          </article>
          <article class="summary-card">
            <p class="summary-card__label">Pendientes</p>
            <strong class="summary-card__value">{{ unresolvedBlockers.length }}</strong>
            <p class="summary-card__meta">Bloqueadores sin resolver.</p>
          </article>
          <article class="summary-card">
            <p class="summary-card__label">Resueltas</p>
            <strong class="summary-card__value">{{ resolvedCount }}</strong>
            <p class="summary-card__meta">Decisiones ya registradas con motivo.</p>
          </article>
          <article class="summary-card">
            <p class="summary-card__label">Versión de reglas</p>
            <strong class="summary-card__value">{{ rulesStore.summary.ruleSetVersionId }}</strong>
            <p class="summary-card__meta">Versión histórica activa para esta campaña.</p>
          </article>
        </div>

        <div class="rules-blockers">
          <h4>Bloqueadores pendientes</h4>
          <p v-if="unresolvedBlockers.length === 0" class="muted">
            No hay bloqueadores activos en este momento.
          </p>
          <ul v-else class="rules-blockers__list">
            <li v-for="blocker in unresolvedBlockers" :key="blocker.key" class="rules-blocker">
              <strong>{{ blocker.key }} · {{ blocker.title }}</strong>
              <span class="muted">{{ blocker.defaultProposal }}</span>
            </li>
          </ul>
        </div>
      </section>

      <section class="rules-decision-list" aria-labelledby="decision-gates-title">
        <div class="rules-summary__header">
          <h3 id="decision-gates-title">Lista de decisiones</h3>
          <p class="muted">
            Cada tarjeta muestra el estado actual, la propuesta por defecto y si el elemento bloquea
            la automatización.
          </p>
        </div>

        <article
          v-for="item in decisionGateItems"
          :key="item.decisionKey"
          class="decision-card"
          :class="decisionCardClass(item)"
        >
          <div class="decision-card__header">
            <div class="decision-card__identity">
              <p class="decision-card__key">{{ item.key }}</p>
              <h4>{{ item.title }}</h4>
              <p class="decision-card__description">{{ item.description }}</p>
            </div>

            <div class="decision-card__badges">
              <span :class="['badge', statusBadgeClass(item)]">
                {{ statusLabel(item) }}
              </span>
              <span :class="['badge', blockerBadgeClass(item)]">
                {{ item.blocksAutomation ? 'Bloquea automatización' : 'No bloquea automatización' }}
              </span>
            </div>
          </div>

          <dl class="decision-card__facts">
            <div>
              <dt>Propuesta por defecto</dt>
              <dd>{{ item.defaultProposal }}</dd>
            </div>
            <div>
              <dt>Estado actual</dt>
              <dd>{{ item.resolutionState === 'resolved' ? 'Resuelta' : 'Pendiente' }}</dd>
            </div>
            <div v-if="item.currentResolution">
              <dt>Resolución actual</dt>
              <dd>{{ item.currentResolution }}</dd>
            </div>
          </dl>

          <div v-if="item.resolutionState === 'resolved'" class="decision-card__resolution">
            <p class="decision-card__resolution-label">Resolución registrada</p>
            <p class="muted">
              <strong>Motivo:</strong> {{ item.reason ?? item.resolvedReason ?? 'Sin motivo guardado' }}
            </p>
            <p class="muted" v-if="item.actor">
              <strong>Actor:</strong> {{ item.actor }}
            </p>
            <p class="muted" v-if="item.source">
              <strong>Fuente:</strong> {{ item.source }}
            </p>
            <p class="muted" v-if="item.resolvedAt">
              <strong>Fecha:</strong> {{ item.resolvedAt }}
            </p>
            <p class="muted" v-if="item.configurationValue !== undefined && item.configurationValue !== null">
              <strong>Valor de configuración:</strong> {{ item.configurationValue }}
            </p>
          </div>

          <form v-else class="decision-form" @submit.prevent="submitDecision(item)">
            <div class="form-grid">
              <label class="field" :for="`reason-${item.decisionKey}`">
                <span>Motivo</span>
                <textarea
                  :id="`reason-${item.decisionKey}`"
                  v-model="getDraft(item.decisionKey).reason"
                  placeholder="Explica por qué se ha elegido esta resolución"
                  rows="4"
                  @input="clearDraftError(item.decisionKey)"
                />
                <small class="field__hint">Obligatorio para dejar rastro auditable.</small>
                <small v-if="draftErrors[item.decisionKey]" class="field__error">{{ draftErrors[item.decisionKey] }}</small>
              </label>

              <label class="field" :for="`config-${item.decisionKey}`">
                <span>Valor de configuración opcional</span>
                <input
                  :id="`config-${item.decisionKey}`"
                  v-model="getDraft(item.decisionKey).configurationValue"
                  type="text"
                  placeholder="Ej.: 20 unidades"
                />
                <small class="field__hint">Solo si la decisión necesita un parámetro visible.</small>
              </label>

              <label class="field" :for="`actor-${item.decisionKey}`">
                <span>Actor</span>
                <input
                  :id="`actor-${item.decisionKey}`"
                  v-model="getDraft(item.decisionKey).actor"
                  type="text"
                  placeholder="Director de juego"
                />
                <small class="field__hint">Se guarda para la trazabilidad auditable.</small>
              </label>

              <label class="field" :for="`source-${item.decisionKey}`">
                <span>Fuente</span>
                <input
                  :id="`source-${item.decisionKey}`"
                  v-model="getDraft(item.decisionKey).source"
                  type="text"
                  placeholder="Decisión manual de campaña"
                />
                <small class="field__hint">Describe de dónde sale la resolución.</small>
              </label>
            </div>

            <div class="decision-form__actions">
              <button class="button" type="submit" :disabled="rulesStore.savingDecisionKey === item.decisionKey">
                {{ rulesStore.savingDecisionKey === item.decisionKey ? 'Guardando…' : 'Guardar resolución' }}
              </button>
            </div>
          </form>
        </article>
      </section>

      <section v-if="auditTrail.length > 0" class="rules-audit" aria-labelledby="audit-trail-title">
        <div class="rules-summary__header">
          <h3 id="audit-trail-title">Historial auditable</h3>
          <p class="muted">
            Cada resolución queda como registro inmutable con actor, fuente, versión y momento de resolución.
          </p>
        </div>

        <ul class="rules-audit__list">
          <li
            v-for="entry in auditTrail"
            :key="`${entry.ruleSetVersionId}-${entry.decisionKey}-${entry.resolvedAt}`"
            class="rules-audit__item"
          >
            <strong>{{ entry.decisionKey }} · {{ entry.decisionTitle }}</strong>
            <p class="muted">
              {{ entry.entryType }} · {{ entry.operationType }} · {{ entry.subjectType }}
              <span v-if="entry.subjectId">· {{ entry.subjectId }}</span>
            </p>
            <p class="muted">{{ entry.currentResolution }}</p>
            <p class="muted">{{ entry.reason }}</p>
            <p class="muted">
              {{ entry.actor }} · {{ entry.source }} · {{ entry.ruleSetVersionId }} · {{ entry.resolvedAt }}
            </p>
          </li>
        </ul>
      </section>
    </section>
  </AppShell>
</template>

<script setup lang="ts">
import { computed, reactive, watch } from 'vue';
import AppShell from '@/layouts/AppShell.vue';
import { useCampaignRulesStore } from '../stores';
import type { DecisionGateItem, ResolveDecisionGateRequest } from '../types';

interface CampaignRulesViewProps {
  campaignId?: string;
}

interface DecisionDraft {
  reason: string;
  configurationValue: string;
  actor: string;
  source: string;
}

const props = defineProps<CampaignRulesViewProps>();
const campaignId = computed(() => props.campaignId ?? 'demo');
const rulesStore = useCampaignRulesStore();

const decisionDrafts = reactive<Record<string, DecisionDraft>>({});
const draftErrors = reactive<Record<string, string>>({});

const decisionGateItems = computed(() => rulesStore.decisionGateItems);
const unresolvedBlockers = computed(() => rulesStore.unresolvedBlockers);
const auditTrail = computed(() => rulesStore.auditTrail);
const resolvedCount = computed(
  () => decisionGateItems.value.filter((item) => item.resolutionState === 'resolved').length,
);

function getDraft(key: string): DecisionDraft {
  decisionDrafts[key] ??= {
    reason: '',
    configurationValue: '',
    actor: 'Director de juego',
    source: 'Decisión manual de campaña',
  };

  return decisionDrafts[key];
}

function clearDraftError(key: string) {
  draftErrors[key] = '';
}

function statusLabel(item: DecisionGateItem): string {
  return item.resolutionState === 'resolved' ? 'Resuelta' : 'Pendiente';
}

function statusBadgeClass(item: DecisionGateItem): string {
  return item.resolutionState === 'resolved' ? 'badge--success' : 'badge--warning';
}

function blockerBadgeClass(item: DecisionGateItem): string {
  return item.blocksAutomation ? 'badge--danger' : 'badge--neutral';
}

function decisionCardClass(item: DecisionGateItem): string[] {
  return [
    item.resolutionState === 'resolved' ? 'decision-card--resolved' : 'decision-card--unresolved',
    item.blocksAutomation ? 'decision-card--blocking' : 'decision-card--permissive',
  ];
}

async function submitDecision(item: DecisionGateItem) {
  const draft = getDraft(item.decisionKey);
  const reason = draft.reason.trim();

  if (!reason) {
    draftErrors[item.decisionKey] = 'Escribe un motivo antes de guardar la resolución.';
    return;
  }

  const request: ResolveDecisionGateRequest = {
    decisionKey: item.decisionKey,
    reason,
  };

  const configurationValue = draft.configurationValue.trim();
  if (configurationValue) {
    request.configurationValue = configurationValue;
  }

  const actor = draft.actor.trim();
  if (actor) {
    request.actor = actor;
  }

  const source = draft.source.trim();
  if (source) {
    request.source = source;
  }

  try {
    await rulesStore.resolveDecision(campaignId.value, request);

    decisionDrafts[item.decisionKey] = {
      reason: '',
      configurationValue: '',
      actor: 'Director de juego',
      source: 'Decisión manual de campaña',
    };
    draftErrors[item.decisionKey] = '';
  } catch {
    // The store already surfaced the error message; keep the local draft intact.
  }
}

watch(
  campaignId,
  async (nextCampaignId) => {
    try {
      await rulesStore.loadCampaignRules(nextCampaignId);
    } catch {
      // The store already captured the error state; keep the view mounted.
    }
  },
  { immediate: true },
);
</script>
