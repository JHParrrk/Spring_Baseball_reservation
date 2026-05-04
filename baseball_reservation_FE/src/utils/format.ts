/**
 * 날짜 포맷 (ISO → 한국어 로케일)
 */
export function formatDate(isoStr?: string): string {
  if (!isoStr) return "";
  return new Date(isoStr).toLocaleString("ko-KR", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
}

/**
 * 가격 포맷 (숫자 → 한국어 천 단위 구분)
 */
export function formatPrice(price: number): string {
  return Number(price).toLocaleString("ko-KR");
}
