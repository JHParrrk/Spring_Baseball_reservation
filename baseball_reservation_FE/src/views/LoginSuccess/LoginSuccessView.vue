<template>
  <div class="login-success">
    <p>로그인 처리 중...</p>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "@/stores/auth";
import "./LoginSuccessView.css";

const router = useRouter();
const auth = useAuthStore();

onMounted(async () => {
  // 게이트웨이가 OAuth2 성공 후 이 페이지로 리다이렉트합니다.
  // HTTP-only 쿠키(auth_token)는 이미 브라우저에 저장된 상태입니다.
  // setLoggedIn()이 완료(role 확보)된 후 라우팅해야 가드의 isAdmin 판단이 정확합니다.
  await auth.setLoggedIn();
  await router.replace("/");
});
</script>
