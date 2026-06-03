package com.example.demo.service.impl;

import com.example.demo.constant.FriendRequestConstant;
import com.example.demo.constant.WebSocketConstant;
import com.example.demo.exception.MessageException;
import com.example.demo.mapper.ConversationMapper;
import com.example.demo.mapper.ConversationUserMapper;
import com.example.demo.mapper.FriendRequestMapper;
import com.example.demo.mapper.MessageMapper;
import com.example.demo.pojo.Conversation;
import com.example.demo.pojo.DTO.*;
import com.example.demo.pojo.FriendRequest;
import com.example.demo.pojo.Message;
import com.example.demo.service.ConversationService;
import com.example.demo.websocket.ChatWebsocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class ConversationServiceImpl implements ConversationService {
    @Autowired
    private ConversationUserMapper conversationUserMapper;
    @Autowired
    private ConversationMapper conversationMapper;
    @Autowired
    private FriendRequestMapper friendRequestMapper;
    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private ChatWebsocket chatWebsocket;

    @Transactional(rollbackFor = Exception.class)
    public void conversationApply(Integer senderId, Integer receiverId) {

        //先进行查询好友请求，如果已经发送不给持续发送，如果已经是好友返回错误信息
        Integer flag = friendRequestMapper.checkFriendRequest(senderId,receiverId);
        if(flag != null && flag == 0){
            throw new MessageException("已经发送好友申请，请等待对方处理");
        }
        if(flag != null && flag == 1){
            throw new MessageException("你们已经是好友了哦");
        }

        //创建好友申请实体类，然后修改好友申请数据表，设置状态为0，表示待处理
        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setSenderId(senderId);
        friendRequest.setReceiverId(receiverId);
        friendRequest.setStatus(FriendRequestConstant.FRIEND_REQUEST);
        friendRequest.setCreateTime(LocalDateTime.now());
        friendRequestMapper.addFriendRequest(friendRequest);

        //websocket传信息，告知他人好友申请通知
        FriendRequestDTO friendRequestDTO = new FriendRequestDTO(senderId, receiverId, friendRequest.getId());
        chatWebsocket.sendApplyMessage(friendRequestDTO);
    }

    //实现查询用户id的所有好友申请
    public List<FriendRequestInfoDTO> getApplyList(Integer userId){
        return friendRequestMapper.getFriendRequest(userId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void conversationAgree(FriendRequestDTO friendRequestDTO) {
        //将好友请求表中的状态改为已接受
        friendRequestMapper.changeStatus(friendRequestDTO.getFriendRequestId(),FriendRequestConstant.FRIEND_REQUEST_AGREE);

        //创建新会话并且获取会话id
        Conversation conversation = new Conversation();
        conversation.setType(0);
        conversation.setLastMessage("我们已经是好友了，快来一起聊天吧");
        conversation.setLastTime(LocalDateTime.now());
        conversation.setCreateTime(LocalDateTime.now());
        conversationMapper.conversationNew(conversation);

        //添加会话中的用户信息
        conversationUserMapper.conversationUserNew(conversation.getId(),friendRequestDTO.getSenderId(),1);
        conversationUserMapper.conversationUserNew(conversation.getId(),friendRequestDTO.getReceiverId(),0);

        //添加消息表信息，添加好友成功默认向发送请求方发出 （“我们已经是好友了，快来一起聊天吧”）
        Message message = new Message();
        message.setSenderId(friendRequestDTO.getReceiverId());
        message.setConversationId(conversation.getId());
        message.setContent("我们已经是好友了，快来一起聊天吧");
        message.setSendTime(LocalDateTime.now());
        messageMapper.addMessage(message);

        chatWebsocket.sendAgreeMessage(friendRequestDTO);
    }

    //实现查询对应用户id的全部会话信息
    public List<conversationDTO> conversationList(Integer userId) {
        return conversationUserMapper.getConversation(userId);
    }

    //实现查询对应会话id的全部消息
    @Transactional(rollbackFor = Exception.class)
    public List<MessageDTO> messageList(Integer conversationId,Integer userId) {
        List<MessageDTO> messageDTOS = messageMapper.getMessage(conversationId);
        conversationUserMapper.clearUnreadCount(conversationId,userId);

        //这里使用一个websocket向userId的用户发送一个消息已读信息，清除界面的未读数
        chatWebsocket.sendEmptyMessage(userId, WebSocketConstant.WEBSOCKET_CLEAR_UNREAD);

        return messageDTOS;
    }

    //实现用户发送信息
    @Transactional(rollbackFor = Exception.class)
    public void sendMessage(Message message) {
        //先查询会话是否还存在（有可能被对面删了）
        Conversation conversation = conversationMapper.getConversationById(message.getConversationId());
        if(conversation == null){
            throw new MessageException("对方已经把你删除，消息发送失败");
        }

        //实现查到所有会话中非发送方的用户进行unreadCount+1
        conversationUserMapper.addUnreadCount(message);

        //实现更新会话最新消息和发送时间
        conversationMapper.newMessage(message);

        //实现message表加入新的message
        messageMapper.addMessage(message);

        message.setSendTime(null);
        //实现websocket通知所有用户进行消息渲染和未读数
        chatWebsocket.sendMessage(message);
    }

    //删除好友
    @Transactional(rollbackFor = Exception.class)
    public void deleteConversation(Integer conversationId,Integer userId) {
        //先鉴权，判断当前令牌提供的id是否是会话中的成员（否则会出现删除他人之间的会话的越权行为）
        List<Integer> list = conversationUserMapper.getUsersId(conversationId);
        if(list == null || list.isEmpty()){
            throw new MessageException("会话不存在，删除失败");
        }
        if(!list.contains(userId)){
            throw new MessageException("非会话成员，不可删除");
        }

        //删除friend_request表中的所有请求
        friendRequestMapper.deleteFriendRequest(conversationId);

        //删除conversation表中的对应会话
        conversationMapper.deleteConversation(conversationId);

        //删除conversation_user表中的会话关系
        conversationUserMapper.deleteConversationUser(conversationId);

        //删除message表所有有关的message
        messageMapper.deleteMessage(conversationId);

        //websocket发送消息通知前端更新会话列表
        chatWebsocket.sendDeleteMessage(conversationId,list);
    }
}
