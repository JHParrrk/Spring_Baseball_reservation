import { useRouter } from "vue-router";
import { useAuthStore } from "@/stores/auth";

/**
 * 로그인 여부를 확인하고, 비로그인 시 확인 알럿 후 로그인 페이지로 유도하는 Composable.
 *
 * @example
 * const { requireLogin } = useAuthGuard();
 * requireLogin(() => router.push(`/matches/${id}`));
 */
export function useAuthGuard() {
  const router = useRouter();
  const auth = useAuthStore();

  /**
   * 로그인 상태면 callback 실행, 비로그인이면 확인 알럿 후 /login 으로 이동.
   */
  function requireLogin(callback: () => void): void {
    if (auth.isLoggedIn) {
      callback();
    } else {
      if (confirm("로그인 후 예약 가능합니다.\n로그인하시겠습니까?")) {
        router.push("/login");
      }
    }
  }

  return { requireLogin };
}
