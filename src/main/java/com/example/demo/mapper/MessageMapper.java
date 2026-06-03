package com.example.demo.mapper;

import com.example.demo.pojo.DTO.MessageDTO;
import com.example.demo.pojo.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {
    void addMessage(Message msg);

    List<MessageDTO> getMessage(Integer conversationId);

    void deleteMessage(Integer conversationId);
}
