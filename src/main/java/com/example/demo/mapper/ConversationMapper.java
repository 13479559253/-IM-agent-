package com.example.demo.mapper;

import com.example.demo.pojo.Conversation;
import com.example.demo.pojo.Message;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ConversationMapper {
    void conversationNew(Conversation conversation);

    void newMessage(Message message);

    Conversation getConversationById(Integer conversationId);

    void deleteConversation(Integer conversationId);
}
