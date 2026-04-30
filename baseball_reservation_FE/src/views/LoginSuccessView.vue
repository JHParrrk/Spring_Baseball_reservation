<template>
  <div class="login-success">
    <p>로그인 처리 중...</p>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "@/stores/auth";

const router = useRouter();
const auth = useAuthStore();

onMounted(() => {
  // 게이트웨이가 OAuth2 성공 후 이 페이지로 리다이렉트합니다.
  // HTTP-only 쿠키(auth_token)는 이미 브라우저에 저장된 상태입니다.
  auth.setLoggedIn();
  void router.replace("/");
});
</script>

<style scoped>
.login-success {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  font-size: 1.1rem;
  color: #666;
}
</style>
