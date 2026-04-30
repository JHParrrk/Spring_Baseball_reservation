import { defineStore } from "pinia";
import { ref } from "vue";

/**
 * 인증 상태 스토어
 *
 * HTTP-only 쿠키는 JS에서 읽을 수 없으므로 localStorage 플래그로 로그인 여부를 판단합니다.
 * - setLoggedIn(): /login/success 라우트에서 OAuth2 완료 후 호출
 * - logout(): API 401 또는 명시적 로그아웃 시 호출
 * - 실제 인증 여부는 게이트웨이의 JWT 쿠키 검증에서 결정됩니다.
 */
export const useAuthStore = defineStore("auth", () => {
  const isLoggedIn = ref(localStorage.getItem("isLoggedIn") === "true");

  function setLoggedIn(): void {
    isLoggedIn.value = true;
    localStorage.setItem("isLoggedIn", "true");
  }

  function logout(): void {
    isLoggedIn.value = false;
    localStorage.removeItem("isLoggedIn");
    window.location.href = `${import.meta.env.VITE_GATEWAY_URL}/login`;
  }

  return { isLoggedIn, setLoggedIn, logout };
});
