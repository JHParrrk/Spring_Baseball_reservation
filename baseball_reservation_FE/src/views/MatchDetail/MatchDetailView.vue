<template>
  <div class="match-detail-page">
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

      <div v-if="reserveSuccess" class="toast success">
        예약 및 결제가 완료되었습니다. ✅
      </div>
      <div v-if="reserveError" class="toast error">
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
import { ref, computed, onMounted, onUnmounted } from "vue";
import { useRoute } from "vue-router";
import axios from "axios";
import { matchApi, reservationApi } from "@/api";
import SeatGrid from "@/components/SeatGrid.vue";
import PaymentModal from "@/components/PaymentModal.vue";
import type { MatchResponse, MatchStatus, SeatResponse } from "@/api/types";
import "./MatchDetailView.css";

const route = useRoute();
const matchId = route.params["id"] as string;

const match = ref<MatchResponse | null>(null);
const seats = ref<SeatResponse[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);

const selectedSeats = ref<SeatResponse[]>([]);
const reserving = ref(false);
const showPaymentModal = ref(false);
const paySubmitting = ref(false);
const reserveSuccess = ref(false);
const reserveError = ref<string | null>(null);
const reservePartial = ref(false);

// 좌석 선점 후 모달에 넘길 예약 정보
const pendingReservationIds = ref<number[]>([]);
const pendingReservations = ref<
  { id: number; seatNumber: string; tier: string; price: number }[]
>([]);

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

/** 버튼 클릭 시: 즉시 예약(좌석 PENDING 선점) 후 결제 모달 오픈 */
async function openPaymentModal(): Promise<void> {
  if (selectedSeats.value.length === 0) return;
  reserving.value = true;
  reserveError.value = null;
  reserveSuccess.value = false;
  reservePartial.value = false;
  try {
    const createRes = await reservationApi.create(
      selectedSeats.value.map((s) => s.id),
    );
    pendingReservationIds.value = createRes.data.map((r) => r.id);
    pendingReservations.value = createRes.data.map((r) => ({
      id: r.id,
      seatNumber: r.seatNumber,
      tier: r.tier,
      price: r.price,
    }));
    selectedSeats.value = [];
    await loadData(); // 좌석 PENDING 상태 즉시 반영
    showPaymentModal.value = true;
  } catch (e: unknown) {
    const msg = axios.isAxiosError(e)
      ? (e.response?.data as { message?: string })?.message
      : undefined;
    reserveError.value = msg ?? "좌석 선점에 실패했습니다. 다시 시도해주세요.";
    selectedSeats.value = [];
    await loadData();
  } finally {
    reserving.value = false;
  }
}

/** 모달 취소: 선점된 예약을 취소하여 좌석 복원 */
async function cancelPendingAndClose(): Promise<void> {
  showPaymentModal.value = false;
  if (pendingReservationIds.value.length === 0) return;
  try {
    await Promise.all(
      pendingReservationIds.value.map((id) => reservationApi.cancel(id)),
    );
  } catch {
    // 취소 실패 시 5분 후 자동 취소됨 — 무시
  } finally {
    pendingReservationIds.value = [];
    pendingReservations.value = [];
    await loadData();
  }
}

/** 결제하기: 선점된 예약 ID로 결제만 진행 */
async function pay(cvc: string): Promise<void> {
  if (pendingReservationIds.value.length === 0) return;
  paySubmitting.value = true;
  reserveError.value = null;
  try {
    await reservationApi.pay([...pendingReservationIds.value], cvc);
    reserveSuccess.value = true;
    showPaymentModal.value = false;
    pendingReservationIds.value = [];
    pendingReservations.value = [];
    await loadData();
    setTimeout(() => (reserveSuccess.value = false), 5000);
  } catch (e: unknown) {
    const msg = axios.isAxiosError(e)
      ? (e.response?.data as { message?: string })?.message
      : undefined;
    // 결제 실패 — 좌석은 5분간 유지됨
    reservePartial.value = true;
    reserveError.value =
      msg ??
      "결제에 실패했습니다. 좌석은 5분간 확보되어 있습니다. 내 예약에서 재결제하세요.";
    showPaymentModal.value = false;
    pendingReservationIds.value = [];
    pendingReservations.value = [];
    await loadData();
  } finally {
    paySubmitting.value = false;
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

// 페이지 이탈 시 선점 중인 PENDING 예약을 즉시 취소하여 좌석을 복원합니다.
// (백엔드 TTL 5분이 있지만 이탈 즉시 해제하면 다른 사용자가 바로 선택 가능)
// loadData()는 호출하지 않습니다 — unmount된 컴포넌트의 상태에 접근하면 경고가 발생합니다.
onUnmounted(() => {
  if (pendingReservationIds.value.length > 0) {
    void Promise.all(
      pendingReservationIds.value.map((id) => reservationApi.cancel(id)),
    ).catch(() => {
      // 취소 실패 시 백엔드 TTL 5분으로 자동 복원됨
    });
  }
});
</script>
