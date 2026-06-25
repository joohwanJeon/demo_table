<template>
  <div class="app">
    <header class="topbar">
      <NuxtLink to="/" class="logo">T3<b>-Table</b></NuxtLink>
      <span class="tagline">예약 오픈런 데모</span>
      <div class="topbar-right">
        <template v-if="isLoggedIn">
          <span class="user-info">👤 {{ username }}</span>
          <button class="btn-logout" @click="handleLogout">로그아웃</button>
        </template>
        <template v-else>
          <NuxtLink to="/login" class="btn-login">로그인</NuxtLink>
        </template>
      </div>
    </header>

    <!-- 좌: 실제 서비스 / 우: 운영 관제 대시보드 (로그인 페이지에선 대시보드 숨김) -->
    <div class="split" :class="{ solo: hideDash }">
      <main class="container">
        <NuxtPage />
      </main>
      <AdminDashboard v-if="!hideDash" class="dashboard-col" />
    </div>
  </div>
</template>

<script setup>
const { isLoggedIn, username, clearAuth } = useAuth()
const router = useRouter()
const route = useRoute()

// 로그인 페이지에서는 대시보드를 숨겨 화면을 온전히 사용
const hideDash = computed(() => route.path === '/login')

const handleLogout = () => {
  clearAuth()
  router.push('/login')
}
</script>

<style>
:root {
  --ink: #1d1b18;
  --muted: #6b675f;
  --line: #e7e3da;
  --bg: #faf8f3;
  --accent: #c4452f;
  --pod: #0f6e56;
}
* { box-sizing: border-box; }
body { margin: 0; background: var(--bg); color: var(--ink);
  font-family: -apple-system, "Apple SD Gothic Neo", "Pretendard", sans-serif; }
.topbar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 24px;
  border-bottom: 1px solid var(--line);
  background: #fff;
  position: sticky;
  top: 0;
  z-index: 10;
}
.logo { font-size: 22px; letter-spacing: -0.5px; text-decoration: none; color: inherit; font-weight: 700; }
.logo b { color: var(--accent); }
.tagline { font-size: 13px; color: var(--muted); flex: 1; }
.topbar-right { display: flex; align-items: center; gap: 12px; margin-left: auto; }
.user-info { font-size: 13px; color: var(--muted); }
.btn-logout {
  background: none; border: 1px solid var(--line); border-radius: 6px;
  padding: 6px 12px; font-size: 13px; color: var(--muted); cursor: pointer;
}
.btn-logout:hover { background: var(--bg); }
.btn-login {
  background: var(--accent); color: #fff; border-radius: 6px;
  padding: 7px 14px; font-size: 13px; font-weight: 600; text-decoration: none;
}

/* 좌우 6:4 분할 */
.split { display: grid; grid-template-columns: 6fr 4fr; align-items: start; }
.split.solo { grid-template-columns: 1fr; }
.container { max-width: 920px; margin: 0 auto; padding: 28px 24px 64px; width: 100%; }
.dashboard-col { position: sticky; top: 53px; align-self: start; }

/* 좁은 화면에서는 위/아래로 */
@media (max-width: 980px) {
  .split { grid-template-columns: 1fr; }
  .dashboard-col { position: static; }
}

a { color: inherit; text-decoration: none; }
</style>