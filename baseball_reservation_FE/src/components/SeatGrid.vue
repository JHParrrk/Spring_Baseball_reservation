<template>
  <div class="seat-grid" aria-label="좌석 선택 영역">
    <div class="legend" aria-label="좌석 상태 범례">
      <span class="badge available">예매 가능</span>
      <span class="badge pending">선점 중</span>
      <span class="badge reserved">예매 완료</span>
      <span class="badge selected">선택됨</span>
    </div>

    <div v-if="tiers.length === 0" class="empty">좌석 정보가 없습니다.</div>

    <section
      v-for="tier in tiers"
      :key="tier"
      class="tier-section"
      :aria-label="`${tier} 좌석 구역`"
    >
      <h3 class="tier-title">{{ tier }}</h3>
      <div class="seats">
        <button
          v-for="seat in seatsByTier[tier]"
          :key="seat.id"
          class="seat"
          :class="{
            available: seat.status === 'AVAILABLE',
            pending: seat.status === 'PENDING',
            reserved: seat.status === 'RESERVED',
            selected: selectedSeatIds?.includes(seat.id) ?? false,
          }"
          :disabled="seat.status !== 'AVAILABLE'"
          :aria-disabled="seat.status !== 'AVAILABLE'"
          :aria-pressed="selectedSeatIds?.includes(seat.id) ?? false"
          :aria-label="buildSeatAriaLabel(seat)"
          @click="selectSeat(seat)"
        >
          {{ seat.seatNumber }}
        </button>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import type { SeatResponse } from "@/api/types";

const props = defineProps<{
  seats: SeatResponse[];
  selectedSeatIds?: number[];
}>();

const emit = defineEmits<{
  select: [seat: SeatResponse];
}>();

const tiers = computed(() => [...new Set(props.seats.map((s) => s.tier))]);

const seatsByTier = computed(() => {
  const map: Record<string, SeatResponse[]> = {};
  for (const seat of props.seats) {
    if (!map[seat.tier]) map[seat.tier] = [];
    map[seat.tier].push(seat);
  }
  return map;
});

function selectSeat(seat: SeatResponse): void {
  if (seat.status !== "AVAILABLE") return;
  emit("select", seat);
}

function buildSeatAriaLabel(seat: SeatResponse): string {
  const statusLabel =
    seat.status === "AVAILABLE"
      ? "예매 가능"
      : seat.status === "PENDING"
        ? "선점 중"
        : "예매 완료";
  const selectedLabel = selectedSeatIdsIncludes(seat.id) ? ", 선택됨" : "";

  return `${seat.tier} ${seat.seatNumber}, ${seat.price.toLocaleString("ko-KR")}원, ${statusLabel}${selectedLabel}`;
}

function selectedSeatIdsIncludes(seatId: number): boolean {
  return props.selectedSeatIds?.includes(seatId) ?? false;
}
</script>

<style scoped>
.seat-grid {
  margin-top: 16px;
}

.legend {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
}

.badge.available {
  background: #e3f2fd;
  color: #1565c0;
  border: 1px solid #90caf9;
}

.badge.pending {
  background: #fff8e1;
  color: #f57f17;
  border: 1px solid #ffe082;
}

.badge.reserved {
  background: #f5f5f5;
  color: #999;
  border: 1px solid #ddd;
}

.badge.selected {
  background: #e8f5e9;
  color: #2e7d32;
  border: 1px solid #a5d6a7;
}

.tier-section {
  margin-bottom: 24px;
}

.tier-title {
  font-size: 0.9rem;
  font-weight: 700;
  color: #555;
  margin-bottom: 10px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.seats {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.seat {
  width: 56px;
  height: 40px;
  border-radius: 6px;
  font-size: 0.75rem;
  font-weight: 600;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.15s;
}

.seat.available {
  background: #e3f2fd;
  border-color: #90caf9;
  color: #1565c0;
}

.seat.available:hover {
  background: #bbdefb;
}

.seat.pending {
  background: #fff8e1;
  border-color: #ffe082;
  color: #f57f17;
  cursor: not-allowed;
}

.seat.reserved {
  background: #f5f5f5;
  border-color: #e0e0e0;
  color: #bbb;
  cursor: not-allowed;
}

.seat.selected {
  background: #2e7d32;
  border-color: #2e7d32;
  color: #fff;
}

.empty {
  color: #999;
  font-size: 0.9rem;
  text-align: center;
  padding: 40px;
}
</style>
