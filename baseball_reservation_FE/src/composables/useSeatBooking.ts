import { ref, computed, onUnmounted } from "vue";
import { matchApi, reservationApi } from "@/api";
import type { SeatResponse, ReservationResponse } from "@/api/types";
import { extractApiError } from "@/utils/apiError";

export interface PendingReservationItem {
  id: number;
  seatNumber: string;
  tier: string;
  price: number;
}

/**
 * 좌석 선택 → 선점(PENDING) 예약 → 결제 흐름을 관리하는 Composable.
 *
 * @param matchId - 현재 경기 ID
 * @param onRefresh - 예약 상태 변경 후 좌석 목록을 새로 고치는 콜백
 *
 * @example
 * const booking = useSeatBooking(matchId, () => loadData());
 */
export function useSeatBooking(
  matchId: number,
  onRefresh: () => Promise<void>,
) {
  const maxSeatsFromEnv = Number(
    import.meta.env.VITE_MAX_SEATS_PER_RESERVATION,
  );
  const MAX_SEATS =
    Number.isInteger(maxSeatsFromEnv) && maxSeatsFromEnv > 0
      ? maxSeatsFromEnv
      : 10;

  const selectedSeats = ref<SeatResponse[]>([]);
  const reserving = ref(false);
  const showPaymentModal = ref(false);
  const paySubmitting = ref(false);

  const pendingReservationIds = ref<number[]>([]);
  const pendingReservations = ref<PendingReservationItem[]>([]);

  const feedbackSuccess = ref(false);
  const feedbackError = ref<string | null>(null);
  const feedbackPartial = ref(false);

  const totalPrice = computed(() =>
    selectedSeats.value.reduce((sum, s) => sum + s.price, 0),
  );

  function onSelectSeat(seat: SeatResponse): void {
    const idx = selectedSeats.value.findIndex((s) => s.id === seat.id);
    if (idx >= 0) {
      selectedSeats.value.splice(idx, 1);
    } else {
      if (selectedSeats.value.length >= MAX_SEATS) {
        feedbackError.value = `최대 ${MAX_SEATS}매까지 선택 가능합니다.`;
        return;
      }
      selectedSeats.value.push(seat);
    }
    feedbackSuccess.value = false;
    feedbackError.value = null;
  }

  /** 예약 버튼 클릭: 좌석 PENDING 선점 후 결제 모달 오픈 */
  async function openPaymentModal(): Promise<void> {
    if (selectedSeats.value.length === 0) return;

    reserving.value = true;
    feedbackError.value = null;
    feedbackSuccess.value = false;
    feedbackPartial.value = false;

    try {
      // 선점 직전 최신 좌석 상태를 확인하여 동시성 충돌 UX를 개선합니다.
      const latest = await matchApi.getAllSeats(matchId);
      const latestMap = new Map(
        latest.data.map((seat) => [seat.id, seat.status]),
      );
      const invalidSeat = selectedSeats.value.find(
        (seat) => latestMap.get(seat.id) !== "AVAILABLE",
      );
      if (invalidSeat) {
        feedbackError.value =
          "선택한 좌석 중 일부가 이미 예약되었습니다. 좌석 상태를 새로고침 후 다시 선택해주세요.";
        selectedSeats.value = [];
        await onRefresh();
        return;
      }

      const res = await reservationApi.create(
        selectedSeats.value.map((s) => s.id),
      );
      pendingReservationIds.value = res.data.map((r) => r.id);
      pendingReservations.value = res.data.map(
        (r: ReservationResponse): PendingReservationItem => ({
          id: r.id,
          seatNumber: r.seatNumber,
          tier: r.tier,
          price: r.price,
        }),
      );
      selectedSeats.value = [];
      await onRefresh();
      showPaymentModal.value = true;
    } catch (e) {
      feedbackError.value = extractApiError(
        e,
        "좌석 선점에 실패했습니다. 다시 시도해주세요.",
      );
      selectedSeats.value = [];
      await onRefresh();
    } finally {
      reserving.value = false;
    }
  }

  /** 모달 취소: 선점 예약 취소 → 좌석 복원 */
  async function cancelPendingAndClose(): Promise<void> {
    showPaymentModal.value = false;
    if (pendingReservationIds.value.length === 0) return;
    try {
      await Promise.all(
        pendingReservationIds.value.map((id) => reservationApi.cancel(id)),
      );
    } catch (err) {
      // 취소 실패 시 백엔드 TTL 5분으로 자동 복원됨 — 무시
      console.warn("Failed to cancel pending reservations:", err);
    } finally {
      pendingReservationIds.value = [];
      pendingReservations.value = [];
      await onRefresh();
    }
  }

  /** 결제: 선점 예약 ID로 결제 진행 */
  async function pay(cvc: string): Promise<void> {
    if (pendingReservationIds.value.length === 0) return;

    paySubmitting.value = true;
    feedbackError.value = null;

    try {
      // TODO(security): 현재는 테스트용으로 CVC를 직접 전송합니다.
      // 실서비스 전에는 Stripe.js(또는 동등한 PCI-DSS 준수 결제 SDK)로 전환해야 합니다.
      // 1) 프런트에서 카드정보를 결제사 SDK로 토큰화
      // 2) 백엔드에는 token/paymentMethodId만 전달
      // 3) CVC/카드번호/만료일은 우리 서버 로그/DB/메모리에 저장하지 않기
      await reservationApi.pay([...pendingReservationIds.value], cvc);
      feedbackSuccess.value = true;
      showPaymentModal.value = false;
      pendingReservationIds.value = [];
      pendingReservations.value = [];
      await onRefresh();
      setTimeout(() => (feedbackSuccess.value = false), 5000);
    } catch (e) {
      feedbackPartial.value = true;
      feedbackError.value = extractApiError(
        e,
        "결제에 실패했습니다. 좌석은 5분간 확보되어 있습니다. 내 예약에서 재결제하세요.",
      );
      showPaymentModal.value = false;
      pendingReservationIds.value = [];
      pendingReservations.value = [];
      await onRefresh();
    } finally {
      paySubmitting.value = false;
    }
  }

  // 페이지 이탈 시 선점 예약 즉시 취소 (백엔드 TTL 5분 있으나 즉시 해제가 UX 상 이상적)
  onUnmounted(() => {
    if (pendingReservationIds.value.length > 0) {
      void Promise.all(
        pendingReservationIds.value.map((id) => reservationApi.cancel(id)),
      ).catch((err) => {
        console.warn("Failed to cancel pending reservations on unmount:", err);
      });
    }
  });

  return {
    selectedSeats,
    reserving,
    showPaymentModal,
    paySubmitting,
    pendingReservations,
    totalPrice,
    feedbackSuccess,
    feedbackError,
    feedbackPartial,
    onSelectSeat,
    openPaymentModal,
    cancelPendingAndClose,
    pay,
  };
}
