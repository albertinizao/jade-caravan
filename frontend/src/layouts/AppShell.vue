<template>
  <div class="app-shell">
    <header class="app-shell__header">
      <div class="app-shell__brand">
        <p class="eyebrow">Jade Caravan</p>
        <h1>Gestión de caravana</h1>
        <p v-if="campaignId" class="app-shell__campaign">
          Campaña activa: <strong>{{ campaignId }}</strong>
        </p>
      </div>
      <nav class="app-shell__nav" aria-label="Navegación principal">
        <RouterLink to="/" class="app-shell__nav-link">Inicio</RouterLink>
        <RouterLink
          v-for="item in navigationItems"
          :key="item.to"
          :to="item.to"
          class="app-shell__nav-link"
        >
          {{ item.label }}
        </RouterLink>
      </nav>
    </header>

    <main class="app-shell__content">
      <slot />
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, watchEffect } from 'vue';
import { RouterLink } from 'vue-router';
import { useCampaignStore } from '@/modules/campaign/stores';

interface NavigationItem {
  label: string;
  to: string;
}

const campaignStore = useCampaignStore();

watchEffect(() => {
  campaignStore.restoreCampaignSession();
});

const campaignId = computed(() => campaignStore.campaignId);

const navigationItems = computed<NavigationItem[]>(() => {
  const activeCampaignId = campaignId.value;
  if (!activeCampaignId) {
    return [];
  }

  return [
    { label: 'Tablero', to: `/campaigns/${activeCampaignId}/dashboard` },
    { label: 'Ciclo diario', to: `/campaigns/${activeCampaignId}/day-cycle` },
    { label: 'Carros', to: `/campaigns/${activeCampaignId}/carts` },
    { label: 'Viajeros', to: `/campaigns/${activeCampaignId}/travellers` },
    { label: 'Inventario', to: `/campaigns/${activeCampaignId}/inventory` },
    { label: 'Bestias', to: `/campaigns/${activeCampaignId}/beasts` },
    { label: 'Comercio', to: `/campaigns/${activeCampaignId}/commerce` },
    { label: 'Historial', to: `/campaigns/${activeCampaignId}/history` },
    { label: 'Reglas', to: `/campaigns/${activeCampaignId}/rules` },
  ];
});
</script>
