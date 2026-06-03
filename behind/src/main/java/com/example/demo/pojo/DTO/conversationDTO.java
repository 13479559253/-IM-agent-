package com.example.demo.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class conversationDTO {
    private Integer conversationId;
    private Integer unreadCount;
    private String lastMessage;
    private LocalDateTime lastTime;
    private String nickname;

}
