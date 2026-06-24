// composables/useAuth.js
// 로그인 상태 및 인증 관련 전역 상태 관리

export const useAuth = () => {
  const token = useCookie('auth_token', { maxAge: 3600, secure: false, sameSite: 'lax' })
  const username = useCookie('auth_user', { maxAge: 3600 })

  const isLoggedIn = computed(() => !!token.value)

  const setAuth = (newToken, user) => {
    token.value = newToken
    username.value = user
  }

  const clearAuth = () => {
    token.value = null
    username.value = null
  }

  return { token, username, isLoggedIn, setAuth, clearAuth }
}
