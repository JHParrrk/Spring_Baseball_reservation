<template>
  <div class="home-page" :aria-busy="loading">
    <div class="page-header">
      <h1>경기 목록</h1>
      <p class="sub">예매 가능한 경기를 선택하세요</p>
    </div>

    <div v-if="loading" class="center-msg" aria-live="polite">
      불러오는 중...
    </div>
    <div v-else-if="error" class="center-msg error" role="alert">
      {{ error }}
    </div>

    <template v-else>
      <div v-if="matches.length === 0" class="center-msg">
        현재 예매 가능한 경기가 없습니다.
      </div>
      <div v-else class="match-grid">
        <div
          v-for="match in matches"
          :key="match.id"
          class="match-card"
          @click="handleMatchClick(match.id)"
          @keydown.enter="handleMatchClick(match.id)"
          @keydown.space.prevent="handleMatchClick(match.id)"
          role="button"
          tabindex="0"
          :aria-label="`${match.title} 상세 페이지로 이동`"
          style="cursor: pointer"
        >
          <div class="match-status" :class="match.status.toLowerCase()">
            {{ matchStatusLabel(match.status) }}
          </div>
          <h2 class="match-title">{{ match.title }}</h2>
          <div class="match-info">
            <span>📅 {{ formatDate(match.matchDate) }}</span>
            <span>🏟️ {{ match.stadiumName }}</span>
          </div>
        </div>
      </div>
    </template>

    <PaginationBar
      :loading="loading"
      :current-page="currentPage"
      :total-pages="totalPages"
      :can-prev="canPrev"
      :can-next="canNext"
      @prev="prevPage"
      @next="nextPage"
    />
  </div>
</template>

<script setup lang="ts">
import { onMounted } from "vue";
import { useRouter } from "vue-router";
import { matchApi } from "@/api";
import PaginationBar from "@/components/PaginationBar.vue";
import { useAuthGuard } from "@/composables/useAuthGuard";
import { usePagedList } from "@/composables/usePagedList";
import { formatDate } from "@/utils/format";
import { matchStatusLabel } from "@/utils/statusLabel";
import "./HomeView.css";

const router = useRouter();
const { requireLogin } = useAuthGuard();

const {
  items: matches,
  loading,
  error,
  currentPage,
  totalPages,
  canPrev,
  canNext,
  loadPage,
  prevPage,
  nextPage,
} = usePagedList(
  (page) => matchApi.getMatches(page),
  "경기 목록을 불러오지 못했습니다.",
);

function handleMatchClick(matchId: number): void {
  requireLogin(() => router.push(`/matches/${matchId}`));
}

onMounted(() => void loadPage(0));
</script>
