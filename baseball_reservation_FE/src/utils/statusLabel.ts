import type { MatchStatus, ReservationStatus } from "@/api/types";

const MATCH_STATUS_MAP: Partial<Record<MatchStatus, string>> = {
  UPCOMING: "예정",
  ON_SALE: "예매 중",
  CLOSED: "마감",
  CANCELLED: "취소됨",
};

const RESERVATION_STATUS_MAP: Partial<Record<ReservationStatus, string>> = {
  PENDING: "결제 대기 중",
  CONFIRMED: "예약 완료",
  CANCELLED: "취소됨",
};

export function matchStatusLabel(status?: MatchStatus): string {
  if (!status) return "";
  return MATCH_STATUS_MAP[status] ?? status;
}

export function reservationStatusLabel(status: ReservationStatus): string {
  return RESERVATION_STATUS_MAP[status] ?? status;
}
