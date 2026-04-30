<template>
  <div class="page">
    <div class="page-header">
      <h1>경기 목록</h1>
      <p class="sub">예매 가능한 경기를 선택하세요</p>
    </div>

    <div v-if="loading" class="center-msg">불러오는 중...</div>
    <div v-else-if="error" class="center-msg error">{{ error }}</div>

    <div v-else class="match-grid">
      <RouterLink
        v-for="match in matches"
        :key="match.id"
        :to="`/matches/${match.id}`"
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

    <div v-if="!loading && matches.length === 0" class="center-msg">
      현재 예매 가능한 경기가 없습니다.
    </div>

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
import type { MatchResponse, MatchStatus } from "@/api/types";

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

<style scoped>
.page {
  max-width: 1100px;
  margin: 0 auto;
  padding: 32px 20px;
}

.page-header {
  margin-bottom: 28px;
}

.page-header h1 {
  font-size: 1.8rem;
  font-weight: 700;
  color: #1565c0;
}

.page-header .sub {
  color: #666;
  margin-top: 6px;
}

.match-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.match-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.07);
  text-decoration: none;
  color: inherit;
  transition:
    transform 0.15s,
    box-shadow 0.15s;
  display: block;
}

.match-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
}

.match-status {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 0.78rem;
  font-weight: 700;
  margin-bottom: 12px;
}

.match-status.on_sale {
  background: #e8f5e9;
  color: #2e7d32;
}

.match-status.upcoming {
  background: #e3f2fd;
  color: #1565c0;
}

.match-title {
  font-size: 1.1rem;
  font-weight: 700;
  margin-bottom: 14px;
  line-height: 1.4;
}

.match-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 0.88rem;
  color: #555;
}

.center-msg {
  text-align: center;
  padding: 60px;
  color: #888;
}

.center-msg.error {
  color: #c62828;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 32px;
}

.pagination button {
  padding: 8px 20px;
  background: #1565c0;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
}

.pagination button:disabled {
  background: #bbb;
  cursor: not-allowed;
}

.pagination span {
  font-size: 0.95rem;
  color: #555;
}
</style>
