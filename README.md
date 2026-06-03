# IM项目后端介绍

## 技术栈

- Spring Boot
- MyBatis
- MySQL
- Redis
- WebSocket
- JWT
- Lombok
- Spring Security Crypto


## 项目结构

```
src/main/java/com/example/demo/
├── configuration/       # 配置类
│   ├── RedisConfig.java
│   ├── WebConfig.java
│   └── WebSocketConfig.java
├── constant/            # 常量定义
│   ├── FriendRequestConstant.java
│   └── WebSocketConstant.java
├── controller/          # 控制层
│   ├── ConversationController.java
│   └── UserController.java
├── exception/           # 异常处理
│   ├── BaseException.java
│   ├── GlobalExceptionHandler.java
│   ├── LoginException.java
│   ├── MessageException.java
│   └── RegexException.java
├── interceptor/         # 拦截器
│   └── AuthInterceptor.java
├── mapper/              # 数据访问层
│   ├── ConversationMapper.java
│   ├── ConversationUserMapper.java
│   ├── FriendRequestMapper.java
│   ├── MessageMapper.java
│   └── UserMapper.java
├── pojo/                # 实体类和DTO
│   ├── DTO/             # 数据传输对象
│   ├── Conversation.java
│   ├── FriendRequest.java
│   ├── Message.java
│   ├── Result.java
│   └── User.java
├── service/             # 业务逻辑层
│   ├── impl/            # 实现类
│   ├── ConversationService.java
│   └── UserService.java
├── utils/               # 工具类
│   ├── JwtUtil.java
│   ├── RedisUtil.java
│   └── RegexUtil.java
├── websocket/           # WebSocket相关
│   ├── ChatWebsocket.java
│   └── SocketMessage.java
└── DemoApplication.java # 启动类
```

## 功能模块

### 1. 用户管理模块

| API | 方法 | 功能 |
|-----|------|------|
| `/user/register` | POST | 用户注册 |
| `/user/code` | GET | 发送验证码 |
| `/user/login/password` | POST | 密码登录 |
| `/user/login/code` | POST | 验证码登录 |
| `/user/info` | GET | 获取用户信息 |
| `/user/info/modify` | POST | 修改用户信息 |

### 2. 会话管理模块

| API | 方法 | 功能 |
|-----|------|------|
| `/conversation/apply` | POST | 发送好友申请 |
| `/conversation/apply/list` | GET | 获取好友申请列表 |
| `/conversation/apply/agree` | POST | 同意好友申请 |
| `/conversation/list` | GET | 获取会话列表 |
| `/conversation/message/list` | GET | 获取消息列表 |
| `/conversation/send/message` | POST | 发送消息 |
| `/conversation/delete` | DELETE | 删除会话 |

### 3. WebSocket实时通信

WebSocket 服务支持以下实时消息类型：

| 消息类型 | 说明 |
|----------|------|
| `WEBSOCKET_RECEIVE_MESSAGE` | 接收消息 |
| `WEBSOCKET_SEND_MESSAGE` | 发送消息 |
| `WEBSOCKET_SEND_APPLY` | 发送好友申请通知 |
| `WEBSOCKET_RECEIVE_APPLY` | 同意好友申请通知 |
| `WEBSOCKET_CLEAR_UNREAD` | 清除未读数 |
| `WEBSOCKET_INFO_MODIFY` | 个人信息更新通知 |
| `WEBSOCKET_FRIEND_DELETE` | 删除好友通知 |

## 核心技术特性

1. **JWT认证**: 使用JJWT实现无状态身份认证，通过拦截器验证token
2. **密码加密**: 使用Spring Security Crypto进行密码加密存储
3. **请求参数校验**: 使用RegexUtil进行参数格式校验
4. **全局异常处理**: 通过GlobalExceptionHandler统一处理异常
5. **实时消息推送**: 通过WebSocket实现消息实时推送
6. **Redis缓存**: 用于存储验证码等临时数据


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
