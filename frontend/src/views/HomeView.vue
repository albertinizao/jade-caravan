<template>
  <AppShell>
    <section class="page">
      <h2>Selector de campaña</h2>
      <p>
        Esta base ya separa vistas, módulos, API, router y stores para crecer sin mezclar reglas
        con presentación.
      </p>
      <RouterLink class="button" to="/campaigns/demo/dashboard">Abrir tablero demo</RouterLink>
    </section>
  </AppShell>
</template>

<script setup lang="ts">
import { RouterLink } from 'vue-router';
import AppShell from '@/layouts/AppShell.vue';
</script>
