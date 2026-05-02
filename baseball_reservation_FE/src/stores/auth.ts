import { defineStore } from "pinia";
import { ref, computed } from "vue";
import { authApi, userApi } from "@/api";
import type { UserRole } from "@/api/types";

/**
 * 인증 상태 스토어
 *
 * HTTP-only 쿠키는 JS에서 읽을 수 없으므로 localStorage 플래그로 로그인 여부를 판단합니다.
 * - setLoggedIn(): /login/success 라우트에서 OAuth2 완료 후 호출 → /users/me 로 role 조회
 * - logout(): API 401 또는 명시적 로그아웃 시 호출
 * - 실제 인증 여부는 게이트웨이의 JWT 쿠키 검증에서 결정됩니다.
 */
export const useAuthStore = defineStore("auth", () => {
  const isLoggedIn = ref(localStorage.getItem("isLoggedIn") === "true");
  // userRole은 localStorage를 초기 신뢰 소스로 사용하지 않음.
  // restoreSession()이 서버에서 role을 검증 후 설정합니다.
  const userRole = ref<UserRole | null>(null);
  const sessionChecked = ref(false);

  const isAdmin = computed(() => userRole.value === "ADMIN");

  async function setLoggedIn(): Promise<void> {
    isLoggedIn.value = true;
    sessionChecked.value = true;
    localStorage.setItem("isLoggedIn", "true");
    try {
      const res = await userApi.getMe();
      userRole.value = res.data.role;
      localStorage.setItem("userRole", res.data.role);
    } catch {
      // role 조회 실패 시에도 로그인 상태는 유지
    }
  }

  async function logout(): Promise<void> {
    try {
      await authApi.logout();
    } catch {
      // 서버 로그아웃 실패 시에도 클라이언트 상태는 정리합니다.
    }

    isLoggedIn.value = false;
    userRole.value = null;
    sessionChecked.value = true;
    localStorage.removeItem("isLoggedIn");
    localStorage.removeItem("userRole");
    window.location.href = "/login";
  }

  async function restoreSession(): Promise<void> {
    if (sessionChecked.value) return;
    try {
      const res = await userApi.getMe();
      isLoggedIn.value = true;
      userRole.value = res.data.role;
      localStorage.setItem("isLoggedIn", "true");
      localStorage.setItem("userRole", res.data.role);
    } catch {
      isLoggedIn.value = false;
      userRole.value = null;
      localStorage.removeItem("isLoggedIn");
      localStorage.removeItem("userRole");
    } finally {
      sessionChecked.value = true;
    }
  }

  return {
    isLoggedIn,
    userRole,
    isAdmin,
    setLoggedIn,
    logout,
    restoreSession,
  };
});
