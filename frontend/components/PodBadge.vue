<template>
  <div class="podbadge">
    <span class="dot"></span>
    <span class="label">응답한 Pod</span>
    <span class="pod">{{ pod || '연결 안 됨' }}</span>
    <button class="refresh" @click="$emit('refresh')">새로고침</button>
  </div>
</template>

<script setup>
// 로드밸런싱 증적용 위젯.
// 새로고침을 누를 때마다 응답 Pod 이름이 바뀌면 트래픽이 여러 Pod로 분산되고 있다는 증거.
defineProps({ pod: { type: String, default: '' } })
defineEmits(['refresh'])
</script>

<style scoped>
.podbadge { display: inline-flex; align-items: center; gap: 8px;
  background: #fff; border: 1px solid var(--line); border-radius: 999px;
  padding: 6px 8px 6px 14px; margin: 14px 0; font-size: 13px; }
.dot { width: 8px; height: 8px; border-radius: 50%; background: var(--pod); }
.label { color: var(--muted); }
.pod { font-family: ui-monospace, monospace; color: var(--pod); font-weight: 600; }
.refresh { border: 0; background: #f1efe8; border-radius: 999px;
  padding: 5px 12px; font-size: 12px; cursor: pointer; }
</style>
