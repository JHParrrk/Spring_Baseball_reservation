<template>
  <div class="page">
    <div class="page-header">
      <h1>내 예약 내역</h1>
    </div>

    <div v-if="loading" class="center-msg">불러오는 중...</div>
    <div v-else-if="error" class="center-msg error">{{ error }}</div>

    <div v-else-if="reservations.length === 0" class="center-msg">
      예약 내역이 없습니다.
    </div>

    <div v-else class="reservation-list">
      <div
        v-for="res in reservations"
        :key="res.id"
        class="reservation-card"
        :class="{ 'is-checked': checkedIds.includes(res.id) }"
      >
        <div class="card-top">
          <div class="card-top-left">
            <input
              v-if="res.status === 'PENDING'"
              type="checkbox"
              class="res-checkbox"
              :checked="checkedIds.includes(res.id)"
              @change="toggleCheck(res.id)"
            />
            <div>
              <h2 class="match-title">{{ res.matchTitle }}</h2>
              <div class="meta">
                <span>📅 {{ formatDate(res.matchDate) }}</span>
                <span>🏟️ {{ res.stadiumName }}</span>
              </div>
            </div>
          </div>
          <span class="res-status" :class="res.status.toLowerCase()">
            {{ statusLabel(res.status) }}
            <span v-if="res.status === 'PENDING'" class="spinner">⟳</span>
          </span>
        </div>

        <div class="card-body">
          <div class="seat-info">
            <span
              >좌석 <strong>{{ res.seatNumber }}</strong></span
            >
            <span
              >등급 <strong>{{ res.tier }}</strong></span
            >
            <span
              >금액
              <strong class="price"
                >{{ formatPrice(res.price) }}원</strong
              ></span
            >
          </div>
          <div class="created-at">예약일: {{ formatDate(res.createdAt) }}</div>
        </div>

        <div class="card-actions">
          <button
            v-if="res.status === 'CONFIRMED'"
            class="btn cancel"
            :disabled="actionId === res.id"
            @click="cancel(res.id)"
          >
            {{ actionId === res.id ? "처리 중..." : "예약 취소" }}
          </button>
          <button
            v-if="res.status === 'CANCELLED'"
            class="btn delete"
            :disabled="actionId === res.id"
            @click="deleteRes(res.id)"
          >
            {{ actionId === res.id ? "처리 중..." : "삭제" }}
          </button>
          <span v-if="res.status === 'PENDING'" class="pending-msg">
            체크박스를 선택하여 결제하세요.
          </span>
        </div>
      </div>
    </div>

    <!-- 결제 하단 바 -->
    <div v-if="checkedIds.length > 0" class="pay-bar">
      <span class="pay-bar-info"
        >선택한 <strong>{{ checkedIds.length }}매</strong> · 합계
        <strong>{{ formatPrice(checkedTotalPrice) }}원</strong></span
      >
      <button class="btn pay" @click="showPaymentModal = true">
        선택 결제하기
      </button>
    </div>

    <!-- 결제 성공/실패 toast -->
    <div v-if="payResult" class="toast" :class="payResult.type">
      {{ payResult.message }}
    </div>

    <PaymentModal
      v-if="showPaymentModal"
      :reservations="checkedReservations"
      :submitting="paySubmitting"
      @close="showPaymentModal = false"
      @confirm="pay"
    />

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
import { ref, computed, onMounted } from "vue";
import { reservationApi } from "@/api";
import PaymentModal from "@/components/PaymentModal.vue";
import type { ReservationResponse, ReservationStatus } from "@/api/types";

const reservations = ref<ReservationResponse[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);
const currentPage = ref(0);
const totalPages = ref(1);
const actionId = ref<number | null>(null);

const checkedIds = ref<number[]>([]);
const showPaymentModal = ref(false);
const paySubmitting = ref(false);
const payResult = ref<{ type: "success" | "error"; message: string } | null>(
  null,
);

const checkedReservations = computed(() =>
  reservations.value.filter((r) => checkedIds.value.includes(r.id)),
);

const checkedTotalPrice = computed(() =>
  checkedReservations.value.reduce((sum, r) => sum + r.price, 0),
);

function toggleCheck(id: number): void {
  const idx = checkedIds.value.indexOf(id);
  if (idx >= 0) {
    checkedIds.value.splice(idx, 1);
  } else {
    checkedIds.value.push(id);
  }
}

async function loadPage(page: number): Promise<void> {
  loading.value = true;
  error.value = null;
  checkedIds.value = [];
  try {
    const res = await reservationApi.getMyReservations(page);
    reservations.value = res.data.content;
    currentPage.value = res.data.number;
    totalPages.value = res.data.totalPages;
  } catch {
    error.value = "예약 내역을 불러오지 못했습니다.";
  } finally {
    loading.value = false;
  }
}

