<template>
  <div class="seat-grid">
    <div class="legend">
      <span class="badge available">AVAILABLE</span>
      <span class="badge reserved">RESERVED</span>
      <span class="badge selected">선택됨</span>
    </div>

    <div v-if="tiers.length === 0" class="empty">좌석 정보가 없습니다.</div>

    <div v-for="tier in tiers" :key="tier" class="tier-section">
      <h3 class="tier-title">{{ tier }}</h3>
      <div class="seats">
        <button
          v-for="seat in seatsByTier[tier]"
          :key="seat.id"
          class="seat"
          :class="{
            available: seat.status === 'AVAILABLE',
            reserved: seat.status === 'RESERVED',
            selected: selectedSeatIds?.includes(seat.id) ?? false,
          }"
          :disabled="seat.status === 'RESERVED'"
          @click="selectSeat(seat)"
        >
          {{ seat.seatNumber }}
        </button>
      </div>
    </div>
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
