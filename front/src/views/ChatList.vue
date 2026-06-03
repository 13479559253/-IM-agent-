<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { service } from '../utils/axios'
import { jwtDecode } from 'jwt-decode'
import { getWs, closeWs } from '../utils/ws'  

interface Conversation {
  conversationId: number
  unreadCount: number
  lastMessage: string
  lastTime: string
  nickname: string
  otherUserId?: number
}

interface UserInfo {
  id: number
  nickname: string
  phone: string
  email: string | null
}
const router = useRouter()
const route = useRoute()

const conversations = ref<Conversation[]>([])
const loading = ref(false)
const userInfo = ref<UserInfo | null>(null)
const toastMessage = ref('')
const toastType = ref<'success' | 'error'>('error')
const showDropdown = ref(false)
const showAddFriendModal = ref(false)
const searchPhone = ref('')
const searchedUser = ref<UserInfo | null>(null)
const searchLoading = ref(false)
const searchPerformed = ref(false)
const searchError = ref('')
const hasNewFriendRequest = ref(false)
const activeTab = ref<'chats' | 'requests'>('chats')
const friendRequests = ref<any[]>([])
const requestsLoading = ref(false)

const showToast = (message: string, type: 'success' | 'error' = 'error') => {
  toastMessage.value = message
  toastType.value = type
  setTimeout(() => {
    toastMessage.value = ''
  }, 1000)
}

const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value
}

const logout = () => {
  closeWs()
  localStorage.removeItem('token')
  showDropdown.value = false
  router.replace('/')
}

const goToProfile = () => {
  showDropdown.value = false
  router.replace('/chats/profile')
}

const selectedConversation = computed(() => {
  return Number(route.params.conversationId) || null
})

const loadConversations = async () => {
  loading.value = true
  try {
    const {code,data,msg}= await service.get<Conversation[]>('/conversation/list')
    if (code !== 1000) {
      showToast(msg || '加载会话列表失败')
      return
    }
      conversations.value = data  
    } catch (error) {
      showToast('加载会话列表失败')
    return
  } finally {
    loading.value = false
  }
}

const loadUserInfo = async () => {
  const token = localStorage.getItem('token')
  if(!token) return
  const decode = jwtDecode(token)
  const userId = (decode as { id: number }).id
  const {code,data,msg} = await service.get("/user/info",{params:{userId}})
  if (code !== 1000) {
    showToast(msg || '加载用户信息失败')  
    return
  }
  userInfo.value = data
}

const selectConversation = (conversationId: number) => {
  router.replace(`/chats/${conversationId}`)
}

const goToAgent = () => {
  router.replace('/chats/agent')
}

const openAddFriendModal = () => {
  showAddFriendModal.value = true
  searchPhone.value = ''
  searchedUser.value = null
}

const closeAddFriendModal = () => {
  showAddFriendModal.value = false
  searchPhone.value = ''
  searchedUser.value = null
  searchPerformed.value = false
  searchError.value = ''
}

const switchToChats = () => {
  activeTab.value = 'chats'
  loadConversations()
}

const handleFriendRequestClick = () => {
  hasNewFriendRequest.value = false
  activeTab.value = 'requests'
  loadFriendRequests()
}

const loadFriendRequests = async () => {
  requestsLoading.value = true
  try {
    const { code, data } = await service.get('/conversation/apply/list')
    if (code === 1000) {
      friendRequests.value = data || []
    }
  } catch (error) {
    friendRequests.value = []
  } finally {
    requestsLoading.value = false
  }
}

const handleFriendRequest = async (friendRequestId: number, senderId: number) => {
  try {
    const receiverId = userInfo.value?.id
    
    const { code, msg } = await service.post('/conversation/apply/agree', {
      friendRequestId: friendRequestId,
      senderId: senderId,
      receiverId: receiverId
    })
    if (code === 1000) {
      showToast('已同意好友申请', 'success')
      friendRequests.value = friendRequests.value.filter(r => r.friendRequestId !== friendRequestId)
    } else {
      showToast(msg || '操作失败')
    }
  } catch (error) {
    showToast('操作失败')
  }
}

const searchUserByPhone = async () => {
  if (!searchPhone.value.trim()) {
    return
  }
  
  searchPerformed.value = true
  searchLoading.value = true
  searchError.value = ''
  try {
    const { code, data, msg } = await service.get('/user/info', {
      params: { phone: searchPhone.value.trim() }
    })
    if (code !== 1000) {
      searchError.value = msg || '未找到该用户'
      searchedUser.value = null
      return
    }
    searchedUser.value = data
    searchError.value = ''
  } catch (error) {
    searchError.value = '未找到该用户'
    searchedUser.value = null
  } finally {
    searchLoading.value = false
  }
}

