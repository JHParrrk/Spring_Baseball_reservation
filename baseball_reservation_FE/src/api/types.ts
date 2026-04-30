export type MatchStatus = "UPCOMING" | "ON_SALE" | "CANCELLED" | "CLOSED";
export type SeatStatus = "AVAILABLE" | "RESERVED";
export type ReservationStatus = "PENDING" | "CONFIRMED" | "CANCELLED";

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
