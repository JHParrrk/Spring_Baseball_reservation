import { defineStore } from "pinia";
import { ref, computed } from "vue";
import { authApi, userApi } from "@/api";
import type { UserRole } from "@/api/types";

/**
 * 인증 상태 스토어
 *
 * 실제 인증 여부는 게이트웨이의 HTTP-only JWT 쿠키 검증으로 결정됩니다.
 * - setLoggedIn(): /login/success 라우트에서 OAuth2 완료 후 호출 → /users/me 로 role 조회
 * - logout(): API 401 또는 명시적 로그아웃 시 호출
 */
export const useAuthStore = defineStore("auth", () => {
  const isLoggedIn = ref(false);
  // restoreSession()이 서버에서 role을 검증 후 설정합니다.
  const userRole = ref<UserRole | null>(null);
  const sessionChecked = ref(false);
  let isLoggingOut = false;

  const isAdmin = computed(() => userRole.value === "ADMIN");

  async function setLoggedIn(): Promise<void> {
    isLoggedIn.value = true;
    sessionChecked.value = true;
    try {
      const res = await userApi.getMe();
      userRole.value = res.data.role;
    } catch (err) {
      // role 조회 실패 시에도 로그인 상태는 유지
      console.warn("Failed to load role after login:", err);
    }
  }

  async function logout(): Promise<void> {
    if (isLoggingOut) return;
    isLoggingOut = true;

    try {
      await authApi.logout();
    } catch (err) {
      // 서버 로그아웃 실패 시에도 클라이언트 상태는 정리합니다.
      console.warn("Logout API failed; proceeding with local cleanup:", err);
    } finally {
      isLoggedIn.value = false;
      userRole.value = null;
      sessionChecked.value = true;
    }
    window.location.href = "/login";
  }

  async function restoreSession(): Promise<void> {
    if (sessionChecked.value) return;
    try {
      const res = await userApi.getMe();
      isLoggedIn.value = true;
      userRole.value = res.data.role;
    } catch (err) {
      isLoggedIn.value = false;
      userRole.value = null;
      console.warn("Session restore failed:", err);
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
