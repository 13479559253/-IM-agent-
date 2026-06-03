package com.example.demo.service;


import com.example.demo.pojo.DTO.*;
import com.example.demo.pojo.Message;

import java.util.List;

public interface ConversationService {
    void conversationApply(Integer senderId, Integer receiverId);

    void conversationAgree(FriendRequestDTO friendRequestDTO);

    List<conversationDTO> conversationList(Integer userId);

    List<MessageDTO> messageList(Integer conversationId,Integer userId);

    void sendMessage(Message message);

    List<FriendRequestInfoDTO> getApplyList(Integer userId);

    void deleteConversation(Integer conversationId,Integer userId);
}
