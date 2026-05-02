import axios, { type AxiosResponse } from "axios";
import type {
  AdminReservationResponse,
  MatchCreateRequest,
  MatchResponse,
  MatchStatus,
  PageResponse,
  ReservationResponse,
  ReservationStatus,
  SeatResponse,
  StadiumTemplateCreateRequest,
  StadiumTemplateDetailResponse,
  StadiumTemplateSummaryResponse,
  UserResponse,
  UserRole,
  UserStatus,
  UserSummaryResponse,
} from "./types";

const GATEWAY_URL = import.meta.env.VITE_GATEWAY_URL;

/**
 * axios 인스턴스 설정
 * - baseURL: 모든 API 요청을 게이트웨이로 전달
 * - withCredentials: HTTP-only 쿠키(auth_token)를 자동으로 첨부
 */
const api = axios.create({
  baseURL: GATEWAY_URL,
  withCredentials: true,
});

/**
 * 응답 인터셉터: 401 응답 시 게이트웨이 로그인 페이지로 이동
 * - HTTP-only 쿠키 만료 또는 미설정 상태에서 인증이 필요한 API를 호출한 경우
 */
api.interceptors.response.use(
  (response) => response,
  (error: unknown) => {
    if (axios.isAxiosError(error) && error.response?.status === 401) {
      // 로그아웃 엔드포인트 자체의 401은 무한 루프 방지를 위해 무시
      const url = error.config?.url ?? "";
      if (!url.includes("/api/auth/logout")) {
        localStorage.removeItem("isLoggedIn");
        localStorage.removeItem("userRole");
        // Pinia 상태 동기화 — dynamic import으로 auth.ts 순환 의존성 방지
        void import("@/stores/auth").then(({ useAuthStore }) => {
          const store = useAuthStore();
          store.isLoggedIn = false;
          store.userRole = null;
        });
        if (window.location.pathname !== "/login") {
          window.location.href = "/login";
        }
      }
    }
    return Promise.reject(error);
  },
);

// ===================== Match API =====================
export const matchApi = {
  /** 예매 가능한 경기 목록 조회 (UPCOMING, ON_SALE) */
  getMatches: (
    page = 0,
    size = 20,
  ): Promise<AxiosResponse<PageResponse<MatchResponse>>> =>
    api.get("/matches", { params: { page, size } }),

  /** 단일 경기 상세 조회 */
  getMatch: (matchId: number | string): Promise<AxiosResponse<MatchResponse>> =>
    api.get(`/matches/${matchId}`),

  /** 특정 경기의 전체 좌석(상태 포함) 조회 */
  getAllSeats: (
    matchId: number | string,
  ): Promise<AxiosResponse<SeatResponse[]>> =>
    api.get(`/matches/${matchId}/seats/all`),
};

// ===================== Reservation API =====================
export const reservationApi = {
  /** 예약 생성: { seatIds } — CVC 없이 좌석만 PENDING으로 예약. 최대 10매 */
  create: (seatIds: number[]): Promise<AxiosResponse<ReservationResponse[]>> =>
    api.post("/reservations", { seatIds }),

  /** 내 예약 목록 조회 */
  getMyReservations: (
    page = 0,
    size = 20,
  ): Promise<AxiosResponse<PageResponse<ReservationResponse>>> =>
    api.get("/reservations/me", { params: { page, size } }),

  /** 예약 취소 (status → CANCELLED, 좌석 AVAILABLE 복원) */
  cancel: (id: number): Promise<AxiosResponse<ReservationResponse>> =>
    api.patch(`/reservations/${id}/cancel`),

  /** 예약 레코드 삭제 */
  delete: (id: number): Promise<AxiosResponse<void>> =>
    api.delete(`/reservations/${id}`),

  /** 결제 개시: '내 예약'에서 PENDING 예약 선택 후 CVC 입력 → Kafka 결제 이벤트 발행 */
  pay: (
    reservationIds: number[],
    cvc: string,
  ): Promise<AxiosResponse<ReservationResponse[]>> =>
    api.post("/reservations/pay", { reservationIds, cvc }),
};

// ===================== User API =====================
export const userApi = {
  /** 내 프로필 조회 (role 포함) */
  getMe: (): Promise<AxiosResponse<UserResponse>> => api.get("/users/me"),
};

// ===================== Auth API =====================
export const authApi = {
  /** 게이트웨이 쿠키(auth_token) 만료 처리 */
  logout: (): Promise<AxiosResponse<void>> => api.post("/api/auth/logout"),
};

// ===================== Admin - Match API =====================
export const adminMatchApi = {
  /** 관리자 경기 목록 조회 (전체 상태 포함) */
  getMatches: (
    page = 0,
    size = 20,
  ): Promise<AxiosResponse<PageResponse<MatchResponse>>> =>
    api.get("/admin/matches", { params: { page, size } }),

  /** 경기 등록 */
  createMatch: (
    data: MatchCreateRequest,
  ): Promise<AxiosResponse<MatchResponse>> => api.post("/admin/matches", data),

  /** 경기 상태 변경 */
  updateMatchStatus: (
    id: number,
    status: MatchStatus,
  ): Promise<AxiosResponse<MatchResponse>> =>
    api.patch(`/admin/matches/${id}/status`, null, { params: { status } }),

  /** 구장 좌석 템플릿 목록 조회 */
  getStadiumTemplates: (): Promise<
    AxiosResponse<StadiumTemplateSummaryResponse[]>
  > => api.get("/admin/matches/stadium-templates"),

  /** 구장 좌석 템플릿 상세 조회 */
  getStadiumTemplateDetail: (
    stadiumName: string,
  ): Promise<AxiosResponse<StadiumTemplateDetailResponse>> =>
    api.get(
      `/admin/matches/stadium-templates/${encodeURIComponent(stadiumName)}`,
    ),

  /** 신규 구장 좌석 템플릿 등록 */
  createStadiumTemplate: (
    data: StadiumTemplateCreateRequest,
  ): Promise<AxiosResponse<StadiumTemplateDetailResponse>> =>
    api.post("/admin/matches/stadium-templates", data),
};

// ===================== Admin - Reservation API =====================
export const adminReservationApi = {
  /** 전체 예약 목록 조회 (필터링 가능) */
  getReservations: (params: {
    page?: number;
    size?: number;
    status?: ReservationStatus;
    userId?: number;
    matchId?: number;
  }): Promise<AxiosResponse<PageResponse<AdminReservationResponse>>> =>
    api.get("/admin/reservations", { params }),

  /** 예약 강제 취소 */
  cancelReservation: (
    id: number,
  ): Promise<AxiosResponse<AdminReservationResponse>> =>
    api.post(`/admin/reservations/${id}/cancel`),
};

// ===================== Admin - User API =====================
export const adminUserApi = {
  /** 전체 사용자 목록 조회 */
  getUsers: (
    page = 0,
    size = 20,
  ): Promise<AxiosResponse<PageResponse<UserSummaryResponse>>> =>
    api.get("/admin/users", { params: { page, size } }),

  /** 사용자 상태 변경 */
  updateUserStatus: (
    id: number,
    status: UserStatus,
  ): Promise<AxiosResponse<UserResponse>> =>
    api.patch(`/admin/users/${id}/status`, null, { params: { status } }),

  /** 사용자 역할 변경 */
  updateUserRole: (
    id: number,
    role: UserRole,
  ): Promise<AxiosResponse<UserResponse>> =>
    api.patch(`/admin/users/${id}/role`, null, { params: { role } }),
};
