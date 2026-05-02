<template>
  <div class="home-page">
    <div class="page-header">
      <h1>경기 목록</h1>
      <p class="sub">예매 가능한 경기를 선택하세요</p>
    </div>

    <div v-if="loading" class="center-msg">불러오는 중...</div>
    <div v-else-if="error" class="center-msg error">{{ error }}</div>

    <template v-else>
      <div v-if="matches.length === 0" class="center-msg">
        현재 예매 가능한 경기가 없습니다.
      </div>
      <div v-else class="match-grid">
        <RouterLink
          v-for="match in matches"
          :key="match.id"
          :to="auth.isLoggedIn ? `/matches/${match.id}` : '/login'"
          class="match-card"
        >
          <div class="match-status" :class="match.status.toLowerCase()">
            {{ statusLabel(match.status) }}
          </div>
          <h2 class="match-title">{{ match.title }}</h2>
          <div class="match-info">
            <span>📅 {{ formatDate(match.matchDate) }}</span>
            <span>🏟️ {{ match.stadiumName }}</span>
          </div>
        </RouterLink>
      </div>
    </template>

    <!-- 페이지네이션 -->
    <div v-if="totalPages > 1" class="pagination">
      <button :disabled="currentPage === 0" @click="loadPage(currentPage - 1)">
        이전
      </button>
      <span>{{ currentPage + 1 }} / {{ totalPages }}</span>
      <button
        :disabled="currentPage >= totalPages - 1"
        @click="loadPage(currentPage + 1)"
      >
        다음
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { matchApi } from "@/api";
import { useAuthStore } from "@/stores/auth";
import type { MatchResponse, MatchStatus } from "@/api/types";
import "./HomeView.css";

const auth = useAuthStore();
const matches = ref<MatchResponse[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);
const currentPage = ref(0);
const totalPages = ref(1);

async function loadPage(page: number): Promise<void> {
  loading.value = true;
  error.value = null;
  try {
    const res = await matchApi.getMatches(page);
    matches.value = res.data.content;
    currentPage.value = res.data.number;
    totalPages.value = res.data.totalPages;
  } catch {
    error.value = "경기 목록을 불러오지 못했습니다.";
  } finally {
    loading.value = false;
  }
}

function formatDate(isoStr: string): string {
  const d = new Date(isoStr);
  return d.toLocaleString("ko-KR", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
}

const STATUS_MAP: Partial<Record<MatchStatus, string>> = {
  UPCOMING: "예정",
  ON_SALE: "예매 중",
};
function statusLabel(status: MatchStatus): string {
  return STATUS_MAP[status] ?? status;
}

onMounted(() => void loadPage(0));
</script>
