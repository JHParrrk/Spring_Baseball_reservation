<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal">
      <h2 class="modal-title">결제 정보 입력</h2>

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

      <div class="form-group">
        <label>카드 번호</label>
        <input
          v-model="cardNumber"
          placeholder="0000 0000 0000 0000"
          maxlength="19"
          inputmode="numeric"
          @input="formatCard"
        />
      </div>

      <div class="form-row">
        <div class="form-group">
          <label>유효기간</label>
          <input
            v-model="expiry"
            placeholder="MM/YY"
            maxlength="5"
            @input="formatExpiry"
          />
        </div>
        <div class="form-group">
          <label>CVC</label>
          <input
            v-model="cvc"
            placeholder="000"
            maxlength="3"
            inputmode="numeric"
            @input="cvc = cvc.replace(/\D/g, '')"
          />
        </div>
      </div>

      <p class="test-hint">
        🧪 테스트 모드: CVC 마지막 자리 <strong>4~9</strong> → 결제 성공,
        <strong>0~3</strong> → 결제 실패
      </p>

      <div class="modal-actions">
        <button
          class="btn-cancel"
          :disabled="submitting"
          @click="$emit('close')"
        >
          취소
        </button>
        <button
          class="btn-confirm"
          :disabled="!isValid || submitting"
          @click="submit"
        >
          {{ submitting ? "처리 중..." : "결제하기" }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import type { ReservationResponse } from "@/api/types";

const props = defineProps<{
  reservations: ReservationResponse[];
  submitting: boolean;
}>();

const emit = defineEmits<{
  close: [];
  confirm: [cvc: string];
}>();

const cardNumber = ref("");
const expiry = ref("");
const cvc = ref("");

const totalPrice = computed(() =>
  props.reservations.reduce((sum, r) => sum + r.price, 0),
);

const isValid = computed(() => /^\d{3}$/.test(cvc.value));

function formatCard(): void {
  const digits = cardNumber.value.replace(/\D/g, "").slice(0, 16);
  cardNumber.value = digits.replace(/(.{4})/g, "$1 ").trim();
}

function formatExpiry(): void {
  const digits = expiry.value.replace(/\D/g, "").slice(0, 4);
  if (digits.length > 2) {
    expiry.value = digits.slice(0, 2) + "/" + digits.slice(2);
  } else {
    expiry.value = digits;
  }
}

function formatPrice(price: number): string {
  return Number(price).toLocaleString("ko-KR");
}

function submit(): void {
  if (!isValid.value) return;
  emit("confirm", cvc.value);
}
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

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
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

.btn-cancel {
  padding: 10px 20px;
  border-radius: 8px;
  border: 1.5px solid #ddd;
  background: #fff;
  color: #555;
  font-size: 0.95rem;
  cursor: pointer;
}

.btn-cancel:hover:not(:disabled) {
  background: #f5f5f5;
}

.btn-confirm {
  padding: 10px 24px;
  border-radius: 8px;
  border: none;
  background: #1565c0;
  color: #fff;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-confirm:hover:not(:disabled) {
  background: #0d47a1;
}

.btn-confirm:disabled,
.btn-cancel:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
