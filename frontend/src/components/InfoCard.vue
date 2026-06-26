<template>
  <article class="info-card">
    <p class="info-card__label">{{ label }}</p>
    <strong class="info-card__value">{{ value }}</strong>
    <p v-if="helpText" class="info-card__help">{{ helpText }}</p>
  </article>
</template>

<script setup lang="ts">
interface InfoCardProps {
  label: string;
  value: string;
  helpText?: string;
}

defineProps<InfoCardProps>();
</script>
