package com.example.demo.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Conversation {
    private Integer id;
    private Integer type;
    private String title;
    private String lastMessage;
    private LocalDateTime lastTime;
    private LocalDateTime createTime;
}
