<template>
  <div>
    <!-- 오픈런 대기열 오버레이 (진입 시 2초, 숫자 카운트다운) -->
    <transition name="fade">
      <div v-if="queueing" class="queue-overlay">
        <div class="queue-box">
          <div class="queue-spinner"></div>
          <div class="queue-title">예약 대기 중</div>
          <div class="queue-count">내 앞에 <b>{{ waiting }}</b>명</div>
          <div class="queue-sub">잠시만 기다려 주세요…</div>
        </div>
      </div>
    </transition>

    <NuxtLink to="/" class="back">← 목록으로</NuxtLink>

    <p v-if="pending" class="state">불러오는 중…</p>
    <p v-else-if="error || data?.error" class="state err">식당 정보를 불러오지 못했습니다.</p>

    <div v-else class="detail">
      <div class="hero" :style="{ backgroundImage: `url(${data.restaurant.imageUrl})` }"></div>
      <h1 class="name">{{ data.restaurant.name }}</h1>
      <div class="cuisine">{{ data.restaurant.cuisine }}</div>
      <p class="desc">{{ data.restaurant.description }}</p>
      <div class="open">{{ data.restaurant.openTime }}</div>

      <div class="form">
        <h2>예약하기</h2>
        <div v-if="!isLoggedIn" class="login-notice">
          <p>예약하려면 로그인이 필요합니다.</p>
          <NuxtLink to="/login" class="btn-login-link">로그인하기</NuxtLink>
        </div>
        <template v-else>
          <input v-model="name" placeholder="예약자 이름" />
          <input v-model.number="party" type="number" min="1" placeholder="인원수" />
          <button :disabled="sending" @click="reserve">
            {{ sending ? '예약 중…' : '예약하기' }}
          </button>
          <p v-if="result" class="result">
            (이 식당 누적 예약 {{ result.totalReservations }}건)
          </p>
          <p v-if="reserveError" class="err-msg">{{ reserveError }}</p>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
const route = useRoute()
const config = useRuntimeConfig()
const api = config.public.apiBase
const { isLoggedIn, token } = useAuth()

const { data, pending, error, refresh } = await useFetch(
  () => `${api}/api/restaurants/${route.params.id}`,
  { server: false }
)

// 오픈런 대기열 연출: 진입 시 2초간 대기 인원이 0으로 줄며 사라짐
const queueing = ref(true)
const waiting = ref(Math.floor(Math.random() * 400) + 120) // 시작 인원 120~519
const DURATION = 2000   // 총 대기 2초
let timer = null

onMounted(() => {
  const start = waiting.value
  const t0 = Date.now()
  timer = setInterval(() => {
    const p = Math.min((Date.now() - t0) / DURATION, 1)
    // ease-out 느낌으로 줄어듦
    waiting.value = Math.round(start * (1 - p))
    if (p >= 1) {
      clearInterval(timer)
      timer = null
      queueing.value = false
    }
  }, 40)
})
onBeforeUnmount(() => { if (timer) clearInterval(timer) })

const name = ref('')
const party = ref(2)
const sending = ref(false)
const result = ref(null)
const reserveError = ref('')

async function reserve() {
  sending.value = true
  result.value = null
  reserveError.value = ''
  try {
    result.value = await $fetch(`${api}/api/reservations`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${token.value}` },
      body: { restaurantId: Number(route.params.id), customerName: name.value, partySize: party.value }
    })
  } catch (e) {
    if (e?.status === 401) {
      reserveError.value = '로그인이 만료되었습니다. 다시 로그인해주세요.'
    } else {
      reserveError.value = '예약에 실패했습니다.'
    }
  } finally {
    sending.value = false
  }
}
</script>

<style scoped>
.back { color: var(--muted); font-size: 14px; display: inline-block; margin-bottom: 16px; }
.state { color: var(--muted); } .state.err { color: var(--accent); }
.hero { height: 260px; border-radius: 14px; background-size: cover; background-position: center;
  background-color: #e7e3da; }
.name { font-size: 26px; margin: 18px 0 2px; }
.cuisine { color: var(--muted); }
.desc { line-height: 1.6; margin: 14px 0; }
.open { color: var(--accent); font-size: 13px; margin-bottom: 8px; }
.form { background: #fff; border: 1px solid var(--line); border-radius: 12px; padding: 18px 20px; margin-top: 20px; }
.form h2 { font-size: 17px; margin: 0 0 12px; }
.form input { display: block; width: 100%; padding: 10px 12px; margin-bottom: 10px;
  border: 1px solid var(--line); border-radius: 8px; font-size: 15px; }
.form button { background: var(--accent); color: #fff; border: 0; border-radius: 8px;
  padding: 11px 18px; font-size: 15px; cursor: pointer; }
.form button:disabled { opacity: .6; cursor: default; }
.result { margin-top: 12px; color: var(--pod); font-size: 14px; }
.err-msg { color: var(--accent); font-size: 13px; margin-top: 8px; }
.login-notice { text-align: center; padding: 16px 0; }
.login-notice p { color: var(--muted); font-size: 14px; margin: 0 0 12px; }
.btn-login-link { background: var(--accent); color: #fff; border-radius: 8px;
  padding: 10px 20px; font-size: 14px; font-weight: 600; }

/* 대기열 오버레이 */
.queue-overlay {
  position: fixed; inset: 0; z-index: 999;
  background: rgba(20, 16, 14, 0.72); backdrop-filter: blur(3px);
  display: flex; align-items: center; justify-content: center;
}
.queue-box { text-align: center; color: #fff; padding: 32px 48px; }
.queue-spinner {
  width: 44px; height: 44px; margin: 0 auto 18px;
  border: 4px solid rgba(255,255,255,0.25); border-top-color: var(--accent);
  border-radius: 50%; animation: spin 0.7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
.queue-title { font-size: 15px; letter-spacing: 1px; color: #f0d9c0; margin-bottom: 8px; }
.queue-count { font-size: 26px; font-weight: 700; }
.queue-count b { color: #ffb27a; }
.queue-sub { font-size: 13px; color: rgba(255,255,255,0.6); margin-top: 8px; }

.fade-enter-active, .fade-leave-active { transition: opacity 0.4s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>