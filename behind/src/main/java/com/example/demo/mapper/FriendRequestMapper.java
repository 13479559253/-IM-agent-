package com.example.demo.mapper;

import com.example.demo.pojo.DTO.FriendRequestInfoDTO;
import com.example.demo.pojo.DTO.UserInfoDTO;
import com.example.demo.pojo.FriendRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FriendRequestMapper {
    void addFriendRequest(FriendRequest friendRequest);

    void changeStatus(@Param("friendRequestId") Integer friendRequestId, @Param("status") Integer status);

    Integer checkFriendRequest(@Param("senderId") Integer senderId,@Param("receiverId") Integer receiverId);

    List<FriendRequestInfoDTO> getFriendRequest(Integer userId);

    void deleteFriendRequest(Integer conversationId);
}
