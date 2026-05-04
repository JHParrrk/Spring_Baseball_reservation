import type { AxiosResponse } from "axios";
import { apiClient } from "./client";
import type {
  MatchResponse,
  PageResponse,
  ReservationResponse,
  SeatResponse,
  UserResponse,
} from "./types";

// ===================== Match API =====================
export const matchApi = {
  /** 예매 가능한 경기 목록 조회 (UPCOMING, ON_SALE) */
  getMatches: (
    page = 0,
    size = 20,
  ): Promise<AxiosResponse<PageResponse<MatchResponse>>> =>
    apiClient.get("/matches", { params: { page, size } }),

  /** 단일 경기 상세 조회 */
  getMatch: (matchId: number | string): Promise<AxiosResponse<MatchResponse>> =>
    apiClient.get(`/matches/${matchId}`),

  /** 특정 경기의 전체 좌석(상태 포함) 조회 */
  getAllSeats: (
    matchId: number | string,
  ): Promise<AxiosResponse<SeatResponse[]>> =>
    apiClient.get(`/matches/${matchId}/seats/all`),
};

// ===================== Reservation API =====================
export const reservationApi = {
  /** 예약 생성: { seatIds } — CVC 없이 좌석만 PENDING으로 예약. 최대 10매 */
  create: (seatIds: number[]): Promise<AxiosResponse<ReservationResponse[]>> =>
    apiClient.post("/reservations", { seatIds }),

  /** 내 예약 목록 조회 */
  getMyReservations: (
    page = 0,
    size = 20,
  ): Promise<AxiosResponse<PageResponse<ReservationResponse>>> =>
    apiClient.get("/reservations/me", { params: { page, size } }),

  /** 예약 취소 (status → CANCELLED, 좌석 AVAILABLE 복원) */
  cancel: (id: number): Promise<AxiosResponse<ReservationResponse>> =>
    apiClient.patch(`/reservations/${id}/cancel`),

  /** 예약 레코드 삭제 */
  delete: (id: number): Promise<AxiosResponse<void>> =>
    apiClient.delete(`/reservations/${id}`),

  /**
   * 결제 개시: '내 예약'에서 PENDING 예약 선택 후 CVC 입력 → Kafka 결제 이벤트 발행
   *
   * TODO(security): 실서비스 전 Stripe.js 등 PCI-DSS 준수 결제 SDK 토큰화 방식으로 전환 필요.
   * - 현재처럼 CVC를 우리 API로 직접 보내면 안 됩니다.
   * - 프런트에서 결제 토큰 생성 후 서버에는 토큰만 전달하도록 변경해야 합니다.
   */
  pay: (
    reservationIds: number[],
    cvc: string,
  ): Promise<AxiosResponse<ReservationResponse[]>> =>
    apiClient.post("/reservations/pay", { reservationIds, cvc }),
};

// ===================== User API =====================
export const userApi = {
  /** 내 프로필 조회 (role 포함) */
  getMe: (): Promise<AxiosResponse<UserResponse>> => apiClient.get("/users/me"),
};

// ===================== Auth API =====================
export const authApi = {
  /** 게이트웨이 쿠키(auth_token) 만료 처리 */
  logout: (): Promise<AxiosResponse<void>> =>
    apiClient.post("/api/auth/logout"),
};

// Admin API는 @/api/admin 에서 import하세요.
