export type MatchStatus = "UPCOMING" | "ON_SALE" | "CANCELLED" | "CLOSED";
export type SeatStatus = "AVAILABLE" | "PENDING" | "RESERVED";
export type ReservationStatus = "PENDING" | "CONFIRMED" | "CANCELLED";
export type UserRole = "USER" | "ADMIN";
export type UserStatus = "active" | "inactive" | "suspended" | "blacklisted";

export interface MatchResponse {
  id: number;
  title: string;
  matchDate: string;
  stadiumName: string;
  status: MatchStatus;
}

export interface SeatResponse {
  id: number;
  seatNumber: string;
  tier: string;
  price: number;
  status: SeatStatus;
}

export interface ReservationResponse {
  id: number;
  userId: number;
  seatId: number;
  seatNumber: string;
  tier: string;
  price: number;
  matchTitle: string;
  matchDate: string;
  stadiumName: string;
  status: ReservationStatus;
  createdAt: string;
}

export interface PageResponse<T> {
  content: T[];
  number: number;
  totalPages: number;
  totalElements: number;
  size: number;
}

// ===================== User / Me API Types =====================
export interface UserResponse {
  id: number;
  name: string;
  email: string;
  role: UserRole;
  status: UserStatus;
}

// ===================== Admin API Types =====================
export interface UserSummaryResponse {
  id: number;
  name: string;
  email: string;
  role: UserRole;
  status: UserStatus;
  activeReservations: number;
  cancelledReservations: number;
}

export interface AdminReservationResponse {
  id: number;
  userId: number;
  seatId: number;
  seatNumber: string;
  tier: string;
  price: number;
  matchTitle: string;
  matchDate: string;
  stadiumName: string;
  status: ReservationStatus;
  createdAt: string;
}

export interface MatchCreateRequest {
  title: string;
  matchDate: string;
  stadiumName: string;
}

export interface StadiumTemplateSeatItem {
  seatNumber: string;
  tier: string;
  price: number;
}

export interface StadiumTemplateSummaryResponse {
  stadiumName: string;
  seatCount: number;
}

export interface StadiumTemplateDetailResponse {
  stadiumName: string;
  seatCount: number;
  seats: StadiumTemplateSeatItem[];
}

export interface StadiumTemplateCreateRequest {
  stadiumName: string;
  seats: StadiumTemplateSeatItem[];
}
