<template>
  <div>
    <NuxtLink to="/" class="back">← 목록으로</NuxtLink>

    <p v-if="pending" class="state">불러오는 중…</p>
    <p v-else-if="error || data?.error" class="state err">식당 정보를 불러오지 못했습니다.</p>

    <div v-else class="detail">
      <div class="hero" :style="{ backgroundImage: `url(${data.restaurant.imageUrl})` }"></div>
      <h1 class="name">{{ data.restaurant.name }}</h1>
      <div class="cuisine">{{ data.restaurant.cuisine }}</div>
      <p class="desc">{{ data.restaurant.description }}</p>
      <div class="open">{{ data.restaurant.openTime }}</div>

      <PodBadge :pod="data?.servedBy" @refresh="refresh" />

      <div class="form">
        <h2>예약하기</h2>
        <input v-model="name" placeholder="예약자 이름" />
        <input v-model.number="party" type="number" min="1" placeholder="인원수" />
        <button :disabled="sending" @click="reserve">
          {{ sending ? '예약 중…' : '예약하기' }}
        </button>
        <p v-if="result" class="result">
          {{ result.message }} · 응답 Pod: <b>{{ result.servedBy }}</b>
          (이 식당 누적 예약 {{ result.totalReservations }}건)
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
const route = useRoute()
const config = useRuntimeConfig()
const api = config.public.apiBase

const { data, pending, error, refresh } = await useFetch(
  () => `${api}/api/restaurants/${route.params.id}`,
  { server: false }
)

const name = ref('')
const party = ref(2)
const sending = ref(false)
const result = ref(null)

async function reserve() {
  sending.value = true
  result.value = null
  try {
    result.value = await $fetch(`${api}/api/reservations`, {
      method: 'POST',
      body: { restaurantId: Number(route.params.id), customerName: name.value, partySize: party.value }
    })
  } catch (e) {
    result.value = { message: '예약 실패', servedBy: '-', totalReservations: '-' }
  } finally {
    sending.value = false
  }
}
</script>

<style scoped>
.back { color: var(--muted); font-size: 14px; display: inline-block; margin-bottom: 16px; }
.state { color: var(--muted); } .state.err { color: var(--accent); }
.hero { height: 260px; border-radius: 14px; background-size: cover; background-position: center; }
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
</style>
