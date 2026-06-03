package com.example.demo.controller;

import com.example.demo.exception.MessageException;
import com.example.demo.pojo.DTO.*;
import com.example.demo.pojo.Message;
import com.example.demo.pojo.Result;
import com.example.demo.service.ConversationService;
import com.example.demo.utils.RegexUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(value = "/conversation")
public class ConversationController {
    @Autowired
    private ConversationService conversationService;

    @PostMapping(value = "/apply")
    public Result<?> conversationApply(@RequestBody FriendRequestDTO friendRequestDTO, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        friendRequestDTO.setSenderId(userId);
        RegexUtil.checkId(friendRequestDTO.getReceiverId());
        RegexUtil.checkId(friendRequestDTO.getSenderId());
        if(friendRequestDTO.getSenderId().equals(friendRequestDTO.getReceiverId())) {
            throw new MessageException("不可以添加自己");
        }
        conversationService.conversationApply(friendRequestDTO.getSenderId(), friendRequestDTO.getReceiverId());
        return Result.success();
    }

    @GetMapping(value = "/apply/list")
    public Result<?> conversationApplyList(HttpServletRequest request){
        Integer userId = (Integer) request.getAttribute("userId");
        RegexUtil.checkId(userId);
        List<FriendRequestInfoDTO> list = conversationService.getApplyList(userId);
        return Result.success(list);
    }

    @PostMapping(value = "/apply/agree")
    public Result<?> conversationAgree(@RequestBody FriendRequestDTO friendRequestDTO,HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        friendRequestDTO.setReceiverId(userId);
        RegexUtil.checkId(friendRequestDTO.getSenderId());
        RegexUtil.checkId(friendRequestDTO.getReceiverId());
        RegexUtil.checkId(friendRequestDTO.getFriendRequestId());
        if(friendRequestDTO.getSenderId().equals(friendRequestDTO.getReceiverId())) {
            throw new MessageException("不可以同意自己");
        }
        conversationService.conversationAgree(friendRequestDTO);
        return Result.success();
    }

    @GetMapping(value = "/list")
    public Result<?> conversationList(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        RegexUtil.checkId(userId);
        List<conversationDTO> list = conversationService.conversationList(userId);
        return Result.success(list);
    }

    @GetMapping(value = "/message/list")
    public Result<?> messageList(@RequestParam("conversationId") Integer conversationId,HttpServletRequest request) {
        RegexUtil.checkId(conversationId);
        Integer userId = (Integer) request.getAttribute("userId");
        RegexUtil.checkId(userId);
        List<MessageDTO> list = conversationService.messageList(conversationId,userId);
        return Result.success(list);
    }

    @PostMapping(value = "/send/message")
    public Result<?> sendMessage(@RequestBody MessageDTO messageDTO, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        RegexUtil.checkId(userId);
        Message message = new Message();
        message.setSenderId(userId);
        message.setSendTime(LocalDateTime.now());
        message.setContent(messageDTO.getContent());
        message.setConversationId(messageDTO.getConversationId());
        conversationService.sendMessage(message);
        return Result.success();
    }

    @DeleteMapping(value = "/delete")
    public Result<?> deleteConversation(@RequestParam("conversationId") Integer conversationId,HttpServletRequest request) {
        RegexUtil.checkId(conversationId);
        Integer userId = (Integer) request.getAttribute("userId");
        conversationService.deleteConversation(conversationId,userId);
        return Result.success();
    }
 }
