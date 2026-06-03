package com.example.demo.mapper;

import com.example.demo.pojo.DTO.RegisterDTO;
import com.example.demo.pojo.DTO.UserInfoDTO;
import com.example.demo.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    void register(User user);

    User selectByPhone(String phone);

    UserInfoDTO getUserInfo(@Param("userId") Integer userId,@Param("phone") String phone);

    void updateUserInfo(UserInfoDTO userInfoDTO);
}
