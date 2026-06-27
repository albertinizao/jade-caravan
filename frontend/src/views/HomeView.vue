<template>
  <AppShell>
    <section class="page home-page">
      <header class="hero-card">
        <p class="eyebrow">Selector de campaña</p>
        <h2>Abre una campaña y vuelve al estado exacto donde lo dejaste</h2>
        <p class="muted">
          El frontend presenta el estado calculado por el backend: tablero, ciclo diario, carros,
          viajeros, inventario, bestias, comercio, historial y decisiones de reglas.
        </p>
      </header>

      <section class="home-grid">
        <article class="panel">
          <div class="panel__header">
            <div>
              <h3>Campaña activa</h3>
              <p class="muted">Introduce el identificador de campaña que quieres revisar.</p>
            </div>
          </div>

          <form class="form-grid" @submit.prevent="openCampaign">
            <label class="field field--full">
              <span>Identificador de campaña</span>
              <input v-model="campaignIdDraft" type="text" placeholder="demo" autocomplete="off" />
              <small class="field__hint">Se recuerda en tu navegador para volver más rápido.</small>
            </label>
            <div class="field field--inline">
              <span>&nbsp;</span>
              <button class="button" type="submit">Abrir tablero</button>
            </div>
          </form>
        </article>

        <article class="panel">
          <div class="panel__header">
            <div>
              <h3>Campañas recientes</h3>
              <p class="muted">Acceso directo a las últimas campañas abiertas en este navegador.</p>
            </div>
          </div>

          <div v-if="recentCampaignIds.length > 0" class="recent-campaigns">
            <RouterLink
              v-for="recentCampaignId in recentCampaignIds"
              :key="recentCampaignId"
              class="recent-campaigns__item"
              :to="campaignRoute(recentCampaignId)"
              @click="campaignStore.selectCampaign(recentCampaignId)"
            >
              <strong>{{ recentCampaignId }}</strong>
              <span class="muted">Ir al tablero</span>
            </RouterLink>
          </div>
          <p v-else class="muted">
            Todavía no hay campañas recientes. Abre una campaña para registrar el acceso.
          </p>
        </article>
      </section>

      <section class="home-grid">
        <article class="summary-card">
          <p class="summary-card__label">Ciclo diario</p>
          <strong class="summary-card__value">Planificación y cierre</strong>
          <p class="summary-card__meta">
            Revisa el día actual, su preview calculado y los bloqueos antes de cerrar.
          </p>
        </article>
        <article class="summary-card">
          <p class="summary-card__label">Reglas</p>
          <strong class="summary-card__value">Decisiones audibles</strong>
          <p class="summary-card__meta">
            Consulta bloqueadores, resoluciones y el historial de decisiones con trazabilidad.
          </p>
        </article>
        <article class="summary-card">
          <p class="summary-card__label">Dominios</p>
          <strong class="summary-card__value">Carros y recursos</strong>
          <p class="summary-card__meta">
            Las pantallas de carros, viajeros, inventario, bestias, comercio e historial salen del
            mismo estado backend.
          </p>
        </article>
      </section>
    </section>
  </AppShell>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
import AppShell from '@/layouts/AppShell.vue';
import { useCampaignStore } from '@/modules/campaign/stores';

const router = useRouter();
const campaignStore = useCampaignStore();
const campaignIdDraft = ref('demo');

onMounted(() => {
  campaignStore.restoreCampaignSession();
  campaignIdDraft.value = campaignStore.campaignId;
});

const recentCampaignIds = computed(() => campaignStore.recentCampaignIds);

function campaignRoute(campaignId: string) {
  return { name: 'campaign-dashboard', params: { campaignId } };
}

async function openCampaign() {
  const campaignId = campaignIdDraft.value.trim() || 'demo';
  campaignStore.selectCampaign(campaignId);
  await router.push(campaignRoute(campaignId));
}
</script>
