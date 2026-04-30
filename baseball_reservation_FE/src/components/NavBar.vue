<template>
  <nav>
    <div class="nav-inner">
      <RouterLink to="/" class="nav-logo">⚾ 야구장 예약</RouterLink>
      <div class="nav-links">
        <RouterLink to="/">경기 목록</RouterLink>
        <RouterLink to="/my-reservations">내 예약</RouterLink>
        <button class="logout-btn" @click="auth.logout()">로그아웃</button>
      </div>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { useAuthStore } from "@/stores/auth";
const auth = useAuthStore();
</script>

<style scoped>
nav {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: #1565c0;
  z-index: 100;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.nav-inner {
  max-width: 1100px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.nav-logo {
  color: #fff;
  font-size: 1.2rem;
  font-weight: 700;
  text-decoration: none;
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 20px;
}

.nav-links a {
  color: rgba(255, 255, 255, 0.85);
  text-decoration: none;
  font-size: 0.95rem;
  transition: color 0.15s;
}

.nav-links a:hover,
.nav-links a.router-link-active {
  color: #fff;
}

.logout-btn {
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.4);
  color: #fff;
  padding: 6px 14px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: background 0.15s;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.25);
}
</style>
