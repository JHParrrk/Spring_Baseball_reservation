<template>
  <nav v-if="totalPages > 1" class="pagination" aria-label="페이지 이동">
    <button
      type="button"
      :disabled="loading || !canPrev"
      :aria-disabled="loading || !canPrev"
      aria-label="이전 페이지"
      @click="emit('prev')"
    >
      이전
    </button>
    <span aria-live="polite" aria-atomic="true"
      >{{ currentPage + 1 }} / {{ totalPages }}</span
    >
    <button
      type="button"
      :disabled="loading || !canNext"
      :aria-disabled="loading || !canNext"
      aria-label="다음 페이지"
      @click="emit('next')"
    >
      다음
    </button>
  </nav>
</template>

<script setup lang="ts">
const { loading, currentPage, totalPages, canPrev, canNext } = defineProps<{
  loading: boolean;
  currentPage: number;
  totalPages: number;
  canPrev: boolean;
  canNext: boolean;
}>();

const emit = defineEmits<{
  prev: [];
  next: [];
}>();
</script>
