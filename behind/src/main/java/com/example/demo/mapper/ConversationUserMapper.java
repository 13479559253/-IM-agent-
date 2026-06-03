package com.example.demo.mapper;

import com.example.demo.pojo.DTO.conversationDTO;
import com.example.demo.pojo.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ConversationUserMapper {
    List<Integer> getUsersId(Integer conversationId);
    void conversationUserNew(@Param("conversationId") Integer conversationId,
                             @Param("userId") Integer userId,
                             @Param("count") Integer count);

    List<conversationDTO> getConversation(Integer userId);

    void clearUnreadCount(@Param("conversationId") Integer conversationId, @Param("userId") Integer userId);

    void addUnreadCount(Message message);

    void deleteConversationUser(Integer conversationId);
}
