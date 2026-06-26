<template>
  <div class="app-shell">
    <header class="app-shell__header">
      <div>
        <p class="eyebrow">Jade Caravan</p>
        <h1>Base de arquitectura</h1>
      </div>
      <nav class="app-shell__nav" aria-label="Navegación principal">
        <RouterLink to="/">Inicio</RouterLink>
        <RouterLink to="/campaigns/demo/dashboard">Tablero demo</RouterLink>
        <RouterLink to="/campaigns/demo/rules">Reglas demo</RouterLink>
      </nav>
    </header>

    <main class="app-shell__content">
      <slot />
    </main>
  </div>
</template>

<script setup lang="ts">
import { RouterLink } from 'vue-router';
</script>
