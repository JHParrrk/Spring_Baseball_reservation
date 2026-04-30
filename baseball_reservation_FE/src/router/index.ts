import {
  createRouter,
  createWebHistory,
  type RouteRecordRaw,
} from "vue-router";
import { useAuthStore } from "@/stores/auth";

const routes: RouteRecordRaw[] = [
  {
    path: "/login/success",
    name: "login-success",
    component: () => import("@/views/LoginSuccessView.vue"),
    meta: { public: true },
  },
  {
    path: "/",
    name: "home",
    component: () => import("@/views/HomeView.vue"),
  },
  {
    path: "/matches/:id",
    name: "match-detail",
    component: () => import("@/views/MatchDetailView.vue"),
  },
  {
    path: "/my-reservations",
    name: "my-reservations",
    component: () => import("@/views/MyReservationsView.vue"),
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

/**
 * 네비게이션 가드
 * - public 라우트는 인증 없이 접근 허용
 * - 그 외 라우트는 로그인 상태 확인, 미로그인 시 게이트웨이 로그인 페이지로 이동
 */
router.beforeEach((to) => {
  if (to.meta["public"]) return true;

  const auth = useAuthStore();
  if (!auth.isLoggedIn) {
    window.location.href = `${import.meta.env.VITE_GATEWAY_URL}/login`;
    return false;
  }
  return true;
});

export default router;
