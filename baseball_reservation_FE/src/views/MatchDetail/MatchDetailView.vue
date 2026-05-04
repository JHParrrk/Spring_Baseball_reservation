<template>
  <div class="match-detail-page" :aria-busy="loading">
    <RouterLink to="/" class="back-link">← 경기 목록으로</RouterLink>

    <div v-if="loading" class="center-msg" aria-live="polite">
      불러오는 중...
    </div>
    <div v-else-if="error" class="center-msg error" role="alert">
      {{ error }}
    </div>

    <template v-else>
      <div class="match-header">
        <span class="match-status" :class="match?.status?.toLowerCase()">
          {{ matchStatusLabel(match?.status) }}
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
        <button
          class="reserve-btn"
          :disabled="reserving || paySubmitting"
          @click="openPaymentModal"
        >
          {{
            reserving
              ? "좌석 선점 중..."
              : paySubmitting
                ? "결제 처리 중..."
                : "예약 및 결제하기"
          }}
        </button>
      </div>

      <div
        v-if="reserveSuccess"
        class="toast success"
        role="status"
        aria-live="polite"
        aria-atomic="true"
      >
        예약 및 결제가 완료되었습니다. ✅
      </div>
      <div
        v-if="reserveError"
        class="toast error"
        role="alert"
        aria-live="assertive"
      >
        {{ reserveError }}
        <RouterLink
          v-if="reservePartial"
          to="/my-reservations"
          class="toast-link"
        >
          내 예약 보기 →
        </RouterLink>
      </div>

      <PaymentModal
        v-if="showPaymentModal"
        :reservations="pendingReservations"
        :submitting="paySubmitting"
        @close="cancelPendingAndClose"
        @confirm="pay"
      />
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRoute } from "vue-router";
import { matchApi } from "@/api";
import SeatGrid from "@/components/SeatGrid.vue";
import PaymentModal from "@/components/PaymentModal.vue";
import type { MatchResponse, SeatResponse } from "@/api/types";
import { useSeatBooking } from "@/composables/useSeatBooking";
import { formatDate, formatPrice } from "@/utils/format";
import { matchStatusLabel } from "@/utils/statusLabel";
import "./MatchDetailView.css";

const route = useRoute();
const matchId = Number(route.params["id"]);

const match = ref<MatchResponse | null>(null);
const seats = ref<SeatResponse[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);

async function loadData(): Promise<void> {
  if (!Number.isInteger(matchId) || matchId <= 0) {
    error.value = "잘못된 경기 ID입니다.";
    loading.value = false;
    return;
  }

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

const {
  selectedSeats,
  reserving,
  showPaymentModal,
  paySubmitting,
  pendingReservations,
  totalPrice,
  feedbackSuccess: reserveSuccess,
  feedbackError: reserveError,
  feedbackPartial: reservePartial,
  onSelectSeat,
  openPaymentModal,
  cancelPendingAndClose,
  pay,
} = useSeatBooking(matchId, loadData);

onMounted(() => void loadData());
</script>
