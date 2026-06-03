package com.example.demo.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FriendRequest {
    private Integer id;
    private Integer senderId;
    private Integer receiverId;
    private Integer status;
    private LocalDateTime createTime;
}
