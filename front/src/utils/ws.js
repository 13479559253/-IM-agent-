let globalWs = null

export function initWs(token) {
  globalWs = new WebSocket('ws://localhost:8080/chat?token=' + token)
  globalWs.onopen = () => {
    console.log('WebSocket 连接成功')
  }
}

export function getWs() {
  return globalWs
}

export function closeWs() {
  globalWs?.close()
  globalWs = null  
}