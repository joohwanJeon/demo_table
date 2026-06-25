<template>
  <div class="auth-wrap">
    <div class="auth-card">
      <div class="brand">demo<b>table</b></div>
      <h2 class="auth-title">로그인</h2>

      <!-- 1단계: ID/PW -->
      <form v-if="step === 'login'" @submit.prevent="handleLogin" class="form">
        <div class="field">  
          <label>아이디</label>
          <input v-model="form.username" type="text" placeholder="아이디 입력" required autocomplete="username" />
        </div>
        <div class="field">
          <label>비밀번호</label>
          <input v-model="form.password" type="password" placeholder="비밀번호 입력" required autocomplete="current-password" />
        </div>
        <p v-if="errorMsg" class="error">{{ errorMsg }}</p>
        <button type="submit" class="btn-primary" :disabled="loading">
          {{ loading ? '확인 중...' : '로그인' }}
        </button>
        <div class="divider"><span>또는</span></div>
        <NuxtLink to="/login-aws" class="btn-aws">
          🔐 AWS 계정으로 로그인 (MFA)
        </NuxtLink>
        <p class="link-row">계정이 없으신가요? <NuxtLink to="/register">회원가입</NuxtLink></p>
      </form>

      <!-- 2단계: Google OTP -->
      <form v-else-if="step === 'otp'" @submit.prevent="handleOtp" class="form">
        <div class="otp-info">
          <div class="otp-icon">🔐</div>
          <p>Google Authenticator 앱에서<br/>6자리 코드를 입력하세요.</p>
        </div>
        <div class="field">
          <label>OTP 코드</label>
          <input
            v-model="otpCode"
            type="text"
            inputmode="numeric"
            maxlength="6"
            placeholder="6자리 숫자"
            required
            autocomplete="one-time-code"
            ref="otpInput"
          />
        </div>
        <p v-if="errorMsg" class="error">{{ errorMsg }}</p>
        <button type="submit" class="btn-primary" :disabled="loading">
          {{ loading ? '확인 중...' : '인증 완료' }}
        </button>
        <button type="button" class="btn-secondary" @click="step = 'login'; errorMsg = ''">
          ← 돌아가기
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
definePageMeta({ layout: false })

const config = useRuntimeConfig()
const { setAuth } = useAuth()
const router = useRouter()

const step = ref('login')
const form = reactive({ username: '', password: '' })
const otpCode = ref('')
const errorMsg = ref('')
const loading = ref(false)
const otpInput = ref(null)

const handleLogin = async () => {
  loading.value = true
  errorMsg.value = ''
  try {
    const res = await $fetch(`${config.public.apiBase}/api/auth/login`, {
      method: 'POST',
      body: { username: form.username, password: form.password }
    })
    if (res.status === 'OTP_REQUIRED') {
      step.value = 'otp'
      await nextTick()
      otpInput.value?.focus()
    } else if (res.status === 'SUCCESS') {
      setAuth(res.token, form.username)
      router.push('/')
    }
  } catch (e) {
    errorMsg.value = e?.data?.message || '로그인에 실패했습니다.'
  } finally {
    loading.value = false
  }
}

const handleOtp = async () => {
  if (otpCode.value.length !== 6) {
    errorMsg.value = '6자리 코드를 입력해주세요.'
    return
  }
  loading.value = true
  errorMsg.value = ''
  try {
    const res = await $fetch(`${config.public.apiBase}/api/auth/verify-otp`, {
      method: 'POST',
      body: { username: form.username, code: parseInt(otpCode.value) }
    })
    setAuth(res.token, form.username)
    router.push('/')
  } catch (e) {
    errorMsg.value = e?.data?.message || 'OTP 코드가 올바르지 않습니다.'
    otpCode.value = ''
  } finally {
    loading.value = false
  }
}
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
}
.brand { font-size: 24px; letter-spacing: -0.5px; margin-bottom: 8px; }
.brand b { color: var(--accent); }
.auth-title { font-size: 20px; margin: 0 0 28px; color: var(--ink); }
.form { display: flex; flex-direction: column; gap: 16px; }
.field { display: flex; flex-direction: column; gap: 6px; }
label { font-size: 13px; color: var(--muted); font-weight: 500; }
input {
  border: 1px solid var(--line);
  border-radius: 8px;
  padding: 10px 14px;
  font-size: 15px;
  outline: none;
  transition: border-color .15s;
}
input:focus { border-color: var(--accent); }
.btn-primary {
  background: var(--accent);
  color: #fff;
  border: none;
  border-radius: 8px;
  padding: 12px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  margin-top: 4px;
  transition: opacity .15s;
}
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-secondary {
  background: none;
  border: 1px solid var(--line);
  border-radius: 8px;
  padding: 10px;
  font-size: 14px;
  color: var(--muted);
  cursor: pointer;
}
.btn-aws {
  background: #232f3e;
  color: #fff;
  border: none;
  border-radius: 8px;
  padding: 12px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  text-align: center;
  text-decoration: none;
  display: block;
}
.btn-aws:hover { opacity: 0.9; }
.error { color: var(--accent); font-size: 13px; margin: 0; }
.link-row { font-size: 13px; color: var(--muted); text-align: center; margin: 0; }
.link-row a { color: var(--accent); font-weight: 500; }
.otp-info { text-align: center; padding: 8px 0 4px; }
.otp-icon { font-size: 36px; margin-bottom: 8px; }
.otp-info p { color: var(--muted); font-size: 14px; line-height: 1.6; margin: 0; }
input[inputmode="numeric"] { letter-spacing: 6px; font-size: 20px; text-align: center; }
.divider {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--muted);
  font-size: 12px;
}
.divider::before, .divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: var(--line);
}
</style>