const addFriend = async () => {
  if (!searchedUser.value) return
  
  try {
    const { code, msg } = await service.post('/conversation/apply', {
      senderId: userInfo.value?.id || 0,
      receiverId: searchedUser.value.id || 0  
    })
    if (code !== 1000) {
      closeAddFriendModal()
      showToast(msg || '添加好友失败')
      return
    }
    closeAddFriendModal()
    showToast('添加好友请求已发送', 'success')
  } catch (error) {
    closeAddFriendModal()
    showToast('添加好友失败')
  }
}

onMounted(() => {
  loadUserInfo()
  loadConversations()
  const ws = getWs()
  if (!ws) return
  ws.onerror = () => {
    closeWs()
  }
  ws.addEventListener('message', (event) => { 
    const data = JSON.parse(event.data)
    if (data.type === 'clearUnread') {
      if (selectedConversation.value) {
        const c = conversations.value.find(conv => conv.conversationId === selectedConversation.value)
        if (c) {
          c.unreadCount = 0
        }
      }
    }
    if (data.type === 'sendMessage') {
      console.log(data)
      if (selectedConversation.value === data.data.conversationId) {
        const c = conversations.value.find(conv => conv.conversationId === selectedConversation.value)
        if (c) {
          c.lastMessage = data.data.content
          c.lastTime = new Date().toLocaleString()
        }
      }
    }
    if(data.type === 'receiveMessage'){
      const c = conversations.value.find(conv => conv.conversationId === data.data.conversationId)
      if (c) {
        c.lastMessage = data.data.content
        c.lastTime = new Date().toLocaleString()
        if(data.data.conversationId !== selectedConversation.value) c.unreadCount++
      }
    }
    if(data.type === 'sendApply'){
      hasNewFriendRequest.value = true
    }
    if(data.type === 'receiveApply'){
      console.log("收到同意申请")
      loadConversations()
    }
    if(data.type === 'modifyInfo'){
      loadUserInfo()
    }
    if(data.type === 'friendDelete'){
      console.log("收到删除好友申请")
      loadConversations()
    }
  })
})

const formatTime = (time: string) => {
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const hours = Math.floor(diff / (1000 * 60 * 60))
  
  if (hours < 1) {
    const minutes = Math.floor(diff / (1000 * 60))
    return minutes < 1 ? '刚刚' : `${minutes}分钟前`
  } else if (hours < 24) {
    return `${hours}小时前`
  } else if (hours < 48) {
    return '昨天'
  } else {
    return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
  }
}
</script>

