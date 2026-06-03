<script setup lang="ts">
import { ref, nextTick } from 'vue'
import axios from 'axios'

interface Message {
  content: string
  senderId: number
  sendTime: string
}

const messages = ref<Message[]>([])
const inputMessage = ref('')
const messagesContainer = ref<HTMLElement | null>(null)
const loading = ref(false)
const toastMessage = ref('')

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

const showToast = (message: string) => {
  toastMessage.value = message
  setTimeout(() => {
    toastMessage.value = ''
  }, 2000)
}

const handleSend = async () => {
  if (!inputMessage.value.trim()) return
  
  const userInput = inputMessage.value.trim()
  messages.value.push({
    content: userInput,
    senderId: 1,
    sendTime: new Date().toISOString()
  })
  inputMessage.value = ''
  scrollToBottom()
  
  loading.value = true
  
  try {
    const token = localStorage.getItem('token')
    const response = await axios.post('http://localhost:8000/chat', {
      content: userInput
    }, {
      headers: {
        'token': token || ''
      }
    })
    
    messages.value.push({
      content: response.data,
      senderId: 0,
      sendTime: new Date().toISOString()
    })
  } catch (error) {
    showToast('请求失败，请稍后重试')
    console.error('Error:', error)
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

const formatTime = (timeStr: string): string => {
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 60000) {
    return '刚刚'
  }
  if (diff < 3600000) {
    return `${Math.floor(diff / 60000)}分钟前`
  }
  if (diff < 86400000) {
    return `${Math.floor(diff / 3600000)}小时前`
  }
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

const isCurrentUser = (senderId: number): boolean => {
  return senderId === 1
}
</script>

<template>
  <div class="agent-chat-container">
    <div v-if="toastMessage" class="toast">
      {{ toastMessage }}
    </div>
    
    <div class="chat-header">
      <div class="bot-info">
        <div class="bot-avatar">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="8"></circle>
            <circle cx="8" cy="10" r="1.5"></circle>
            <circle cx="16" cy="10" r="1.5"></circle>
            <path d="M8 16 Q12 20 16 16"></path>
            <circle cx="7" cy="5" r="1"></circle>
            <circle cx="17" cy="5" r="1"></circle>
          </svg>
        </div>
        <span class="bot-name">智能助手</span>
      </div>
    </div>
    
    <div ref="messagesContainer" class="messages-container">
      <div
        v-for="(msg, index) in messages"
        :key="index"
        class="message-item"
        :class="{ 'message-mine': isCurrentUser(msg.senderId) }"
      >
        <div class="avatar" :class="{ 'bot-avatar-small': !isCurrentUser(msg.senderId) }">
          <svg v-if="!isCurrentUser(msg.senderId)" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="8"></circle>
            <circle cx="8" cy="10" r="1.5"></circle>
            <circle cx="16" cy="10" r="1.5"></circle>
            <path d="M8 16 Q12 20 16 16"></path>
            <circle cx="7" cy="5" r="1"></circle>
            <circle cx="17" cy="5" r="1"></circle>
          </svg>
          <span v-else>?</span>
        </div>
        <div class="message-bubble">
          <div class="message-content">{{ msg.content }}</div>
          <div class="message-time">{{ formatTime(msg.sendTime) }}</div>
        </div>
      </div>
      
      <div v-if="loading" class="typing-indicator">
        <div class="typing-dots">
          <span></span>
          <span></span>
          <span></span>
        </div>
      </div>
    </div>
    
    <div class="input-area">
      <textarea
        v-model="inputMessage"
        class="message-input"
        placeholder="输入消息..."
        @keyup.enter.exact="handleSend"
        maxlength="500"
      ></textarea>
      <button class="send-btn" @click="handleSend" :disabled="loading">
        <svg v-if="!loading" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M22 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
          <circle cx="12" cy="7" r="4"></circle>
        </svg>
        <span v-else>发送中...</span>
      </button>
    </div>
  </div>
</template>

<style scoped>
.agent-chat-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fafafa;
}

.chat-header {
  padding: 12px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
}

.bot-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.bot-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.bot-avatar svg {
  width: 22px;
  height: 22px;
}

.bot-name {
  color: white;
  font-weight: 600;
  font-size: 1rem;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
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

.avatar.bot-avatar-small {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.bot-avatar-small svg {
  width: 18px;
  height: 18px;
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.message-time {
  font-size: 0.75rem;
  color: #999;
  padding: 0 4px;
}

.message-item.message-mine .message-time {
  color: rgba(255, 255, 255, 0.7);
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
  padding: 10px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 24px;
  resize: none;
  font-size: 0.95rem;
  line-height: 1.6;
  height: 44px;
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
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 50%;
  font-size: 0.95rem;
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.send-btn:hover:not(:disabled) {
  opacity: 0.9;
}

.send-btn:active:not(:disabled) {
  opacity: 0.8;
}

.send-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.send-btn svg {
  width: 20px;
  height: 20px;
}

.typing-indicator {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  padding: 12px 16px;
}

.typing-dots {
  display: flex;
  gap: 6px;
}

.typing-dots span {
  width: 8px;
  height: 8px;
  background: #ccc;
  border-radius: 50%;
  animation: typingBounce 1.4s infinite ease-in-out;
}

.typing-dots span:nth-child(1) {
  animation-delay: 0s;
}

.typing-dots span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-dots span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typingBounce {
  0%, 80%, 100% {
    transform: scale(0.6);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}
</style>
