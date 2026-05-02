import {
  createRouter,
  createWebHistory,
  type RouteRecordRaw,
} from "vue-router";
import { useAuthStore } from "@/stores/auth";

const routes: RouteRecordRaw[] = [
  {
    path: "/login",
    name: "login",
    component: () => import("@/views/Login/LoginView.vue"),
    meta: { public: true },
  },
  {
    path: "/login/success",
    name: "login-success",
    component: () => import("@/views/LoginSuccess/LoginSuccessView.vue"),
    meta: { public: true },
  },
  {
    path: "/",
    name: "home",
    component: () => import("@/views/Home/HomeView.vue"),
    meta: { public: true },
  },
  {
    path: "/matches/:id",
    name: "match-detail",
    component: () => import("@/views/MatchDetail/MatchDetailView.vue"),
  },
  {
    path: "/my-reservations",
    name: "my-reservations",
    component: () => import("@/views/MyReservations/MyReservationsView.vue"),
  },
  {
    path: "/admin",
    name: "admin",
    component: () => import("@/views/Admin/AdminView.vue"),
    meta: { requiresAdmin: true },
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

/**
 * 네비게이션 가드
 * - public 라우트는 인증 없이 접근 허용
 * - requiresAdmin 라우트는 ADMIN 역할 확인
 * - 그 외 라우트는 로그인 상태 확인, 미로그인 시 /login으로 이동
 */
router.beforeEach(async (to) => {
  const auth = useAuthStore();

  await auth.restoreSession();

  if (to.name === "login" && auth.isLoggedIn) {
    return { name: "home" };
  }

  if (to.meta["public"]) return true;

  if (!auth.isLoggedIn) {
    // 프론트엔드의 로그인 페이지로 리다이렉트
    return { name: "login" };
  }

  if (to.meta["requiresAdmin"] && !auth.isAdmin) {
    return { name: "home" };
  }

  return true;
});

export default router;