<template>
  <div class="chat-page">
    <nav class="navbar">
      <div class="logo">
        <div class="logo-icon">
          <span class="logo-dot"></span>
          <span class="logo-wave"></span>
        </div>
      </div>
      <span class="nav-link">聊天列表</span>
      <div class="user-info" v-if="userInfo">
        <div class="user-avatar">?</div>
        <span class="user-nickname" @click="toggleDropdown">{{ userInfo.nickname }}</span>
        <div v-if="showDropdown" class="dropdown-menu">
          <button class="dropdown-item" @click="goToProfile">个人信息</button>
          <button class="dropdown-item" @click="logout">退出登录</button>
        </div>
      </div>
    </nav>
    <div v-if="toastMessage" :class="['toast', toastType]">
      {{ toastMessage }}
    </div>
    <div class="chat-container">
    <div class="conversation-panel">
      <div class="panel-header">
        <div class="tabs">
          <button 
            class="tab-btn" 
            :class="{ active: activeTab === 'chats' }"
            @click="switchToChats"
          >
            消息
          </button>
          <button 
            class="tab-btn" 
            :class="{ active: activeTab === 'requests', 'has-request': hasNewFriendRequest }"
            @click="handleFriendRequestClick"
          >
            好友申请
          </button>
        </div>
        <button class="add-friend-btn-header" @click="openAddFriendModal">添加好友</button>
      </div>
      
      <div v-if="activeTab === 'chats'" class="conversation-list">
        <div 
          class="conversation-item agent-item"
          :class="{ active: route.path === '/chats/agent' }"
          @click="goToAgent"
        >
          <div class="avatar agent-avatar">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="8"></circle>
              <circle cx="8" cy="10" r="1.5"></circle>
              <circle cx="16" cy="10" r="1.5"></circle>
              <path d="M8 16 Q12 20 16 16"></path>
              <circle cx="7" cy="5" r="1"></circle>
              <circle cx="17" cy="5" r="1"></circle>
            </svg>
          </div>
          <div class="conversation-info">
            <div class="conversation-header">
              <span class="nickname">智能助手</span>
              <span class="time">在线</span>
            </div>
            <div class="last-message">有什么可以帮您的？</div>
          </div>
          <div class="online-indicator"></div>
        </div>
        
        <div v-if="loading" class="loading">
          加载中...
        </div>
        <div v-else-if="conversations.length === 0" class="empty">
          暂无会话
        </div>
        <div
          v-for="conv in conversations"
          :key="conv.conversationId"
          class="conversation-item"
          :class="{ active: selectedConversation === conv.conversationId }"
          @click="selectConversation(conv.conversationId)"
        >
          <div class="avatar">?</div>
          <div class="conversation-info">
            <div class="conversation-header">
              <span class="nickname">{{ conv.nickname }}</span>
              <span class="time">{{ formatTime(conv.lastTime) }}</span>
            </div>
            <div class="last-message">{{ conv.lastMessage }}</div>
          </div>
          <div v-if="conv.unreadCount > 0" class="unread-count">
            {{ conv.unreadCount }}
          </div>
        </div>
      </div>
      
      <div v-else class="requests-list">
        <div v-if="requestsLoading" class="loading">
          加载中...
        </div>
        <div v-else-if="friendRequests.length === 0" class="empty">
          暂无好友申请
        </div>
        <div v-else>
          <div v-for="request in friendRequests" :key="request.friendRequestId" class="request-item">
            <div class="avatar">?</div>
            <div class="request-info">
              <div class="request-name">{{ request.nickname }}</div>
              <div class="request-phone">{{ request.phone }}</div>
              <div v-if="request.email" class="request-email">{{ request.email }}</div>
              <div class="request-time">{{ request.createTime }}</div>
            </div>
            <div class="request-actions">
              <button class="accept-btn" @click="handleFriendRequest(request.friendRequestId, request.senderId)">同意</button>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="chat-panel">
      <router-view v-slot="{ Component }">
        <div v-if="Component" class="chat-content">
          <component :is="Component" />
        </div>
      </router-view>
    </div>
    </div>
  </div>
  
  <!-- 添加好友浮窗 -->
  <div v-if="showAddFriendModal" class="modal-overlay" @click.self="closeAddFriendModal">
    <div class="modal-content">
      <div class="modal-header">
        <h3>添加好友</h3>
        <button class="close-btn" @click="closeAddFriendModal">×</button>
      </div>
      <div class="modal-body">
        <div class="search-section">
          <input
            v-model="searchPhone"
            type="tel"
            class="search-input"
            placeholder="请输入电话号码"
            @keyup.enter="searchUserByPhone"
          />
          <button class="search-btn" @click="searchUserByPhone" :disabled="searchLoading">
            <span v-if="searchLoading">搜索中...</span>
            <span v-else>搜索</span>
          </button>
        </div>
        
        <div v-if="searchedUser" class="user-card">
          <div class="user-avatar-large">?</div>
          <div class="user-details">
            <div class="user-name">{{ searchedUser.nickname }}</div>
            <div class="user-phone">{{ searchedUser.phone }}</div>
            <div v-if="searchedUser.email" class="user-email">{{ searchedUser.email }}</div>
          </div>
          <button class="add-friend-btn" @click="addFriend">添加好友</button>
        </div>
        
        <div v-else-if="searchPerformed && !searchLoading" class="no-result">
          {{ searchError || '未找到该用户' }}
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.chat-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.navbar {
  height: 10%;
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 16px 24px;
  background: #f5f5f5;
  border-bottom: 1px solid #e0e0e0;
}

.logo {
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

.nav-link {
  color: #333;
  font-weight: 500;
  padding: 8px 16px;
}



.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
  margin-right: 5%;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 0.85rem;
}

.user-nickname {
  cursor: pointer;
  color: #333;
  font-weight: 500;
}

.dropdown-menu {
  position: absolute;
  top: 80px;
  right: 1%;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  padding: 8px 0;
  min-width: 120px;
  z-index: 100;
}

.dropdown-item {
  width: 100%;
  padding: 10px 20px;
  text-align: left;
  border: none;
  background: none;
  cursor: pointer;
  font-size: 0.9rem;
  color: #333;
  transition: background 0.2s;
}

.dropdown-item:hover {
  background: #f5f5f5;
}

.toast {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  padding: 12px 24px;
  border-radius: 8px;
  color: white;
  font-weight: 500;
  z-index: 1000;
  background: #ef4444;
  animation: fadeIn 0.3s ease;
}

