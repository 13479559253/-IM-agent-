<script setup lang="ts">
import axios from 'axios'
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
import { initWs } from '../utils/ws'  

const isLogin = ref(true)
const loginMode = ref<'password' | 'code'>('password')
const phone = ref<string>('')
const code = ref<string>('')
const password = ref<string>('')
const codeButtonText = ref('获取验证码')
const codeButtonDisabled = ref(false)
const countdown = ref(0)
const toastMessage = ref('')
const toastType = ref<'success' | 'error'>('success')

const showToast = (message: string, type: 'success' | 'error' = 'success') => {
  toastMessage.value = message
  toastType.value = type
  setTimeout(() => {
    toastMessage.value = ''
  }, 1000)
}

const validatePhone = (phoneNum: string): boolean => {
  if (!phoneNum || phoneNum.length === 0) {
    return false
  }
  const phoneRegex = /^[1][0-9]{10}$/
  return phoneRegex.test(phoneNum)
}

const validateCode = (codeNum: string): boolean => {
  const codeRegex = /^\d{4}$/
  return codeRegex.test(codeNum)
}

const validatePassword = (password: string): boolean => {
  if (!password || password.length === 0) {
    showToast('请输入密码', 'error')
    return false
  }
  if (password.length < 6) {
    showToast('密码长度不能少于6位', 'error')
    return false
  }
  if (password.length > 12) {
    showToast('密码长度不能超过12位', 'error')
    return false
  }
  if (!/[0-9]/.test(password)) {
    showToast('密码必须包含数字', 'error')
    return false
  }
  if (!/[a-zA-Z]/.test(password)) {
    showToast('密码必须包含字母', 'error')
    return false
  }
  if (!/^[0-9a-zA-Z]+$/.test(password)) {
    showToast('密码只能包含字母和数字', 'error')
    return false
  }
  return true
}

const getPasswordStrength = (password: string): 'weak' | 'medium' | 'strong' | '' => {
  if (!password) return ''
  
  const hasNumber = /[0-9]/.test(password)
  const hasLetter = /[a-zA-Z]/.test(password)
  
  if (!hasNumber || !hasLetter) {
    return 'weak'
  }
  
  if (password.length < 8) {
    return 'medium'
  }
  
  return 'strong'
}

const sendCode = async () => {
  if (!validatePhone(phone.value)) {
    showToast('请输入正确的手机号','error')
    return
  }
  const type = isLogin.value ? 'LOGIN' : 'REGISTER'
  const response = await axios.get(`http://localhost:8080/user/code?phone=${phone.value}&type=${type}`)
  if(response.data.code !== 1000) {
    showToast(response.data.msg, 'error')
    return;
  } 
  codeButtonDisabled.value = true
  countdown.value = 60
  codeButtonText.value = `${countdown.value}秒`

  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
      codeButtonDisabled.value = false
      codeButtonText.value = '获取验证码'
    } else {
      codeButtonText.value = `${countdown.value}秒`
    }
  }, 1000)
}

const handleSubmit = async () => {
  if (!validatePhone(phone.value)) {
    showToast('电话号码格式错误', 'error')
    return
  }

  if (isLogin.value) {
    if (loginMode.value === 'password') {
      if (!validatePassword(password.value)) {
        showToast('密码格式错误', 'error')
        return
      }
      const response = await axios.post("http://localhost:8080/user/login/password", {
        phone: phone.value,
        password: password.value
      })
      if(response.data.code !== 1000) {
        showToast(response.data.msg, 'error')
        return;
      }
      localStorage.setItem('token', response.data.data)
    } else {
      if (!validateCode(code.value)) {
        showToast('验证码格式错误', 'error')
        return
      }
      const response = await axios.post("http://localhost:8080/user/login/code", {
        phone: phone.value,
        code: code.value
      })
      if(response.data.code !== 1000) {
        showToast(response.data.msg, 'error')
        return;
      }
      localStorage.setItem('token', response.data.data)
    }
  } else {
    if (!validateCode(code.value)) {
      showToast('验证码格式错误', 'error')
      return
    }
    if (!validatePassword(password.value)) {
      showToast('密码格式错误', 'error')
      return
    }
    const response = await axios.post("http://localhost:8080/user/register", {
      phone: phone.value,
      code: code.value,
      password: password.value
    })
    if(response.data.code !== 1000) {
      showToast(response.data.msg, 'error')
      return;
    }
  }
  
  if (localStorage.getItem('token')) {
    initWs(localStorage.getItem('token')) 
  }
  
  showToast(isLogin.value ? '登录成功' : '注册成功', 'success')
  if(!isLogin.value) {
    isLogin.value = !isLogin.value
  }
  else{
    setTimeout(() => {
      router.replace('/chats')
    }, 1000)
  }
}

