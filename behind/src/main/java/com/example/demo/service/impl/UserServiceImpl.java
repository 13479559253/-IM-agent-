package com.example.demo.service.impl;

import com.example.demo.constant.WebSocketConstant;
import com.example.demo.exception.MessageException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.pojo.DTO.RegisterDTO;
import com.example.demo.pojo.DTO.UserInfoDTO;
import com.example.demo.pojo.User;
import com.example.demo.service.UserService;
import com.example.demo.utils.JwtUtil;
import com.example.demo.utils.RedisUtil;
import com.example.demo.websocket.ChatWebsocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;


@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ChatWebsocket chatWebsocket;

    //生成随机四位数验证码
    private String generateCode(){
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int code = random.nextInt(1000, 10000);
        return String.valueOf(code);
    }

    //发送验证码（这里先使用的sout代替，后续更换第三方api真正实现发送验证码短信）
    public void sendCode(String phone,String type) {
        String codeKey = switch (type) {
            case "LOGIN" -> "LOGIN:CODE:" + phone;
            case "REGISTER" -> "REGISTER:CODE:" + phone;
            default -> throw new MessageException("验证码类型错误");
        };
        String code = generateCode();
        if(!redisUtil.setnx(codeKey, code, 120)){
            throw new MessageException("验证码已发送请稍后");
        }
        System.out.println("向电话号码为:" + phone + "发送了验证码:" + code);
    }

    //通过密码登录逻辑（超过5次自动锁10分钟）
    public String loginByPassword(RegisterDTO registerDTO) {
        Object countObj = redisUtil.get("LOGIN:COUNT:" + registerDTO.getPhone());
        int count =  countObj == null ? 0 : Integer.parseInt(String.valueOf(countObj));
        if(count >= 5){
            throw new MessageException("错误次数过多，请稍后再试");
        }
        User u = userMapper.selectByPhone(registerDTO.getPhone());
        if(u == null){
            redisUtil.set("LOGIN:COUNT:" + registerDTO.getPhone(), count + 1, 600);
            throw new MessageException("用户信息错误，请检查电话号码或密码");
        }
        if(!BCrypt.checkpw(registerDTO.getPassword(), u.getPassword())){
            redisUtil.set("LOGIN:COUNT:" + registerDTO.getPhone(), count + 1, 600);
            throw new MessageException("用户信息错误，请检查电话号码或密码");
        }
        redisUtil.remove("LOGIN:COUNT:" + registerDTO.getPhone());
        Map<String,Object> map = new HashMap<>();
        map.put("id",u.getId());
        return jwtUtil.CreateToken(map);
    }

    //通过验证码登录
    public String loginByCode(RegisterDTO registerDTO) {
        User u = userMapper.selectByPhone(registerDTO.getPhone());
        if(u == null){
            throw new MessageException("用户信息错误，请检查电话号码或验证码");
        }
        String code_key = "LOGIN:CODE:" + registerDTO.getPhone();
        String code = (String)redisUtil.get(code_key);
        if(code == null){
            throw new MessageException("用户信息错误，请检查电话号码或验证码");
        }
        if(!code.equals(registerDTO.getCode())){
            throw new MessageException("用户信息错误，请检查电话号码或验证码");
        }
        Map<String,Object> map = new HashMap<>();
        map.put("id",u.getId());
        return jwtUtil.CreateToken(map);
    }

    //电话号码+验证码进行注册
    public void register(RegisterDTO registerDTO) {
        User u = userMapper.selectByPhone(registerDTO.getPhone());
        if(u != null){
            throw new MessageException("用户已注册或验证码错误");
        }
        String code_key = "REGISTER:CODE:" + registerDTO.getPhone();
        String code = (String)redisUtil.get(code_key);
        if(code == null){
            throw new MessageException("用户已注册或验证码错误");
        }
        if(!code.equals(registerDTO.getCode())){
            throw new MessageException("用户已注册或验证码错误");
        }
        User user = new User();
        user.setPhone(registerDTO.getPhone());
        user.setPassword(BCrypt.hashpw(registerDTO.getPassword(), BCrypt.gensalt()));
        user.setLastLoginTime(LocalDateTime.now());
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        try {
            userMapper.register(user);
        } catch (DuplicateKeyException e){
            throw new MessageException("请勿重复提交");
        }
        redisUtil.remove(code_key);
    }

    //查询用户信息（用于自身信息展示和添加好友与收到好友请求时查看他人信息）
    public UserInfoDTO getUserInfo(Integer userId,String phone) {
        return userMapper.getUserInfo(userId,phone);
    }

    //修改自身信息（同时要进行websocket提示前端进行更改）
    public void modifyUserInfo(UserInfoDTO userInfoDTO) {
        //数据库修改信息
        userMapper.updateUserInfo(userInfoDTO);

        //websocket发通知
        chatWebsocket.sendEmptyMessage(userInfoDTO.getId(), WebSocketConstant.WEBSOCKET_INFO_MODIFY);
    }
}
