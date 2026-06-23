<template>
  <div class="app">
    <header class="topbar">
      <span class="logo">demo<b>table</b></span>
      <span class="tagline">예약 오픈런 데모</span>
    </header>
    <main class="container">
      <NuxtPage />
    </main>
  </div>
</template>

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
.topbar { display: flex; align-items: baseline; gap: 12px;
  padding: 18px 24px; border-bottom: 1px solid var(--line); background: #fff; }
.logo { font-size: 22px; letter-spacing: -0.5px; }
.logo b { color: var(--accent); }
.tagline { font-size: 13px; color: var(--muted); }
.container { max-width: 920px; margin: 0 auto; padding: 28px 24px 64px; }
a { color: inherit; text-decoration: none; }
</style>
