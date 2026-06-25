<template>
  <div class="auth-wrap">
    <div class="auth-card">
      <div class="brand">demo<b>table</b></div>
      <p class="msg">{{ message }}</p>
    </div>
  </div>
</template>

<script setup>
definePageMeta({ layout: false })

const route = useRoute()
const { setAuth } = useAuth()
const router = useRouter()
const message = ref('로그인 처리 중...')

onMounted(async () => {
  const code = route.query.code
  if (!code) {
    message.value = '인증 코드가 없습니다. 다시 로그인해주세요.'
    setTimeout(() => router.push('/login'), 2000)
    return
  }

  try {
    const domain = 'ap-northeast-2ykvr8xedc.auth.ap-northeast-2.amazoncognito.com'
    const clientId = '4862re924c65u3j50fjo5hm0c7'
    const redirectUri = 'http://localhost/callback'

    const res = await $fetch(`https://${domain}/oauth2/token`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: new URLSearchParams({
        grant_type: 'authorization_code',
        client_id: clientId,
        code: code,
        redirect_uri: redirectUri
      }).toString()
    })

    // ID 토큰에서 username 추출
    const payload = JSON.parse(atob(res.id_token.split('.')[1]))
    const username = payload['cognito:username'] || payload.sub

    setAuth(res.access_token, username)
    message.value = '로그인 성공! 메인 페이지로 이동합니다.'
    router.push('/')
  } catch (e) {
    message.value = '로그인 처리 중 오류가 발생했습니다.'
    setTimeout(() => router.push('/login'), 2000)
  }
})
</script>

<style scoped>
.auth-wrap {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg);
}
.auth-card {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 16px;
  padding: 40px 36px;
  width: 100%;
  max-width: 400px;
  box-shadow: 0 8px 32px rgba(0,0,0,.06);
  text-align: center;
}
.brand { font-size: 24px; letter-spacing: -0.5px; margin-bottom: 16px; }
.brand b { color: var(--accent); }
.msg { color: var(--muted); font-size: 15px; }
</style>
