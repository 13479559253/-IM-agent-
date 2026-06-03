package com.example.demo.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FriendRequestInfoDTO {
    private Integer friendRequestId;
    private Integer senderId;
    private String nickname;
    private String phone;
    private String email;
    private String createTime;
}
