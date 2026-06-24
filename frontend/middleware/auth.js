// middleware/auth.js
// 로그인 필요한 페이지 접근 시 /login으로 리다이렉트

export default defineNuxtRouteMiddleware((to) => {
  const { isLoggedIn } = useAuth()

  if (!isLoggedIn.value) {
    return navigateTo('/login')
  }
})