.toast.success {
  background: #22c55e;
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

.chat-container {
  height: 90%;
  display: flex;
  background: #f5f5f5;
}

.conversation-panel {
  width: 30%;
  background: #fff;
  border-right: 1px solid #e5e5e5;
  display: flex;
  flex-direction: column;
}

.panel-header {
  padding: 16px;
  border-bottom: 1px solid #e5e5e5;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tabs {
  display: flex;
  gap: 4px;
  background: #f5f5f5;
  padding: 4px;
  border-radius: 8px;
}

.tab-btn {
  background: none;
  border: none;
  padding: 8px 16px;
  font-size: 0.9rem;
  font-weight: 500;
  color: #666;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.2s;
}

.tab-btn.active {
  background: white;
  color: #22c55e;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.tab-btn.has-request {
  color: #ef4444;
}

.add-friend-btn-header {
  font-size: 0.85rem;
  padding: 6px 12px;
  border: 1px solid #22c55e;
  border-radius: 4px;
  background: white;
  color: #22c55e;
  cursor: pointer;
  transition: all 0.2s;
}

.add-friend-btn-header:hover {
  background: #22c55e;
  color: white;
}

.conversation-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.loading,
.empty {
  text-align: center;
  padding: 40px 20px;
  color: #999;
  font-size: 0.9rem;
}

.conversation-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  transition: background 0.2s;
  margin-bottom: 4px;
}

.conversation-item:hover {
  background: #f5f5f5;
}

.conversation-item.active {
  background: #e8f4ff;
}

.avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 1rem;
  flex-shrink: 0;
}

.conversation-info {
  flex: 1;
  min-width: 0;
}

.conversation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.nickname {
  font-weight: 600;
  color: #333;
  font-size: 0.95rem;
}

.time {
  font-size: 0.8rem;
  color: #999;
  flex-shrink: 0;
}

.last-message {
  font-size: 0.85rem;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.unread-count {
  background: #ef4444;
  color: white;
  font-size: 0.75rem;
  padding: 2px 8px;
  border-radius: 10px;
  min-width: 20px;
  text-align: center;
  font-weight: 500;
  flex-shrink: 0;
}

.agent-item {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.08) 0%, rgba(118, 75, 162, 0.08) 100%);
  border: 1px solid rgba(102, 126, 234, 0.2);
}

.agent-item:hover {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.12) 0%, rgba(118, 75, 162, 0.12) 100%);
}

.agent-item.active {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.15) 0%, rgba(118, 75, 162, 0.15) 100%);
}

.agent-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.agent-avatar svg {
  width: 18px;
  height: 18px;
}

.online-indicator {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #22c55e;
  box-shadow: 0 0 4px #22c55e;
  animation: pulse-green 2s ease-in-out infinite;
}

@keyframes pulse-green {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.7;
    transform: scale(1.1);
  }
}

.chat-panel {
  width: 70%;
  background: #fafafa;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chat-content {
  width: 100%;
  height: 100%;
}

/* 添加好友浮窗样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeInOverlay 0.2s ease;
}

.modal-content {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 450px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  animation: slideUp 0.3s ease;
}

@keyframes fadeInOverlay {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-header {
  padding: 16px 20px;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.1rem;
  color: #333;
}

.close-btn {
  width: 28px;
  height: 28px;
  border: none;
  background: #f5f5f5;
  border-radius: 50%;
  font-size: 1.2rem;
  color: #666;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.close-btn:hover {
  background: #e0e0e0;
  color: #333;
}

.modal-body {
  padding: 20px;
}

.search-section {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.search-input {
  flex: 1;
  padding: 10px 14px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 0.95rem;
  outline: none;
  transition: border-color 0.2s;
}

.search-input:focus {
  border-color: #667eea;
}

.search-input::placeholder {
  color: #999;
}

.search-btn {
  padding: 10px 24px;
  background: #5BB5E0;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 0.95rem;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s;
}

.search-btn:hover:not(:disabled) {
  background: #3A9BCF;
}

.search-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.user-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  background: #fafafa;
  border-radius: 12px;
  gap: 16px;
}

.user-avatar-large {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 1.5rem;
}

.user-details {
  text-align: center;
  gap: 4px;
}

.user-name {
  font-size: 1.2rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.user-phone,
.user-email {
  font-size: 0.9rem;
  color: #666;
  margin-bottom: 4px;
}

.add-friend-btn {
  padding: 12px 32px;
  background: linear-gradient(135deg, #3b82f6 0%, #22c55e 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.2s;
}

.add-friend-btn:hover {
  opacity: 0.9;
}

.no-result {
  text-align: center;
  color: #999;
  padding: 20px;
}

.requests-list {
  padding: 8px;
}

.request-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.request-item:last-child {
  border-bottom: none;
}

.request-info {
  flex: 1;
}

.request-name {
  font-size: 0.95rem;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.request-phone,
.request-email {
  font-size: 0.85rem;
  color: #666;
  margin-bottom: 2px;
}

.request-time {
  font-size: 0.8rem;
  color: #999;
}

.request-actions {
  display: flex;
  gap: 6px;
}

.accept-btn {
  padding: 6px 12px;
  background: #22c55e;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 0.85rem;
  cursor: pointer;
  transition: opacity 0.2s;
}

.accept-btn:hover {
  opacity: 0.9;
}

</style>
