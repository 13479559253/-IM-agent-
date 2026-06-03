package com.example.demo.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    private Integer id;
    private String content;
    private Integer senderId;
    private Integer conversationId;
    private LocalDateTime sendTime;
}