async function pay(cvc: string): Promise<void> {
  if (checkedIds.value.length === 0) return;
  paySubmitting.value = true;
  payResult.value = null;
  try {
    await reservationApi.pay([...checkedIds.value], cvc);
    showPaymentModal.value = false;
    checkedIds.value = [];
    payResult.value = {
      type: "success",
      message: "결제 요청 완료! 처리 결과는 잠시 후 확인하세요. ✅",
    };
    await loadPage(currentPage.value);
    setTimeout(() => (payResult.value = null), 5000);
  } catch {
    payResult.value = {
      type: "error",
      message: "결제 요청에 실패했습니다. 다시 시도해주세요.",
    };
  } finally {
    paySubmitting.value = false;
  }
}

async function cancel(id: number): Promise<void> {
  if (!confirm("예약을 취소하시겠습니까?")) return;
  actionId.value = id;
  try {
    await reservationApi.cancel(id);
    await loadPage(currentPage.value);
  } catch {
    alert("취소 처리에 실패했습니다.");
  } finally {
    actionId.value = null;
  }
}

async function deleteRes(id: number): Promise<void> {
  if (!confirm("예약 내역을 삭제하시겠습니까?")) return;
  actionId.value = id;
  try {
    await reservationApi.delete(id);
    await loadPage(currentPage.value);
  } catch {
    alert("삭제에 실패했습니다.");
  } finally {
    actionId.value = null;
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

const STATUS_MAP: Partial<Record<ReservationStatus, string>> = {
  PENDING: "결제 대기 중",
  CONFIRMED: "예약 완료",
  CANCELLED: "취소됨",
};
function statusLabel(status: ReservationStatus): string {
  return STATUS_MAP[status] ?? status;
}

onMounted(() => void loadPage(0));
</script>

<style scoped>
.page {
  max-width: 800px;
  margin: 0 auto;
  padding: 32px 20px;
}

.page-header h1 {
  font-size: 1.8rem;
  font-weight: 700;
  color: #1565c0;
  margin-bottom: 24px;
}

.reservation-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.reservation-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px 24px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.07);
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 14px;
}

.match-title {
  font-size: 1.05rem;
  font-weight: 700;
  margin-bottom: 6px;
}

.meta {
  display: flex;
  gap: 16px;
  font-size: 0.85rem;
  color: #666;
}

.res-status {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.78rem;
  font-weight: 700;
  white-space: nowrap;
}

.res-status.confirmed {
  background: #e8f5e9;
  color: #2e7d32;
}

.res-status.cancelled {
  background: #f5f5f5;
  color: #999;
}

.res-status.pending {
  background: #fff8e1;
  color: #f57f17;
}

.spinner {
  display: inline-block;
  animation: spin 1.5s linear infinite;
  margin-left: 4px;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.pending-msg {
  font-size: 0.82rem;
  color: #f57f17;
}

.card-body {
  border-top: 1px solid #f0f0f0;
  padding-top: 14px;
  margin-bottom: 14px;
}

.seat-info {
  display: flex;
  gap: 24px;
  font-size: 0.9rem;
  color: #444;
  margin-bottom: 8px;
}

.price {
  color: #c62828;
}

.created-at {
  font-size: 0.8rem;
  color: #aaa;
}

.card-actions {
  display: flex;
  gap: 10px;
}

.btn {
  padding: 7px 18px;
  border-radius: 6px;
  font-size: 0.88rem;
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: opacity 0.15s;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn.cancel {
  background: #fff3e0;
  color: #e65100;
  border: 1px solid #ffcc80;
}

.btn.cancel:hover:not(:disabled) {
  background: #ffe0b2;
}

.btn.delete {
  background: #ffebee;
  color: #c62828;
  border: 1px solid #ef9a9a;
}

.btn.delete:hover:not(:disabled) {
  background: #ffcdd2;
}

.btn.pay {
  background: #1565c0;
  color: #fff;
  border: none;
  font-size: 1rem;
  font-weight: 700;
}

.btn.pay:hover {
  background: #0d47a1;
}

/* 체크된 카드 강조 */
.reservation-card.is-checked {
  border: 2px solid #1565c0;
  box-shadow: 0 2px 12px rgba(21, 101, 192, 0.18);
}

/* 카드 상단 좌측 — 체크박스 + 제목 */
.card-top-left {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.res-checkbox {
  width: 18px;
  height: 18px;
  margin-top: 4px;
  accent-color: #1565c0;
  cursor: pointer;
  flex-shrink: 0;
}

/* 결제 하단 바 */
.pay-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #1565c0;
  color: #fff;
  padding: 14px 28px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.15);
  z-index: 50;
}

.pay-bar-info {
  font-size: 1rem;
}

.pay-bar-info strong {
  font-size: 1.1rem;
}

/* 결제 결과 toast */
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
