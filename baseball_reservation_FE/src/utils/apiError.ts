import axios from "axios";

interface ApiErrorData {
  message?: string;
  error?: string;
  detail?: string;
}

/**
 * Axios 에러 또는 알 수 없는 에러에서 서버 메시지를 추출합니다.
 *
 * @param e - catch 블록의 unknown 에러
 * @param fallback - 서버 메시지가 없을 때 사용할 기본 문구
 *
 * @example
 * } catch (e) {
 *   errorMsg.value = extractApiError(e, "요청에 실패했습니다.");
 * }
 */
export function extractApiError(e: unknown, fallback: string): string {
  if (axios.isAxiosError(e)) {
    const data = e.response?.data;
    if (typeof data === "object" && data !== null) {
      const parsed = data as ApiErrorData;
      return parsed.message ?? parsed.error ?? parsed.detail ?? fallback;
    }
  }
  return fallback;
}
