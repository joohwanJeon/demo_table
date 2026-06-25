<!-- components/AdminDashboard.vue -->
<!-- 시연용 운영 관제 대시보드: 로드밸런싱 / 오토스케일링 / Self-healing -->
<template>
  <aside class="dash">
    <div class="dash-head">
      <span class="dot" :class="{ live: polling }"></span>
      <span class="dash-title">CLUSTER CONSOLE</span>
      <span class="dash-sub">team3 · live</span>
    </div>

    <!-- 패널 1: Pod 응답 모니터 (로드밸런싱) -->
    <section class="panel">
      <div class="panel-h">
        <span class="ph-label">RESPONDING PODS</span>
        <button class="mini" @click="togglePoll">{{ polling ? '⏸ 정지' : '▶ 폴링' }}</button>
      </div>
      <div class="now">
        <span class="now-label">now</span>
        <span class="now-pod" :style="{ color: podColor(current) }">{{ current || '—' }}</span>
      </div>
      <div class="pods">
        <div v-for="p in podStats" :key="p.name" class="podrow">
          <span class="pdot" :style="{ background: podColor(p.name) }"></span>
          <span class="pname">{{ shortPod(p.name) }}</span>
          <span class="pbar"><i :style="{ width: barW(p.count) + '%', background: podColor(p.name) }"></i></span>
          <span class="pcount">{{ p.count }}</span>
        </div>
        <p v-if="podStats.length === 0" class="empty">요청을 보내면 응답 Pod가 집계됩니다.</p>
      </div>
      <p class="hint">새로고침마다 응답 Pod가 분산되는 것이 로드밸런싱입니다.</p>
    </section>

    <!-- 패널 2: 부하 테스트 (오토스케일링) -->
    <section class="panel">
      <div class="panel-h"><span class="ph-label">LOAD TEST · 오토스케일링</span></div>
      <p class="desc">예약 오픈런처럼 트래픽을 집중시켜 HPA 스케일아웃을 유도합니다.</p>
      <button class="act act-load" :class="{ on: loading }" @click="toggleLoad">
        {{ loading ? '■ 부하 중지' : '▲ 부하 생성' }}
      </button>
      <div class="metrics">
        <div><span>총 호출</span><b>{{ loadCount }}</b></div>
        <div><span>관측 Pod</span><b>{{ podStats.length }}</b></div>
      </div>
      <p class="hint">관측 Pod 수가 늘면 스케일아웃입니다. 정확한 CPU·개수는 kubectl / Grafana로 확인하세요.</p>
    </section>

    <!-- 패널 3: Self-healing -->
    <section class="panel">
      <div class="panel-h"><span class="ph-label">SELF-HEALING</span></div>
      <p class="desc">현재 응답 Pod를 강제 종료합니다. K8s가 자동으로 새 Pod를 복원합니다.</p>
      <button class="act act-crash" @click="crash">⚠ 장애 발생 (Pod 종료)</button>
      <p class="hint">종료 후 위 모니터에 새 Pod가 등장하면 자동 복구된 것입니다. 복구 과정은 kubectl get pods -w로 함께 보세요.</p>
    </section>

    <!-- 패널 4: 실시간 로그 -->
    <section class="panel log-panel">
      <div class="panel-h">
        <span class="ph-label">LIVE LOG</span>
        <button class="mini" @click="logs = []">clear</button>
      </div>
      <div class="log" ref="logEl">
        <div v-for="(l, i) in logs" :key="i" class="logline" :class="l.kind">
          <span class="t">{{ l.t }}</span>
          <span class="m">{{ l.m }}</span>
        </div>
        <p v-if="logs.length === 0" class="empty">대기 중…</p>
      </div>
    </section>
  </aside>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'

const config = useRuntimeConfig()
const base = config.public.apiBase || ''

// ── 부하 강도 (필요시 이 두 값만 조절) ──
const CONCURRENCY = 30      // 한 번에 동시 호출 수
const INTERVAL_MS = 500     // 발사 주기(ms)  → 초당 약 60건
const LOAD_MS = 2000        // 각 호출이 CPU 태우는 시간(백엔드 최대치)

const polling = ref(true)
const loading = ref(false)
const current = ref('')
const loadCount = ref(0)
const counts = ref({})       // { podName: n }
const logs = ref([])
const logEl = ref(null)

let pollTimer = null
let loadTimer = null

const podStats = computed(() =>
  Object.entries(counts.value)
    .map(([name, count]) => ({ name, count }))
    .sort((a, b) => b.count - a.count)
)
const maxCount = computed(() => Math.max(1, ...podStats.value.map(p => p.count)))
const barW = (c) => Math.round((c / maxCount.value) * 100)

const PALETTE = ['#5DCAA5', '#85B7EB', '#EF9F27', '#F0997B', '#AFA9EC', '#F4C0D1']
const colorMap = {}
function podColor(name) {
  if (!name) return '#5F5E5A'
  if (!colorMap[name]) colorMap[name] = PALETTE[Object.keys(colorMap).length % PALETTE.length]
  return colorMap[name]
}
const shortPod = (n) => n ? n.replace('team3-backend-', '…') : n

function log(m, kind = '') {
  const t = new Date().toLocaleTimeString('ko-KR', { hour12: false })
  logs.value.push({ t, m, kind })
  if (logs.value.length > 200) logs.value.shift()
  nextTick(() => { if (logEl.value) logEl.value.scrollTop = logEl.value.scrollHeight })
}
function tally(pod) {
  if (!pod) return
  counts.value = { ...counts.value, [pod]: (counts.value[pod] || 0) + 1 }
}

