import axios from "axios";

const GATEWAY_URL = import.meta.env.VITE_GATEWAY_URL;

/**
 * 공유 axios 인스턴스
 * - baseURL: 모든 API 요청을 게이트웨이로 전달
 * - withCredentials: HTTP-only 쿠키(auth_token)를 자동으로 첨부
 */
export const apiClient = axios.create({
  baseURL: GATEWAY_URL,
  withCredentials: true,
});

async function clearAuthStateSafely(): Promise<void> {
  try {
    const { useAuthStore } = await import("@/stores/auth");
    const store = useAuthStore();
    store.isLoggedIn = false;
    store.userRole = null;
  } catch (err) {
    // 인터셉터에서 store 동기화 실패 시에도 리다이렉트는 진행됨
    console.warn("Failed to sync auth store during 401 handling:", err);
  }
}

/**
 * 응답 인터셉터: 401 응답 시 게이트웨이 로그인 페이지로 이동
 * - HTTP-only 쿠키 만료 또는 미설정 상태에서 인증이 필요한 API를 호출한 경우
 */
apiClient.interceptors.response.use(
  (response) => response,
  (error: unknown) => {
    if (!axios.isAxiosError(error)) {
      return Promise.reject(error);
    }

    if (error.response?.status === 401) {
      // 로그아웃 엔드포인트 자체의 401은 무한 루프 방지를 위해 무시
      const url = error.config?.url ?? "";
      if (!url.includes("/api/auth/logout")) {
        void clearAuthStateSafely();
        if (window.location.pathname !== "/login") {
          window.location.href = "/login";
        }
      }
    }

    if (error.code === "ECONNABORTED") {
      error.message = "요청 시간이 초과되었습니다. 잠시 후 다시 시도해주세요.";
    } else if (!error.response) {
      error.message = "네트워크 연결을 확인한 뒤 다시 시도해주세요.";
    }

    return Promise.reject(error);
  },
);
