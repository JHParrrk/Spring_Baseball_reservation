<template>
  <div class="modal-overlay" @click.self="requestClose">
    <div
      ref="modalRef"
      class="modal"
      role="dialog"
      aria-modal="true"
      aria-labelledby="payment-modal-title"
      aria-describedby="payment-modal-description"
    >
      <h2 id="payment-modal-title" class="modal-title">결제 정보 입력</h2>

      <div class="price-summary">
        <div v-for="r in reservations" :key="r.id" class="seat-row">
          <span class="seat-label">{{ r.seatNumber }} ({{ r.tier }})</span>
          <span class="seat-price">{{ formatPrice(r.price) }}원</span>
        </div>
        <div v-if="reservations.length > 1" class="total-row">
          <span>합계 {{ reservations.length }}매</span>
          <strong class="price">{{ formatPrice(totalPrice) }}원</strong>
        </div>
        <div v-else class="total-row single">
          <strong class="price">{{ formatPrice(totalPrice) }}원</strong>
        </div>
      </div>

      <p id="payment-modal-description" class="sr-only">
        결제용 CVC 세 자리를 입력하고 결제를 진행하세요.
      </p>

      <div class="form-group">
        <label for="payment-cvc">CVC</label>
        <!-- TODO(security): 실서비스에서는 CVC 입력을 자체 UI에서 받지 말고 Stripe.js 같은 SDK 카드 엘리먼트로 대체하세요. -->
        <input
          id="payment-cvc"
          v-model="cvc"
          placeholder="000"
          maxlength="3"
          inputmode="numeric"
          autocomplete="cc-csc"
          :aria-invalid="!isValid && cvc.length > 0"
          aria-describedby="payment-cvc-hint"
          @input="cvc = cvc.replace(/\D/g, '')"
        />
      </div>

      <p id="payment-cvc-hint" class="test-hint">
        <!-- TODO(security): 아래 테스트 규칙은 데모용입니다. 운영에서는 결제사 토큰 응답으로 성공/실패를 판단해야 합니다. -->
        🧪 테스트 모드: CVC 마지막 자리 <strong>4~9</strong> → 결제 성공,
        <strong>0~3</strong> → 결제 실패
      </p>

      <div class="modal-actions">
        <button
          class="ui-btn-cancel"
          :disabled="submitting"
          @click="requestClose"
        >
          취소
        </button>
        <button
          class="ui-btn-confirm"
          :disabled="!isValid || submitting"
          :aria-disabled="!isValid || submitting"
          :aria-busy="submitting"
          @click="submit"
        >
          {{ submitting ? "처리 중..." : "결제하기" }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, onMounted, onUnmounted } from "vue";
import { formatPrice } from "@/utils/format";

interface PaymentItem {
  id: number;
  seatNumber: string;
  tier: string;
  price: number;
}

const props = defineProps<{
  reservations: PaymentItem[];
  submitting: boolean;
}>();

const emit = defineEmits<{
  close: [];
  confirm: [cvc: string];
}>();

const modalRef = ref<HTMLElement | null>(null);
const cvc = ref("");
let prevFocusedElement: HTMLElement | null = null;

const totalPrice = computed(() =>
  props.reservations.reduce((sum, r) => sum + r.price, 0),
);

const isValid = computed(() => /^\d{3}$/.test(cvc.value));

function submit(): void {
  if (!isValid.value) return;
  emit("confirm", cvc.value);
}

function requestClose(): void {
  if (props.submitting) return;
  emit("close");
}

function getFocusableElements(): HTMLElement[] {
  if (!modalRef.value) return [];

  return Array.from(
    modalRef.value.querySelectorAll<HTMLElement>(
      "button:not([disabled]), input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [href], [tabindex]:not([tabindex='-1'])",
    ),
  );
}

function handleKeydown(e: KeyboardEvent): void {
  if (e.key === "Escape") {
    e.preventDefault();
    requestClose();
    return;
  }

  if (e.key !== "Tab") return;

  const focusables = getFocusableElements();
  if (focusables.length === 0) return;

  const first = focusables[0];
  const last = focusables[focusables.length - 1];
  const active = document.activeElement as HTMLElement | null;

  if (e.shiftKey) {
    if (!active || active === first || !modalRef.value?.contains(active)) {
      e.preventDefault();
      last.focus();
    }
    return;
  }

  if (!active || active === last || !modalRef.value?.contains(active)) {
    e.preventDefault();
    first.focus();
  }
}

onMounted(async () => {
  prevFocusedElement =
    document.activeElement instanceof HTMLElement
      ? document.activeElement
      : null;

  document.addEventListener("keydown", handleKeydown);

  await nextTick();
  const [first] = getFocusableElements();
  first?.focus();
});

onUnmounted(() => {
  document.removeEventListener("keydown", handleKeydown);
  prevFocusedElement?.focus();
});
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  width: 100%;
  max-width: 420px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.18);
}

.modal-title {
  font-size: 1.3rem;
  font-weight: 700;
  color: #1565c0;
  margin-bottom: 20px;
}

.price-summary {
  background: #f0f4ff;
  border-radius: 8px;
  padding: 12px 16px;
  margin-bottom: 24px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.seat-row {
  display: flex;
  justify-content: space-between;
  font-size: 0.9rem;
  color: #555;
}

.seat-price {
  font-weight: 600;
  color: #333;
}

.total-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid #c5cae9;
  padding-top: 8px;
  margin-top: 4px;
  font-size: 0.9rem;
  color: #333;
}

.total-row.single {
  border-top: none;
  padding-top: 0;
  margin-top: 0;
  justify-content: flex-end;
}

.seat-label {
  font-size: 0.95rem;
  color: #555;
}

.price {
  font-size: 1.2rem;
  color: #1565c0;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 16px;
}

.form-group label {
  font-size: 0.85rem;
  font-weight: 600;
  color: #333;
}

.form-group input {
  border: 1.5px solid #ddd;
  border-radius: 8px;
  padding: 10px 12px;
  font-size: 1rem;
  outline: none;
  transition: border-color 0.2s;
}

.form-group input:focus {
  border-color: #1565c0;
}

.test-hint {
  font-size: 0.8rem;
  color: #888;
  background: #fffbea;
  border: 1px solid #ffe082;
  border-radius: 6px;
  padding: 8px 12px;
  margin-bottom: 24px;
}

.modal-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.modal button:focus-visible,
.modal input:focus-visible {
  outline: 3px solid rgba(21, 101, 192, 0.35);
  outline-offset: 2px;
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}
</style>
