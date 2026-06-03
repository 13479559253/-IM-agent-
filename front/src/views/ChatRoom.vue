<script setup lang="ts">
import { ref, onMounted, computed, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { service } from '../utils/axios'
import { jwtDecode } from 'jwt-decode'
import { getWs, closeWs } from '../utils/ws'  


interface Message {
  content: string
  senderId: number
  sendTime: string
}

const route = useRoute()
const router = useRouter()
const messages = ref<Message[]>([])
const loading = ref(false)
const currentUserId = ref<number | null>(null)
const toastMessage = ref('')
const inputMessage = ref('')
const messagesContainer = ref<HTMLElement | null>(null)
const showDropdown = ref(false)
const showConfirmDialog = ref(false)

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

const handleSend = async () => {
  if (!inputMessage.value.trim()) return
  const {code,msg} = await service.post('/conversation/send/message', {
    conversationId: conversationId.value,
    content: inputMessage.value.trim(),
    senderId: currentUserId.value
  })
  if (code !== 1000) {
    showToast(msg || '发送消息失败')
    inputMessage.value = ''
    return
  }
  messages.value.push({
    content: inputMessage.value.trim(),
    senderId: currentUserId.value,
    sendTime: new Date().toISOString()
  })
  inputMessage.value = ''
  scrollToBottom()
}

const showToast = (message: string) => {
  toastMessage.value = message
  setTimeout(() => {
    toastMessage.value = ''
  }, 2000)
}

const conversationId = computed(() => {
  return Number(route.params.conversationId)
})

const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value
}

const handleDeleteFriend = () => {
  showDropdown.value = false
  showConfirmDialog.value = true
}

const confirmDelete = async () => {
  const {code,msg} = await service.delete('/conversation/delete', {
    params: { conversationId: conversationId.value }
  })
  if (code !== 1000) {
    showToast(msg || '删除好友失败')
    return
  }
  showToast('删除好友成功')
  setTimeout(() => {
    router.push({ name: 'ChatList' })
  }, 1500)  
  showConfirmDialog.value = false
}

const handleEditRemark = () => {
  showDropdown.value = false
}

watch(() => route.params.conversationId, (newId) => {
  if (newId) {
    loadMessages()
  }
})

