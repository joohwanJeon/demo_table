<template>
  <div class="auth-wrap">
    <div class="auth-card">
      <div class="brand">demo<b>table</b></div>
      <h2 class="auth-title">회원가입</h2>

      <!-- 1단계: 기본 정보 입력 -->
      <form v-if="step === 'register'" @submit.prevent="handleRegister" class="form">
        <div class="field">
          <label>아이디</label>
          <input v-model="form.username" type="text" placeholder="아이디 입력" required />
        </div>
        <div class="field">
          <label>비밀번호</label>
          <input v-model="form.password" type="password" placeholder="비밀번호 입력 (6자 이상)" required minlength="6" />
        </div>
        <p v-if="errorMsg" class="error">{{ errorMsg }}</p>
        <button type="submit" class="btn-primary" :disabled="loading">
          {{ loading ? '처리 중...' : '가입하기' }}
        </button>
        <p class="link-row">이미 계정이 있으신가요? <NuxtLink to="/login">로그인</NuxtLink></p>
      </form>

      <!-- 2단계: OTP QR코드 등록 -->
      <div v-else-if="step === 'otp-setup'" class="form">
        <div class="qr-section">
          <p class="qr-guide">
            Google Authenticator 앱으로<br/>아래 QR코드를 스캔하세요.
          </p>
          <img v-if="qrCode" :src="`data:image/png;base64,${qrCode}`" class="qr-img" alt="OTP QR코드" />
          <p class="secret-label">또는 키를 직접 입력:</p>
          <code class="secret-code">{{ otpSecret }}</code>
        </div>
        <div class="field">
          <label>앱에서 생성된 6자리 코드 입력</label>
          <input
            v-model="otpCode"
            type="text"
            inputmode="numeric"
            maxlength="6"
            placeholder="6자리 숫자"
            ref="otpInput"
          />
        </div>
        <p v-if="errorMsg" class="error">{{ errorMsg }}</p>
        <button class="btn-primary" :disabled="loading" @click="handleEnableOtp">
          {{ loading ? '확인 중...' : 'OTP 등록 완료' }}
        </button>
        <button class="btn-secondary" @click="skipOtp">나중에 설정하기</button>
      </div>

      <!-- 완료 -->
      <div v-else-if="step === 'done'" class="done-section">
        <div class="done-icon">✅</div>
        <p class="done-msg">{{ otpSkipped ? '회원가입이 완료되었습니다.' : 'Google OTP 등록이 완료되었습니다!' }}</p>
        <NuxtLink to="/login" class="btn-primary" style="text-align:center; display:block;">
          로그인하기
        </NuxtLink>
      </div>
    </div>
  </div>
</template>

<script setup>
definePageMeta({ layout: false })

const config = useRuntimeConfig()

const step = ref('register')
const form = reactive({ username: '', password: '' })
const otpCode = ref('')
const otpSecret = ref('')
const qrCode = ref('')
const errorMsg = ref('')
const loading = ref(false)
const otpSkipped = ref(false)
const otpInput = ref(null)

// 1단계: 회원가입
const handleRegister = async () => {
  loading.value = true
  errorMsg.value = ''
  try {
    await $fetch(`${config.public.apiBase}/api/auth/register`, {
      method: 'POST',
      body: { username: form.username, password: form.password }
    })
    // 회원가입 성공 → OTP 설정 단계
    await loadOtpSetup()
  } catch (e) {
    errorMsg.value = e?.data?.message || '회원가입에 실패했습니다.'
  } finally {
    loading.value = false
  }
}

// OTP QR코드 로드
const loadOtpSetup = async () => {
  loading.value = true
  try {
    const res = await $fetch(
      `${config.public.apiBase}/api/auth/otp/setup?username=${form.username}`
    )
    otpSecret.value = res.secret
    qrCode.value = res.qrCodeBase64
    step.value = 'otp-setup'
    await nextTick()
    otpInput.value?.focus()
  } catch (e) {
    errorMsg.value = 'QR코드 로드에 실패했습니다.'
  } finally {
    loading.value = false
  }
}

// OTP 활성화
const handleEnableOtp = async () => {
  if (otpCode.value.length !== 6) {
    errorMsg.value = '6자리 코드를 입력해주세요.'
    return
  }
  loading.value = true
  errorMsg.value = ''
  try {
    await $fetch(
      `${config.public.apiBase}/api/auth/otp/enable?username=${form.username}&code=${otpCode.value}`,
      { method: 'POST' }
    )
    step.value = 'done'
  } catch (e) {
    errorMsg.value = e?.data?.message || 'OTP 코드가 올바르지 않습니다.'
    otpCode.value = ''
  } finally {
    loading.value = false
  }
}

// OTP 나중에 설정
const skipOtp = () => {
  otpSkipped.value = true
  step.value = 'done'
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
  max-width: 420px;
  box-shadow: 0 8px 32px rgba(0,0,0,.06);
}
.brand { font-size: 24px; letter-spacing: -0.5px; margin-bottom: 8px; }
.brand b { color: var(--accent); }
.auth-title { font-size: 20px; margin: 0 0 28px; }
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
  transition: opacity .15s;
  text-decoration: none;
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
.error { color: var(--accent); font-size: 13px; margin: 0; }
.link-row { font-size: 13px; color: var(--muted); text-align: center; margin: 0; }
.link-row a { color: var(--accent); font-weight: 500; }
.qr-section { display: flex; flex-direction: column; align-items: center; gap: 10px; }
.qr-guide { font-size: 14px; color: var(--muted); text-align: center; line-height: 1.6; margin: 0; }
.qr-img { width: 200px; height: 200px; border: 1px solid var(--line); border-radius: 8px; }
.secret-label { font-size: 12px; color: var(--muted); margin: 0; }
.secret-code {
  background: var(--bg);
  border: 1px solid var(--line);
  border-radius: 6px;
  padding: 6px 12px;
  font-size: 13px;
  letter-spacing: 2px;
  word-break: break-all;
  text-align: center;
}
input[inputmode="numeric"] { letter-spacing: 6px; font-size: 20px; text-align: center; }
.done-section { display: flex; flex-direction: column; align-items: center; gap: 16px; padding: 16px 0; }
.done-icon { font-size: 48px; }
.done-msg { font-size: 15px; color: var(--ink); text-align: center; margin: 0; }
</style>
