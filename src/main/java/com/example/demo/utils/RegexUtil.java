package com.example.demo.utils;

import com.example.demo.exception.RegexException;

public class RegexUtil {
    public static void checkPhone(String phone){
        if(phone == null || phone.isEmpty()){
            throw new RegexException("电话号码不可为空");
        }
        String phoneRegx = "^[1][0-9]{10}$";
        if(!phone.matches(phoneRegx)){
            throw new RegexException("电话号码格式错误");
        }
    }
    public static void checkPassword(String password){
        if(password == null || password.isEmpty()){
            throw new RegexException("密码不可为空");
        }
        String passwordRegx = "^(?=.*[a-zA-Z])(?=.*[0-9])[0-9a-zA-Z]{6,12}$";
        if(!password.matches(passwordRegx)){
            throw new RegexException("密码格式错误");
        }
    }
    public static void checkId(Integer id){
        if(id == null || id < 0){
            throw new RegexException("id格式错误");
        }
    }
    public static void checkEmail(String email){
        if(email == null || email.isEmpty()){
            throw new RegexException("邮箱不可为空");
        }
        String emailRegx = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        if(!email.matches(emailRegx)){
            throw new RegexException("邮箱格式错误");
        }
    }
}
