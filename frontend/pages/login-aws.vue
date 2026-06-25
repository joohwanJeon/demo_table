<template>
  <div class="auth-wrap">
    <div class="auth-card">
      <div class="brand">demo<b>table</b></div>
      <h2 class="auth-title">AWS 계정으로 로그인</h2>
      <p class="desc">AWS IAM 계정 + Google OTP 2차 인증으로 로그인합니다.</p>
      <button class="btn-primary" @click="cognitoLogin">
        🔐 AWS 계정으로 로그인 (MFA)
      </button>
      <div class="divider"><span>또는</span></div>
      <NuxtLink to="/login" class="btn-secondary">
        일반 계정으로 로그인
      </NuxtLink>
    </div>
  </div>
</template>

<script setup>
definePageMeta({ layout: false })

const cognitoLogin = () => {
  const domain = 'ap-northeast-2ykvr8xedc.auth.ap-northeast-2.amazoncognito.com'
  const clientId = '4862re924c65u3j50fjo5hm0c7'
  const redirectUri = encodeURIComponent('http://localhost/callback')
  window.location.href = `https://${domain}/login?client_id=${clientId}&response_type=code&scope=openid+profile&redirect_uri=${redirectUri}`
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
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.brand { font-size: 24px; letter-spacing: -0.5px; }
.brand b { color: var(--accent); }
.auth-title { font-size: 20px; margin: 0; color: var(--ink); }
.desc { font-size: 13px; color: var(--muted); margin: 0; line-height: 1.6; }
.btn-primary {
  background: var(--accent);
  color: #fff;
  border: none;
  border-radius: 8px;
  padding: 14px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity .15s;
}
.btn-primary:hover { opacity: 0.9; }
.btn-secondary {
  background: none;
  border: 1px solid var(--line);
  border-radius: 8px;
  padding: 12px;
  font-size: 14px;
  color: var(--muted);
  text-align: center;
  text-decoration: none;
}
.btn-secondary:hover { background: var(--bg); }
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
