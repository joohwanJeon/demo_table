export default defineNuxtConfig({
  compatibilityDate: '2025-01-01',
  ssr: false,
  devtools: { enabled: true },
  runtimeConfig: {
    public: {
      // 비워두면 API 호출이 /api/... 상대경로가 됨 → Nginx가 백엔드로 프록시.
      // 로컬 개발 시엔 NUXT_PUBLIC_API_BASE=http://localhost:8080 으로 주입해서 실행.
      apiBase: process.env.NUXT_PUBLIC_API_BASE || ''
    }
  }
})
