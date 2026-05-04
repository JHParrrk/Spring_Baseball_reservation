import type { AxiosResponse } from "axios";
import { apiClient } from "./client";
import type {
  AdminReservationResponse,
  MatchCreateRequest,
  MatchResponse,
  MatchStatus,
  PageResponse,
  ReservationStatus,
  StadiumTemplateCreateRequest,
  StadiumTemplateDetailResponse,
  StadiumTemplateSummaryResponse,
  UserResponse,
  UserRole,
  UserStatus,
  UserSummaryResponse,
} from "./types";

// ===================== Admin - Match API =====================
export const adminMatchApi = {
  /** 관리자 경기 목록 조회 (전체 상태 포함) */
  getMatches: (
    page = 0,
    size = 20,
  ): Promise<AxiosResponse<PageResponse<MatchResponse>>> =>
    apiClient.get("/admin/matches", { params: { page, size } }),

  /** 경기 등록 */
  createMatch: (
    data: MatchCreateRequest,
  ): Promise<AxiosResponse<MatchResponse>> =>
    apiClient.post("/admin/matches", data),

  /** 경기 상태 변경 */
  updateMatchStatus: (
    id: number,
    status: MatchStatus,
  ): Promise<AxiosResponse<MatchResponse>> =>
    apiClient.patch(`/admin/matches/${id}/status`, null, {
      params: { status },
    }),

  /** 구장 좌석 템플릿 목록 조회 */
  getStadiumTemplates: (): Promise<
    AxiosResponse<StadiumTemplateSummaryResponse[]>
  > => apiClient.get("/admin/matches/stadium-templates"),

  /** 구장 좌석 템플릿 상세 조회 */
  getStadiumTemplateDetail: (
    stadiumName: string,
  ): Promise<AxiosResponse<StadiumTemplateDetailResponse>> =>
    apiClient.get(
      `/admin/matches/stadium-templates/${encodeURIComponent(stadiumName)}`,
    ),

  /** 신규 구장 좌석 템플릿 등록 */
  createStadiumTemplate: (
    data: StadiumTemplateCreateRequest,
  ): Promise<AxiosResponse<StadiumTemplateDetailResponse>> =>
    apiClient.post("/admin/matches/stadium-templates", data),
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
    apiClient.get("/admin/reservations", { params }),

  /** 예약 강제 취소 */
  cancelReservation: (
    id: number,
  ): Promise<AxiosResponse<AdminReservationResponse>> =>
    apiClient.post(`/admin/reservations/${id}/cancel`),
};

// ===================== Admin - User API =====================
export const adminUserApi = {
  /** 전체 사용자 목록 조회 */
  getUsers: (
    page = 0,
    size = 20,
  ): Promise<AxiosResponse<PageResponse<UserSummaryResponse>>> =>
    apiClient.get("/admin/users", { params: { page, size } }),

  /** 사용자 상태 변경 */
  updateUserStatus: (
    id: number,
    status: UserStatus,
  ): Promise<AxiosResponse<UserResponse>> =>
    apiClient.patch(`/admin/users/${id}/status`, null, { params: { status } }),

  /** 사용자 역할 변경 */
  updateUserRole: (
    id: number,
    role: UserRole,
  ): Promise<AxiosResponse<UserResponse>> =>
    apiClient.patch(`/admin/users/${id}/role`, null, { params: { role } }),
};