const toggleMode = () => {
  isLogin.value = !isLogin.value
  phone.value = ''
  code.value = ''
  password.value = ''
}
</script>

<template>
  <div class="login-container">
    <div v-if="toastMessage" class="toast" :class="toastType">
      {{ toastMessage }}
    </div>
    <div class="login-box">
      <div class="logo">
        <div class="logo-icon">
          <span class="logo-dot"></span>
          <span class="logo-wave"></span>
        </div>
      </div>
      <h1 class="title">{{ isLogin ? '登录' : '注册' }}</h1>
      
      <div class="form-group">
        <label>手机号</label>
        <input
          v-model="phone"
          type="tel"
          placeholder="请输入手机号"
          maxlength="11"
          class="form-input"
          @keyup.enter="handleSubmit"
        />
      </div>

      <div v-if="isLogin && loginMode === 'password'" class="form-group">
        <label>密码</label>
        <input
          v-model="password"
          type="password"
          placeholder="请输入密码"
          maxlength="12"
          class="form-input"
          @keyup.enter="handleSubmit"
        />
      </div>

      <div v-if="isLogin && loginMode === 'code'" class="form-group">
        <label>验证码</label>
        <div class="code-input-wrapper">
          <input
          v-model="code"
          type="tel"
          placeholder="请输入验证码"
          maxlength="4"
          class="form-input code-input"
          @keyup.enter="handleSubmit"
        />
          <button
            @click="sendCode"
            :disabled="codeButtonDisabled || !phone"
            class="code-button"
          >
            {{ codeButtonText }}
          </button>
        </div>
      </div>

      <div v-if="!isLogin" class="form-group">
        <label>验证码</label>
        <div class="code-input-wrapper">
          <input
          v-model="code"
          type="tel"
          placeholder="请输入验证码"
          maxlength="4"
          class="form-input code-input"
        />
          <button
            @click="sendCode"
            :disabled="codeButtonDisabled || !phone"
            class="code-button"
          >
            {{ codeButtonText }}
          </button>
        </div>
      </div>

      <div v-if="!isLogin" class="form-group">
        <label>密码</label>
        <input
          v-model="password"
          type="password"
          placeholder="密码（6-12位，需包含字母和数字）"
          maxlength="12"
          class="form-input"
          @keyup.enter="handleSubmit"
        />
        <div v-if="password" class="password-strength">
          <div class="strength-label">密码强度：{{ getPasswordStrength(password) === 'weak' ? '弱' : getPasswordStrength(password) === 'medium' ? '中' : '强' }}</div>
          <div class="strength-bar">
            <div 
              class="strength-item weak" 
              :class="{ active: getPasswordStrength(password) !== '' }"
            ></div>
            <div 
              class="strength-item medium" 
              :class="{ active: getPasswordStrength(password) === 'medium' || getPasswordStrength(password) === 'strong' }"
            ></div>
            <div 
              class="strength-item strong" 
              :class="{ active: getPasswordStrength(password) === 'strong' }"
            ></div>
          </div>
        </div>
      </div>

      <button @click="handleSubmit" class="submit-button">
        {{ isLogin ? '登录' : '注册' }}
      </button>

      <p v-if="isLogin" class="toggle-text">
        <span @click="loginMode = loginMode === 'password' ? 'code' : 'password'" class="toggle-link">
          {{ loginMode === 'password' ? '验证码登录' : '密码登录' }}
        </span>
        <span class="divider">|</span>
        <span @click="toggleMode" class="toggle-link">
          立即注册
        </span>
      </p>
      <p v-else class="toggle-text">
        已有账号？
        <span @click="toggleMode" class="toggle-link">
          立即登录
        </span>
      </p>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
  padding: 20px;
}