const loadMessages = async () => {
  if (!conversationId.value) return
  
  loading.value = true
  try {
    const {data,code,msg} = await service.get('/conversation/message/list', {
      params: { conversationId: conversationId.value }
    })
    if (code !== 1000) {
      showToast(msg || '加载消息失败')
    }
    messages.value = data || []
  } catch (error) {
    console.error('加载消息失败:', error)
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

const isCurrentUser = (senderId: number): boolean => {
  return senderId === currentUserId.value
}

const formatTime = (timeStr: string): string => {
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  // 小于 1 分钟
  if (diff < 60000) {
    return '刚刚'
  }
  // 小于 1 小时
  if (diff < 3600000) {
    return `${Math.floor(diff / 60000)}分钟前`
  }
  // 小于 24 小时
  if (diff < 86400000) {
    return `${Math.floor(diff / 3600000)}小时前`
  }
  // 显示日期
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

onMounted(() => {
  const token = localStorage.getItem('token')
  if (token) {
    const decode = jwtDecode(token)
    currentUserId.value = (decode as { id: number }).id
  }
  loadMessages()
  const ws = getWs()
  if (!ws) return
  ws.onerror = (error) => {
    ws.close()
    console.log('连接失败', error)
  }
  ws.addEventListener('message', (event) => {
    const data = JSON.parse(event.data)
    if(data.type === 'receiveMessage'){
      if(data.data.conversationId === conversationId.value){
        messages.value.push({
          content: data.data.content,
          senderId: data.data.senderId,
          sendTime: new Date().toISOString()
        })
        scrollToBottom()
      }
    }
  })
})
</script>

<template>
  <div class="chat-room-container">
    <div v-if="toastMessage" class="toast">
      {{ toastMessage }}
    </div>

    <div v-if="showConfirmDialog" class="confirm-overlay">
      <div class="confirm-dialog">
        <div class="confirm-content">确定要删除该好友吗？</div>
        <div class="confirm-actions">
          <button class="confirm-btn cancel" @click="showConfirmDialog = false">取消</button>
          <button class="confirm-btn danger" @click="confirmDelete">确定</button>
        </div>
      </div>
    </div>
    
    <div class="chat-header">
      <div class="dropdown-wrapper">
        <button 
          class="dropdown-btn" 
          @click="toggleDropdown"
        >
          ...
        </button>
        <div v-if="showDropdown" class="dropdown-menu">
          <button class="dropdown-item" @click="handleEditRemark">更改备注</button>
          <button class="dropdown-item danger" @click="handleDeleteFriend">删除好友</button>
        </div>
      </div>
    </div>
    
    <div ref="messagesContainer" class="messages-container">
      <div v-if="loading" class="loading">
        加载中...
      </div>
      <div v-else-if="messages.length === 0" class="empty">
        暂无消息
      </div>
      <div
        v-for="(msg, index) in messages"
        :key="index"
        class="message-item"
        :class="{ 'message-mine': isCurrentUser(msg.senderId) }"
      >
        <div class="avatar">?</div>
        <div class="message-bubble">
          <div class="message-content">{{ msg.content }}</div>
          <div class="message-time">{{ formatTime(msg.sendTime) }}</div>
        </div>
      </div>
    </div>
    <div class="input-area">
      <textarea
        v-model="inputMessage"
        class="message-input"
        placeholder="输入消息..."
        @keyup.enter.exact="handleSend"
        maxlength="200"
      ></textarea>
      <button class="send-btn" @click="handleSend" @keyup.enter="handleSend">发送</button>
    </div>
  </div>
</template>

<style scoped>
.chat-room-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fafafa;
}

.chat-header {
  padding: 12px 20px;
  background: white;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

.dropdown-wrapper {
  position: relative;
}

.dropdown-btn {
  padding: 6px 14px;
  background: transparent;
  color: #666;
  border: none;
  border-radius: 0;
  font-size: 1.5rem;
  cursor: pointer;
  transition: all 0.2s;
  text-decoration: none;
}

.dropdown-btn:hover {
  color: #333;
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
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

.dropdown-item.danger {
  color: #ef4444;
}

.dropdown-item.danger:hover {
  background: #fef2f2;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.loading,
.empty {
  text-align: center;
  color: #999;
  padding: 40px;
}

.message-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 16px;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 0.9rem;
  flex-shrink: 0;
}

.message-item.message-mine {
  flex-direction: row-reverse;
}

.message-bubble {
  max-width: 60%;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.message-item:not(.message-mine) .message-bubble {
  align-items: flex-start;
}

.message-item.message-mine .message-bubble {
  align-items: flex-end;
}

.message-content {
  padding: 12px 16px;
  border-radius: 12px;
  background: white;
  color: #333;
  font-size: 0.95rem;
  line-height: 1.5;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.message-item.message-mine .message-content {
  background: #e8f4ff;
  color: #333;
}

.message-time {
  font-size: 0.75rem;
  color: #999;
  padding: 0 4px;
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
  background: #ef4444;
  animation: fadeIn 0.3s ease;
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

.input-area {
  padding: 12px 20px;
  background: white;
  border-top: 1px solid #e0e0e0;
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.message-input {
  flex: 1;
  padding: 8px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 20px;
  resize: none;
  font-size: 0.95rem;
  line-height: 1.6;
  height: 40px;
  outline: none;
  transition: border-color 0.2s;
  overflow: hidden;
}

.message-input:focus {
  border-color: #667eea;
}

.message-input::placeholder {
  color: #999;
}

.send-btn {
  padding: 12px 24px;
  background: #4facfe;
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 0.95rem;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s;
  min-width: 80px;
}

.send-btn:hover {
  opacity: 0.9;
}

.send-btn:active {
  opacity: 0.8;
}

.confirm-overlay {
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
}

.confirm-dialog {
  background: white;
  border-radius: 12px;
  padding: 24px;
  min-width: 280px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
}

.confirm-content {
  font-size: 1rem;
  color: #333;
  text-align: center;
  margin-bottom: 20px;
}

.confirm-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.confirm-btn {
  padding: 10px 24px;
  border-radius: 8px;
  font-size: 0.9rem;
  cursor: pointer;
  border: none;
  transition: opacity 0.2s;
}

.confirm-btn.cancel {
  background: #f5f5f5;
  color: #666;
}

.confirm-btn.danger {
  background: #ef4444;
  color: white;
}

.confirm-btn:hover {
  opacity: 0.9;
}
</style>