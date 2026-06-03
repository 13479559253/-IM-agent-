<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { service } from '../utils/axios'
import { jwtDecode } from 'jwt-decode'

interface UserInfo {
  id: number
  nickname: string
  phone: string
  email: string | null
}

const route = useRoute()
const userInfo = ref<UserInfo | null>(null)
const loading = ref(false)
const toastMessage = ref('')
const toastType = ref<'success' | 'error'>('success')
const editNickname = ref('')
const editEmail = ref('')

const targetUserId = computed(() => {
  const routeUserId = Number(route.params.userId)
  if (routeUserId) return routeUserId
  
  const token = localStorage.getItem('token')
  if (!token) return null
  
  const decode = jwtDecode(token)
  return (decode as { id: number }).id
})

const showToast = (message: string, type: 'success' | 'error' = 'success') => {
  toastMessage.value = message
  toastType.value = type
  setTimeout(() => {
    toastMessage.value = ''
  }, 2000)
}

const loadUserInfo = async () => {
  if (!targetUserId.value) {
    loading.value = false
    return
  }
  
  loading.value = true
  
  try {
    const { code, data, msg } = await service.get('/user/info', { params: { userId: targetUserId.value } })
    
    if (code === 1000) {
      userInfo.value = data
      editNickname.value = data.nickname
      editEmail.value = data.email || ''
    } else {
      showToast(msg || '加载用户信息失败')
    }
  } catch (error) {
    showToast('加载用户信息失败')
  } finally {
    loading.value = false
  }
}

const saveInfo = async () => {
  if (!userInfo.value) return
  
  try {
    const { code, msg } = await service.post('/user/info/modify', {
      id: userInfo.value.id,
      nickname: editNickname.value,
      email: editEmail.value
    })
    
    if (code === 1000) {
      showToast('保存成功', 'success')
      userInfo.value.nickname = editNickname.value
      userInfo.value.email = editEmail.value || null
    } else {
      showToast(msg || '保存失败', 'error')
    }
  } catch (error) {
    showToast('保存失败', 'error')
  }
}

watch(() => route.params.userId, () => {
  loadUserInfo()
})

onMounted(() => {
  loadUserInfo()
})
</script>

<template>
  <div class="profile-container">
    <div v-if="toastMessage" class="toast" :class="toastType">{{ toastMessage }}</div>
    
    <div v-if="loading" class="loading">加载中...</div>
    
    <template v-else-if="userInfo">
      <div class="user-top">
        <div class="avatar-large">?</div>
        <div class="phone">{{ userInfo.phone }}</div>
      </div>
      <div class="info-card">
        <div class="info-row">
          <span class="label">昵称</span>
          <input 
            v-model="editNickname" 
            class="edit-input" 
            type="text"
            placeholder="请输入昵称"
          />
        </div>
        <div class="info-row">
          <span class="label">邮箱</span>
          <input 
            v-model="editEmail" 
            class="edit-input" 
            type="email"
            placeholder="请输入邮箱"
          />
        </div>
        <div class="save-btn-row">
          <button class="save-btn" @click="saveInfo">保存</button>
        </div>
      </div>
    </template>
    
    <div v-else class="empty">加载失败，请重试</div>
  </div>
</template>

<style scoped>
.profile-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 200px;
  background: #fafafa;
}

.profile-content {
  text-align: center;
}

.user-top {
  text-align: center;
  margin-bottom: 30px;
}

.avatar-large {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2.5rem;
  font-weight: 600;
  margin: 0 auto 16px;
}

.phone {
  font-size: 1.1rem;
  font-weight: 600;
  color: #333;
}

.info-card {
  width: 60%;
  margin: 0 auto;
  background: white;
  border-radius: 12px;
  padding: 16px 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 14px 20px;
}

.info-row:not(:last-child) {
  border-bottom: 1px solid #f0f0f0;
}

.label {
  font-size: 0.95rem;
  color: #666;
}

.value {
  font-size: 0.95rem;
  color: #333;
  font-weight: 500;
}

.edit-input {
  flex: 1;
  max-width: 200px;
  padding: 6px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  font-size: 0.95rem;
  outline: none;
  text-align: right;
}

.edit-input:focus {
  border-color: #22c55e;
}

.save-btn-row {
  padding: 14px 20px 0;
  text-align: center;
}

.save-btn {
  padding: 8px 32px;
  background: #22c55e;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 0.95rem;
  cursor: pointer;
  transition: background 0.2s;
}

.save-btn:hover {
  background: #16a34a;
}

.loading,
.empty {
  color: #999;
  font-size: 1rem;
}

.toast {
  position: fixed;
  top: 120px;
  left: 50%;
  transform: translateX(-50%);
  padding: 12px 24px;
  border-radius: 8px;
  color: white;
  font-weight: 500;
  z-index: 1000;
  background: #666;
}

.toast.success {
  background: #22c55e;
}

.toast.error {
  background: #ef4444;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateX(-50%) translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateX(-50%) translateY(0);
  }
}
</style>