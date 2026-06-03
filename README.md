# -IM-agent-
# IM项目后端介绍
-
# IM项目后端介绍

## 技术栈
- Vue 3 + TypeScript
- Vite
- Vue Router
- Element Plus
- Axios
- jwt-decode

## 项目结构
```
src/
├── main.ts          # 入口
├── App.vue          # 根组件
├── style.css        # 全局样式
├── router/
│   └── index.ts     # 路由配置
├── utils/
│   ├── axios.js     # HTTP封装
│   └── ws.js        # WebSocket封装
└── views/
    ├── Login.vue    # 登录页
    ├── ChatList.vue # 会话列表
    ├── ChatRoom.vue # 聊天房间
    ├── AgentChat.vue# 智能助手
    └── Profile.vue  # 用户资料
```

## 核心功能
1. **用户认证**: 密码/验证码登录注册、JWT管理
2. **即时通讯**: 会话列表、WebSocket实时消息
3. **智能助手**: 机器人对话，调用 `http://localhost:8000/chat`
4. **用户管理**: 资料编辑、好友请求、删除好友

## 关键实现
- **HTTP封装**: 自动添加Token，处理过期重定向
- **WebSocket**: 实时消息推送
- **路由**: 嵌套路由结构
