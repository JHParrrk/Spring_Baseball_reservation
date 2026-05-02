<template>
  <NavBar v-if="showNav" />
  <main :class="{ 'with-nav': showNav }">
    <RouterView />
  </main>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useRoute } from "vue-router";
import NavBar from "./components/NavBar.vue";

const route = useRoute();
const showNav = computed(
  () => route.name !== "login" && route.name !== "login-success",
);
</script>

<style>
*,
*::before,
*::after {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

body {
  font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
  background: #f0f2f5;
  color: #222;
  min-height: 100vh;
}

main.with-nav {
  padding-top: 60px;
}
</style>
