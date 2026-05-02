<template>
  <div class="my-reservations-page">
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
          <template v-if="res.status === 'CONFIRMED'">
            <button
              v-if="confirmingId !== res.id"
              class="ui-btn ui-btn-reservation-cancel"
              :disabled="actionId === res.id"
              @click="confirmingId = res.id"
            >
              예약 취소
            </button>
            <span v-else class="confirm-inline">
              취소?
              <button
                class="ui-btn ui-btn-reservation-cancel"
                @click="cancel(res.id)"
              >
                확인
              </button>
              <button class="btn-xs-neutral" @click="confirmingId = null">
                아니요
              </button>
            </span>
          </template>
          <template v-if="res.status === 'CANCELLED'">
            <button
              v-if="confirmingId !== res.id"
              class="ui-btn ui-btn-delete"
              :disabled="actionId === res.id"
              @click="confirmingId = res.id"
            >
              삭제
            </button>
            <span v-else class="confirm-inline">
              삭제?
              <button class="ui-btn ui-btn-delete" @click="deleteRes(res.id)">
                확인
              </button>
              <button class="btn-xs-neutral" @click="confirmingId = null">
                아니요
              </button>
            </span>
          </template>
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
      <button class="ui-btn ui-btn-pay" @click="showPaymentModal = true">
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
import "./MyReservationsView.css";

const reservations = ref<ReservationResponse[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);
const currentPage = ref(0);
const totalPages = ref(1);
const actionId = ref<number | null>(null);
const confirmingId = ref<number | null>(null);

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
  confirmingId.value = null;
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
    await loadPage(currentPage.value);
    payResult.value = {
      type: "success",
      message: "결제 요청 완료! 처리 결과는 잠시 후 확인하세요. ✅",
    };
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
  confirmingId.value = null;
  actionId.value = id;
  try {
    await reservationApi.cancel(id);
    await loadPage(currentPage.value);
  } catch {
    payResult.value = {
      type: "error",
      message: "취소 처리에 실패했습니다.",
    };
  } finally {
    actionId.value = null;
  }
}

async function deleteRes(id: number): Promise<void> {
  confirmingId.value = null;
  actionId.value = id;
  try {
    await reservationApi.delete(id);
    await loadPage(currentPage.value);
  } catch {
    payResult.value = {
      type: "error",
      message: "삭제에 실패했습니다.",
    };
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
