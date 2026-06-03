package com.example.demo.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    public void set(String key,Object value){
        redisTemplate.opsForValue().set(key,value);
    }

    public void set(String key,Object value,Integer expire){
        redisTemplate.opsForValue().set(key,value,expire, TimeUnit.SECONDS);
    }

    public boolean setnx(String key,Object value){
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, value));
    }

    public boolean setnx(String key,Object value,Integer expire){
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, value,expire, TimeUnit.SECONDS));
    }

    public Object get(String key){
        return redisTemplate.opsForValue().get(key);
    }

    public void remove(String key){
        redisTemplate.delete(key);
    }

}