.login-box {
  position: relative;
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  width: 100%;
  max-width: 420px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.logo {
  position: absolute;
  top: 20px;
  left: 20px;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-icon {
  position: relative;
  width: 32px;
  height: 32px;
}

.logo-dot {
  position: absolute;
  width: 10px;
  height: 10px;
  background: #3b82f6;
  border-radius: 50%;
  top: 50%;
  left: 6px;
  transform: translateY(-50%);
  animation: pulse 2s ease-in-out infinite;
}

.logo-wave {
  position: absolute;
  width: 16px;
  height: 16px;
  border: 3px solid #22c55e;
  border-radius: 50%;
  top: 50%;
  right: 0;
  transform: translateY(-50%);
  animation: wave 2s ease-in-out infinite;
}

.logo-wave::before {
  content: '';
  position: absolute;
  width: 6px;
  height: 6px;
  background: #22c55e;
  border-radius: 50%;
  top: 2px;
  right: 2px;
}

@keyframes pulse {
  0%, 100% {
    transform: translateY(-50%) scale(1);
    opacity: 1;
  }
  50% {
    transform: translateY(-50%) scale(1.2);
    opacity: 0.8;
  }
}

@keyframes wave {
  0%, 100% {
    transform: translateY(-50%) scale(1);
    border-color: #22c55e;
  }
  50% {
    transform: translateY(-50%) scale(1.1);
    border-color: #16a34a;
  }
}

.title {
  text-align: center;
  font-size: 1.8rem;
  color: #000;
  margin-bottom: 32px;
  font-weight: 600;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
  font-size: 0.9rem;
}

.form-input {
  width: 100%;
  padding: 12px 16px;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 1rem;
  color: #000;
  outline: none;
  transition: all 0.2s ease;
}

.form-input::placeholder {
  color: #999;
}

.form-input:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.toast {
  position: fixed;
  top: 15%;
  left: 50%;
  transform: translateX(-50%);
  padding: 12px 24px;
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 500;
  text-align: center;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
  animation: toastIn 0.3s ease;
  z-index: 1000;
}

.toast.success {
  background: #dcfce7;
  color: #166534;
  border: 1px solid #bbf7d0;
}

.toast.error {
  background: #fee2e2;
  color: #991b1b;
  border: 1px solid #fecaca;
}

@keyframes toastIn {
  from {
    opacity: 0;
    transform: translateX(-50%) translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(-50%) translateY(0);
  }
}

.code-input-wrapper {
  display: flex;
  gap: 12px;
}

.code-input {
  flex: 1;
}

.code-button {
  padding: 12px 20px;
  background: #3b82f6;
  color: #fff;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 500;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.code-button:hover:not(:disabled) {
  background: #2563eb;
}

.code-button:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.submit-button {
  width: 100%;
  padding: 14px;
  background: #22c55e;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.submit-button:hover {
  background: #16a34a;
}

.toggle-text {
  text-align: center;
  margin-top: 24px;
  color: #666;
  font-size: 0.9rem;
}

.divider {
  margin: 0 8px;
  color: #ccc;
}

.toggle-link {
  color: #3b82f6;
  cursor: pointer;
  font-weight: 500;
}

.toggle-link:hover {
  color: #2563eb;
  text-decoration: underline;
}

.password-strength {
  margin-top: 10px;
}

.strength-label {
  font-size: 0.85rem;
  color: #666;
  margin-bottom: 6px;
}

.strength-bar {
  display: flex;
  gap: 4px;
}

.strength-item {
  flex: 1;
  height: 4px;
  border-radius: 2px;
  background: #eee;
  transition: all 0.2s ease;
}

.strength-item.active.weak {
  background: #dc2626;
}

.strength-item.active.medium {
  background: #f59e0b;
}

.strength-item.active.strong {
  background: #22c55e;
}
</style>