<template>
  <div class="page">
    <RouterLink to="/" class="back-link">← 경기 목록으로</RouterLink>

    <div v-if="loading" class="center-msg">불러오는 중...</div>
    <div v-else-if="error" class="center-msg error">{{ error }}</div>

    <template v-else>
      <div class="match-header">
        <span class="match-status" :class="match?.status?.toLowerCase()">
          {{ statusLabel(match?.status) }}
        </span>
        <h1>{{ match?.title }}</h1>
        <div class="match-meta">
          <span>📅 {{ formatDate(match?.matchDate) }}</span>
          <span>🏟️ {{ match?.stadiumName }}</span>
        </div>
      </div>

      <SeatGrid
        :seats="seats"
        :selected-seat-ids="selectedSeats.map((s) => s.id)"
        @select="onSelectSeat"
      />

      <div v-if="selectedSeats.length > 0" class="booking-panel">
        <div class="booking-info">
          <strong>선택 좌석 {{ selectedSeats.length }}매</strong>
          <span class="seat-list">{{
            selectedSeats.map((s) => s.seatNumber).join(", ")
          }}</span>
          <span class="price">총 {{ formatPrice(totalPrice) }}원</span>
        </div>
        <button class="reserve-btn" :disabled="reserving" @click="reserve">
          {{ reserving ? "예약 중..." : "예약하기" }}
        </button>
      </div>

      <div v-if="reserveSuccess" class="toast success">
        예약 완료! 내 예약에서 결제하실 수 있습니다. ✅
      </div>
      <div v-if="reserveError" class="toast error">{{ reserveError }}</div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useRoute } from "vue-router";
import axios from "axios";
import { matchApi, reservationApi } from "@/api";
import SeatGrid from "@/components/SeatGrid.vue";
import type { MatchResponse, MatchStatus, SeatResponse } from "@/api/types";

const route = useRoute();
const matchId = route.params["id"] as string;

const match = ref<MatchResponse | null>(null);
const seats = ref<SeatResponse[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);

const selectedSeats = ref<SeatResponse[]>([]);
const reserving = ref(false);
const reserveSuccess = ref(false);
const reserveError = ref<string | null>(null);

const totalPrice = computed(() =>
  selectedSeats.value.reduce((sum, s) => sum + s.price, 0),
);

async function loadData(): Promise<void> {
  loading.value = true;
  error.value = null;
  try {
    const [matchRes, seatsRes] = await Promise.all([
      matchApi.getMatch(matchId),
      matchApi.getAllSeats(matchId),
    ]);
    match.value = matchRes.data;
    seats.value = seatsRes.data;
  } catch {
    error.value = "경기 정보를 불러오지 못했습니다.";
  } finally {
    loading.value = false;
  }
}

function onSelectSeat(seat: SeatResponse): void {
  const idx = selectedSeats.value.findIndex((s) => s.id === seat.id);
  if (idx >= 0) {
    selectedSeats.value.splice(idx, 1);
  } else {
    if (selectedSeats.value.length >= 10) {
      reserveError.value = "최대 10매까지 선택 가능합니다.";
      return;
    }
    selectedSeats.value.push(seat);
  }
  reserveSuccess.value = false;
  reserveError.value = null;
}

async function reserve(): Promise<void> {
  if (selectedSeats.value.length === 0) return;
  reserving.value = true;
  reserveError.value = null;
  reserveSuccess.value = false;
  try {
    await reservationApi.create(selectedSeats.value.map((s) => s.id));
    reserveSuccess.value = true;
    selectedSeats.value = [];
    await loadData();
  } catch (e: unknown) {
    const msg = axios.isAxiosError(e)
      ? (e.response?.data as { message?: string })?.message
      : undefined;
    reserveError.value = msg ?? "예약에 실패했습니다. 다시 시도해주세요.";
  } finally {
    reserving.value = false;
  }
}

function formatDate(isoStr?: string): string {
  if (!isoStr) return "";
  return new Date(isoStr).toLocaleString("ko-KR", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
}

function formatPrice(price: number): string {
  return Number(price).toLocaleString("ko-KR");
}

const STATUS_MAP: Partial<Record<MatchStatus, string>> = {
  UPCOMING: "예정",
  ON_SALE: "예매 중",
  CANCELLED: "취소됨",
  CLOSED: "마감",
};
function statusLabel(status?: MatchStatus): string {
  if (!status) return "";
  return STATUS_MAP[status] ?? status;
}

onMounted(() => void loadData());
</script>

<style scoped>
.page {
  max-width: 900px;
  margin: 0 auto;
  padding: 32px 20px;
}

.back-link {
  display: inline-block;
  color: #1565c0;
  text-decoration: none;
  font-size: 0.9rem;
  margin-bottom: 24px;
}

.back-link:hover {
  text-decoration: underline;
}

.match-header {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.07);
  margin-bottom: 24px;
}

.match-status {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 0.78rem;
  font-weight: 700;
  margin-bottom: 10px;
}

.match-status.on_sale {
  background: #e8f5e9;
  color: #2e7d32;
}

.match-status.upcoming {
  background: #e3f2fd;
  color: #1565c0;
}

.match-header h1 {
  font-size: 1.5rem;
  font-weight: 700;
  margin-bottom: 12px;
}

.match-meta {
  display: flex;
  gap: 20px;
  font-size: 0.9rem;
  color: #555;
}

.booking-panel {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  border-top: 2px solid #1565c0;
  padding: 16px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.1);
  z-index: 50;
}

.booking-info {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 0.95rem;
}

.booking-info strong {
  font-weight: 700;
  color: #1565c0;
  white-space: nowrap;
}

.seat-list {
  font-size: 0.85rem;
  color: #555;
  max-width: 350px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.price {
  font-size: 1.1rem;
  font-weight: 700;
  color: #c62828;
}

.reserve-btn {
  background: #1565c0;
  color: #fff;
  border: none;
  padding: 12px 28px;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 700;
  cursor: pointer;
  transition: background 0.15s;
}

.reserve-btn:hover {
  background: #0d47a1;
}

.reserve-btn:disabled {
  background: #90caf9;
  cursor: not-allowed;
}

.toast {
  position: fixed;
  top: 80px;
  right: 20px;
  padding: 12px 20px;
  border-radius: 8px;
  font-size: 0.95rem;
  font-weight: 600;
  z-index: 200;
  animation: fadeIn 0.2s ease;
}

.toast.success {
  background: #e8f5e9;
  color: #2e7d32;
  border: 1px solid #a5d6a7;
}

.toast.error {
  background: #ffebee;
  color: #c62828;
  border: 1px solid #ef9a9a;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.center-msg {
  text-align: center;
  padding: 60px;
  color: #888;
}

.center-msg.error {
  color: #c62828;
}
</style>
