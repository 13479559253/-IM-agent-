package com.example.demo.controller;

import com.example.demo.exception.MessageException;
import com.example.demo.pojo.DTO.RegisterDTO;
import com.example.demo.pojo.DTO.UserInfoDTO;
import com.example.demo.pojo.Result;
import com.example.demo.service.UserService;
import com.example.demo.utils.RegexUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/user")
public class UserController {

    @Autowired
    private UserService userService;


    @PostMapping(value = "/register")
    public Result<?> register(@RequestBody RegisterDTO registerDTO) {
        RegexUtil.checkPhone(registerDTO.getPhone());
        userService.register(registerDTO);
        return Result.success();
    }

    @GetMapping(value = "/code")
    public Result<?> sendCode(@RequestParam("phone") String phone,@RequestParam("type") String type) {
        RegexUtil.checkPhone(phone);
        userService.sendCode(phone,type);
        return Result.success();
    }

    @PostMapping(value = "/login/password")
    public Result<?> loginByPassword(@RequestBody RegisterDTO registerDTO){
        RegexUtil.checkPhone(registerDTO.getPhone());
        RegexUtil.checkPassword(registerDTO.getPassword());
        String token = userService.loginByPassword(registerDTO);
        return Result.success(token);
    }

    @PostMapping(value = "/login/code")
    public Result<String> LoginByCode(@RequestBody RegisterDTO registerDTO){
        RegexUtil.checkPhone(registerDTO.getPhone());
        String token = userService.loginByCode(registerDTO);
        return Result.success(token);
    }

    @GetMapping(value = "/info")
    public Result<UserInfoDTO> getUserInfo(@RequestParam(value = "userId",required = false) Integer userId,
                                           @RequestParam(value = "phone",required = false) String phone,
                                           HttpServletRequest request){

        if(userId != null){
            RegexUtil.checkId(userId);
        }
        if(phone != null){
            RegexUtil.checkPhone(phone);
        }
        if(phone == null && userId == null){
            userId = (Integer) request.getAttribute("userId");
        }
        UserInfoDTO u = userService.getUserInfo(userId,phone);
        return Result.success(u);
    }

    @PostMapping(value = "/info/modify")
    public Result<?> modifyUserInfo(@RequestBody UserInfoDTO userInfoDTO, HttpServletRequest request){
        Integer userId = (Integer) request.getAttribute("userId");
        if(userInfoDTO.getId() != null && !userId.equals( userInfoDTO.getId())){
            throw new MessageException("不可修改他人信息");
        }
        userInfoDTO.setId(userId);
        RegexUtil.checkId(userInfoDTO.getId());
        RegexUtil.checkEmail(userInfoDTO.getEmail());
        userService.modifyUserInfo(userInfoDTO);
        return Result.success();
    }
}
