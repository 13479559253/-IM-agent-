package com.example.demo.utils;

import com.example.demo.exception.LoginException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {
    @Value("${jwt_key}")
    private String KEY;

    private static final Integer ExpireTime = 3600 * 3000;

    private Key GetKey(){return Keys.hmacShaKeyFor(KEY.getBytes());}

    public String CreateToken(Map<String,Object> claims) {
        return Jwts.builder()
                .signWith(GetKey())
                .setClaims(claims)
                .setExpiration(new Date(System.currentTimeMillis() + ExpireTime))
                .compact();
    }

    public Claims ParseToken(String token) {
        try{
            return Jwts.parserBuilder().setSigningKey(GetKey()).build().parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e){
            throw new LoginException("令牌过期，请重新登录");
        } catch (Exception e){
            throw new LoginException("令牌无效");
        }
    }
}
