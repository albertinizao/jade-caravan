<template>
  <AppShell>
    <section class="page">
      <h2>Tablero de campaña</h2>
      <p>
        Vista de dominio preparada para mostrar el estado calculado de la caravana y sus alertas.
      </p>

      <div class="dashboard-grid">
        <InfoCard label="Campaña" :value="campaignId" helpText="Identificador de la campaña activa" />
        <InfoCard label="Estado" value="Base creada" helpText="Aún sin datos de dominio cargados" />
        <InfoCard label="API" value="Preparada" helpText="Cliente HTTP tipado listo para conectar" />
      </div>

      <p class="dashboard-link">
        <RouterLink :to="{ name: 'campaign-day-cycle', params: { campaignId } }">
          Abrir ciclo diario
        </RouterLink>
      </p>
    </section>
  </AppShell>
</template>

<script setup lang="ts">
import { RouterLink } from 'vue-router';
import AppShell from '@/layouts/AppShell.vue';
import InfoCard from '@/components/InfoCard.vue';

interface CampaignDashboardViewProps {
  campaignId?: string;
}

const props = defineProps<CampaignDashboardViewProps>();
const campaignId = props.campaignId ?? 'demo';
</script>
