package com.example.demo.websocket;

import com.example.demo.constant.WebSocketConstant;
import com.example.demo.mapper.ConversationUserMapper;
import com.example.demo.pojo.DTO.FriendRequestDTO;
import com.example.demo.pojo.Message;
import com.example.demo.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebsocket extends TextWebSocketHandler {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private ConversationUserMapper conversationUserMapper;
    private static final Map<Integer, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        try {
            String query = Objects.requireNonNull(session.getUri()).getQuery();
            String token = query.split("=")[1];
            Claims claims = jwtUtil.ParseToken(token);
            Integer id = claims.get("id", Integer.class);
            session.getAttributes().put("id", id);
            sessions.put(id, session);
        } catch (Exception e) {
            session.close();
            if(session.getAttributes().get("id") != null) {
                sessions.values().remove(session);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Integer userId = (Integer) session.getAttributes().get("id");
        if (userId != null) {
            sessions.remove(userId);
        }
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

    }


    //发送消息，根据会话中的用户广播，不论单聊或群聊都适用
    public void sendMessage(Message message) {
        Integer conversationId = message.getConversationId();
        List<Integer> list = conversationUserMapper.getUsersId(conversationId);
        SocketMessage<Message> socketMessage = new SocketMessage<>(WebSocketConstant.WEBSOCKET_RECEIVE_MESSAGE,message);
        try {
            for(Integer id : list) {
                if(id.equals(message.getSenderId())) socketMessage.setType(WebSocketConstant.WEBSOCKET_SEND_MESSAGE);
                else socketMessage.setType(WebSocketConstant.WEBSOCKET_RECEIVE_MESSAGE);
                WebSocketSession session = sessions.get(id);
                if (session != null && session.isOpen()) {
                    String json = mapper.writeValueAsString(socketMessage);
                    session.sendMessage(new TextMessage(json));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    //发送websocket请求，在发送完好友申请后调用，让接收方得知收到了好友申请
    public void sendApplyMessage(FriendRequestDTO friendRequestDTO) {
        WebSocketSession session = sessions.get(friendRequestDTO.getReceiverId());
        try {
            if (session != null && session.isOpen()) {
                SocketMessage<FriendRequestDTO> msg = new SocketMessage<>(WebSocketConstant.WEBSOCKET_SEND_APPLY, friendRequestDTO);
                String json = mapper.writeValueAsString(msg);
                session.sendMessage(new TextMessage(json));
            }
        } catch (Exception e) {
            throw new RuntimeException("网络异常");
        }
    }

    //发送websocket请求，在同意好友申请后调用，提示发送请求方添加会话
    public void sendAgreeMessage(FriendRequestDTO friendRequestDTO) {
        WebSocketSession session = sessions.get(friendRequestDTO.getSenderId());
        try {
            if (session != null && session.isOpen()) {
                SocketMessage<FriendRequestDTO> msg = new SocketMessage<>(WebSocketConstant.WEBSOCKET_RECEIVE_APPLY, friendRequestDTO);
                String json = mapper.writeValueAsString(msg);
                session.sendMessage(new TextMessage(json));
            }
        } catch (Exception e) {
            throw new RuntimeException("网络异常");
        }
    }

    //单纯传递信号，空内容
    //此处type：
    //1.WebSocketConstant.WEBSOCKET_CLEAR_UNREAD：提示前端更新消息的未读数
    //2.WebSocketConstant.WEBSOCKET_INFO_MODIFY：提示前端个人信息更新，导航栏修改
    public void sendEmptyMessage(Integer userId,String type){
        WebSocketSession session = sessions.get(userId);
        try {
            if (session != null && session.isOpen()) {
                SocketMessage<?> msg = new SocketMessage<>(type, null);
                String json = mapper.writeValueAsString(msg);
                session.sendMessage(new TextMessage(json));
            }
        } catch (Exception e) {
            throw new RuntimeException("网络异常");
        }
    }


    //传递删除好友信息，让会话中所有用户的列表中删除对应会话
    public void sendDeleteMessage(Integer conversationId,List<Integer> userIds){
        for(Integer userId : userIds) {
            WebSocketSession session = sessions.get(userId);
            try{
                if (session != null && session.isOpen()) {
                    SocketMessage<Integer> msg = new SocketMessage<>(WebSocketConstant.WEBSOCKET_FRIEND_DELETE,conversationId);
                    String json = mapper.writeValueAsString(msg);
                    session.sendMessage(new TextMessage(json));
                }
            } catch (Exception e){
                throw new RuntimeException("网络异常");
            }
        }
    }
}
