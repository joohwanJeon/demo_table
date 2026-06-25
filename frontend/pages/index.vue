<template>
  <div>
    <h1 class="title">오늘의 예약 오픈</h1>
    <p class="sub">인기 식당은 오픈 시각에 동시 접속이 몰립니다.</p>

    <PodBadge :pod="data?.servedBy" @refresh="refresh" />

    <p v-if="pending" class="state">불러오는 중…</p>
    <p v-else-if="error" class="state err">백엔드에 연결하지 못했습니다. API 주소를 확인하세요.</p>

    <div v-else class="grid">
      <NuxtLink
        v-for="(r, i) in topThree"
        :key="r.id"
        :to="`/restaurants/${r.id}`"
        class="card"
      >
        <div class="thumb" :style="{ background: theme(i).bg }">
          <img
            class="photo"
            :src="theme(i).img"
            alt=""
            loading="lazy"
            @error="(e) => (e.target.style.display = 'none')"
          />
          <span class="emoji">{{ theme(i).emoji }}</span>
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
import { computed } from 'vue'
const config = useRuntimeConfig()
const { data, pending, error, refresh } = await useFetch(
  () => `${config.public.apiBase}/api/restaurants`,
  { server: false }
)

// T3-Table — 대표 식당 3곳만 노출
const topThree = computed(() => (data.value?.restaurants ?? []).slice(0, 3))

// 표시용 테마: 0=양식, 1=일식, 2=한식
// 사진(img)이 막히거나 깨지면 그라데이션(bg)+이모지(emoji)가 자연스럽게 대체됨
const THEMES = [
  { img: 'https://images.unsplash.com/photo-1551183053-bf91a1d81141?w=600&q=80&auto=format&fit=crop',
    bg: 'linear-gradient(135deg, #8e2de2 0%, #c4452f 100%)', emoji: '🍝' },
  { img: 'https://images.unsplash.com/photo-1579871494447-9811cf80d66c?w=600&q=80&auto=format&fit=crop',
    bg: 'linear-gradient(135deg, #1d9e75 0%, #0f6e56 100%)', emoji: '🍣' },
  { img: 'https://images.unsplash.com/photo-1583224944844-5dc6cf8e0a0e?w=600&q=80&auto=format&fit=crop',
    bg: 'linear-gradient(135deg, #ba7517 0%, #993c1d 100%)', emoji: '🍚' },
]
const theme = (i) => THEMES[i % THEMES.length]
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
.thumb { height: 140px; position: relative; overflow: hidden;
  display: flex; align-items: center; justify-content: center; }
.photo { position: absolute; inset: 0; width: 100%; height: 100%; object-fit: cover; }
.emoji { font-size: 44px; opacity: .92; filter: drop-shadow(0 2px 6px rgba(0,0,0,.25)); }
.hot { position: absolute; top: 10px; left: 10px; background: var(--accent); color: #fff;
  font-size: 12px; padding: 3px 8px; border-radius: 999px; z-index: 2; }
.body { padding: 12px 14px 16px; }
.name { font-size: 16px; font-weight: 600; }
.cuisine { color: var(--muted); font-size: 13px; margin-top: 2px; }
.open { color: var(--accent); font-size: 12px; margin-top: 8px; }
</style>