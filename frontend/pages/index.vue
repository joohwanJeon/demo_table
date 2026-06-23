<template>
  <div>
    <h1 class="title">오늘의 예약 오픈</h1>
    <p class="sub">인기 식당은 오픈 시각에 동시 접속이 몰립니다.</p>

    <PodBadge :pod="data?.servedBy" @refresh="refresh" />

    <p v-if="pending" class="state">불러오는 중…</p>
    <p v-else-if="error" class="state err">백엔드에 연결하지 못했습니다. API 주소를 확인하세요.</p>

    <div v-else class="grid">
      <NuxtLink
        v-for="r in data.restaurants"
        :key="r.id"
        :to="`/restaurants/${r.id}`"
        class="card"
      >
        <div class="thumb" :style="{ backgroundImage: `url(${r.imageUrl})` }">
          <span v-if="r.popular" class="hot">오픈런</span>
        </div>
        <div class="body">
          <div class="name">{{ r.name }}</div>
          <div class="cuisine">{{ r.cuisine }}</div>
          <div class="open">{{ r.openTime }}</div>
        </div>
      </NuxtLink>
    </div>
  </div>
</template>

<script setup>
const config = useRuntimeConfig()
const { data, pending, error, refresh } = await useFetch(
  () => `${config.public.apiBase}/api/restaurants`,
  { server: false }
)
</script>

<style scoped>
.title { font-size: 28px; margin: 0 0 4px; letter-spacing: -0.5px; }
.sub { color: var(--muted); margin: 0 0 18px; }
.state { color: var(--muted); }
.state.err { color: var(--accent); }
.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 18px; }
.card { background: #fff; border: 1px solid var(--line); border-radius: 12px; overflow: hidden;
  transition: transform .12s ease, box-shadow .12s ease; }
.card:hover { transform: translateY(-2px); box-shadow: 0 6px 18px rgba(0,0,0,.06); }
.thumb { height: 140px; background-size: cover; background-position: center; position: relative; }
.hot { position: absolute; top: 10px; left: 10px; background: var(--accent); color: #fff;
  font-size: 12px; padding: 3px 8px; border-radius: 999px; }
.body { padding: 12px 14px 16px; }
.name { font-size: 16px; font-weight: 600; }
.cuisine { color: var(--muted); font-size: 13px; margin-top: 2px; }
.open { color: var(--accent); font-size: 12px; margin-top: 8px; }
</style>
