package com.example.demo.service;

import com.example.demo.pojo.DTO.RegisterDTO;
import com.example.demo.pojo.DTO.UserInfoDTO;

public interface UserService {

    void register(RegisterDTO registerDTO);

    void sendCode(String phone,String type);

    String loginByPassword(RegisterDTO registerDTO);

    String loginByCode(RegisterDTO registerDTO);

    UserInfoDTO getUserInfo(Integer userId,String phone);

    void modifyUserInfo(UserInfoDTO userInfoDTO);
}
