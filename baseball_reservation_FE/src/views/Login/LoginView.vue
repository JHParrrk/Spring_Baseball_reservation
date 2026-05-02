<template>
  <div class="login-container">
    <div class="login-card">
      <h1>스포츠 예약 시스템</h1>
      <p class="description">Google 계정으로 로그인하세요</p>

      <button @click="handleLogin" class="login-button">Google로 로그인</button>

      <RouterLink to="/" class="home-button">메인으로 이동</RouterLink>

      <p class="info-text">
        로그인 시 HTTP-only 쿠키로 JWT 토큰이 발급되며,<br />
        이후 모든 API 요청에 자동으로 첨부됩니다.
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import "./LoginView.css";
/**
 * 로그인 페이지 (프론트엔드에서 제공)
 *
 * 사용자가 'Google로 로그인' 버튼을 클릭하면:
 * 1. 게이트웨이의 /oauth2/authorization/google로 리다이렉트
 * 2. Google 인증 진행
 * 3. 게이트웨이가 /login/oauth2/code/google 콜백 처리
 * 4. JWT 토큰 발급 (HTTP-only 쿠키)
 * 5. 프론트엔드 홈(/)으로 자동 리다이렉트
 */
const handleLogin = () => {
  const gatewayUrl =
    import.meta.env.VITE_GATEWAY_URL || "http://localhost:8082";
  // 계정 선택 화면을 강제해 다른 Google 계정으로도 로그인할 수 있게 합니다.
  window.location.href = `${gatewayUrl}/oauth2/authorization/google?prompt=select_account`;
};
</script>
