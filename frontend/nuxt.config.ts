export default defineNuxtConfig({
  compatibilityDate: '2025-01-01',
  devtools: { enabled: true },
  runtimeConfig: {
    public: {
      // 백엔드(Spring Boot) API 주소.
      // 로컬: http://localhost:8080
      // 배포: ALB(Ingress) 주소를 NUXT_PUBLIC_API_BASE 환경변수로 주입
      apiBase: process.env.NUXT_PUBLIC_API_BASE || 'http://localhost:8080'
    }
  }
})