async function ping() {
  try {
    const r = await fetch(`${base}/api/health`, { cache: 'no-store' })
    const j = await r.json()
    current.value = j.servedBy
    tally(j.servedBy)
  } catch (e) {
    current.value = ''
    log('health 호출 실패 (Pod 응답 없음)', 'err')
  }
}

function togglePoll() {
  polling.value = !polling.value
  polling.value ? startPoll() : stopPoll()
  log(polling.value ? '폴링 시작' : '폴링 정지')
}
function startPoll() { stopPoll(); pollTimer = setInterval(ping, 2000); ping() }
function stopPoll() { if (pollTimer) clearInterval(pollTimer); pollTimer = null }

function toggleLoad() {
  loading.value = !loading.value
  if (loading.value) {
    log(`부하 생성 시작 — 동시 ${CONCURRENCY} × ${1000 / INTERVAL_MS}회/초`, 'load')
    loadTimer = setInterval(fireLoad, INTERVAL_MS)
  } else {
    if (loadTimer) clearInterval(loadTimer); loadTimer = null
    log('부하 중지', 'load')
  }
}
function fireLoad() {
  for (let i = 0; i < CONCURRENCY; i++) {
    fetch(`${base}/api/load?ms=${LOAD_MS}`, { cache: 'no-store' })
      .then(r => r.json())
      .then(j => { loadCount.value++; tally(j.servedBy) })
      .catch(() => {})
  }
}

async function crash() {
  const target = current.value || '(현재 Pod)'
  log(`장애 주입 → ${shortPod(target)} 종료 요청`, 'crash')
  try {
    await fetch(`${base}/api/crash`, { cache: 'no-store' })
  } catch (e) {
    // crash는 응답 없이 죽는 게 정상 → 에러 무시
  }
  log('종료됨. K8s 자동 복구 관찰 중…', 'crash')
}

onMounted(() => { startPoll(); log('콘솔 연결됨') })
onBeforeUnmount(() => { stopPoll(); if (loadTimer) clearInterval(loadTimer) })
</script>

<style scoped>
.dash {
  background: #14171c;
  color: #c9cdd4;
  border-left: 1px solid #2a2f37;
  padding: 16px 16px 28px;
  font-family: ui-monospace, "SF Mono", "JetBrains Mono", Menlo, monospace;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.dash-head { display: flex; align-items: center; gap: 8px; padding-bottom: 6px; border-bottom: 1px solid #2a2f37; }
.dot { width: 8px; height: 8px; border-radius: 50%; background: #5f5e5a; }
.dot.live { background: #5DCAA5; box-shadow: 0 0 8px #5DCAA5; animation: pulse 1.6s infinite; }
@keyframes pulse { 50% { opacity: .4; } }
.dash-title { font-size: 13px; letter-spacing: 1px; color: #e6e8ec; font-weight: 600; }
.dash-sub { font-size: 11px; color: #6b727d; margin-left: auto; }

.panel { background: #1a1e24; border: 1px solid #2a2f37; border-radius: 10px; padding: 12px; }
.panel-h { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
.ph-label { font-size: 11px; letter-spacing: 1px; color: #8b93a0; font-weight: 600; }
.mini { background: #232830; border: 1px solid #353b45; color: #aab0ba; font-size: 11px;
  border-radius: 6px; padding: 3px 8px; cursor: pointer; font-family: inherit; }
.mini:hover { background: #2a3038; }

.now { display: flex; align-items: baseline; gap: 8px; margin-bottom: 10px; }
.now-label { font-size: 11px; color: #6b727d; }
.now-pod { font-size: 15px; font-weight: 600; word-break: break-all; }

.pods { display: flex; flex-direction: column; gap: 6px; }
.podrow { display: flex; align-items: center; gap: 8px; font-size: 12px; }
.pdot { width: 8px; height: 8px; border-radius: 2px; flex: none; }
.pname { width: 84px; color: #aab0ba; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.pbar { flex: 1; height: 8px; background: #232830; border-radius: 4px; overflow: hidden; }
.pbar i { display: block; height: 100%; border-radius: 4px; transition: width .3s ease; }
.pcount { width: 28px; text-align: right; color: #e6e8ec; }
.empty { font-size: 12px; color: #5b626c; margin: 6px 0 0; }
.hint { font-size: 11px; color: #6b727d; margin: 8px 0 0; line-height: 1.5; }
.desc { font-size: 12px; color: #9aa1ab; margin: 0 0 10px; line-height: 1.5; }

.act { width: 100%; border: none; border-radius: 8px; padding: 10px; font-size: 13px; font-weight: 600;
  cursor: pointer; font-family: inherit; transition: filter .12s; }
.act:hover { filter: brightness(1.1); }
.act-load { background: #BA7517; color: #fff; }
.act-load.on { background: #EF9F27; color: #2a1c00; }
.act-crash { background: #A32D2D; color: #fff; }

.metrics { display: flex; gap: 10px; margin: 10px 0 0; }
.metrics div { flex: 1; background: #232830; border-radius: 8px; padding: 8px; text-align: center; }
.metrics span { display: block; font-size: 10px; color: #6b727d; }
.metrics b { font-size: 18px; color: #e6e8ec; }

.log-panel { flex: 1; display: flex; flex-direction: column; min-height: 160px; }
.log { flex: 1; overflow-y: auto; background: #0f1216; border-radius: 8px; padding: 8px; max-height: 240px; }
.logline { font-size: 11px; line-height: 1.6; display: flex; gap: 8px; }
.logline .t { color: #5b626c; flex: none; }
.logline .m { color: #aab0ba; }
.logline.err .m { color: #F0997B; }
.logline.load .m { color: #EF9F27; }
.logline.crash .m { color: #F09595; }
</style>