package com.register.byt.commons.utils;

import io.jsonwebtoken.*;
import org.springframework.util.StringUtils;

import java.util.Date;

/**
 * @author LLXX
 * @create 2021-08-11 17:52
 */
public class JwtUtil { // JWT工具类

    private static long TOKEN_EXPIRATION = 24*60*60*1000; // token到期时间
    private static String APP_SECRET = "ukc8BDbRigUDaY6pZFfWus2jZWLPHO"; // 密钥（Key）

    public static String createToken(Long userId, String name) {
        String token = Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("alg", "HS256")
                .setSubject("BYT-USER")
                .setExpiration(new Date(System.currentTimeMillis() + TOKEN_EXPIRATION))
                .claim("userId", userId)
                .claim("name", name)
                .signWith(SignatureAlgorithm.HS256, APP_SECRET)
                .compressWith(CompressionCodecs.GZIP)
                .compact();
        return token;
    }

    /**
     *  根据token获取用户id
     * @param token
     * @return
     */
    public static Long getUserId(String token) {
        if(StringUtils.isEmpty(token)) return null;
        Jws<Claims> claimsJws = Jwts.parser().setSigningKey(APP_SECRET).parseClaimsJws(token);
        Claims claims = claimsJws.getBody();
        Integer userId = (Integer)claims.get("userId");
        return userId.longValue();
    }

    /**
     * 根据token获取用户昵称
     * @param token
     * @return
     */
    public static String getUserName(String token) {
        if(StringUtils.isEmpty(token)) return "";
        Jws<Claims> claimsJws
                = Jwts.parser().setSigningKey(APP_SECRET).parseClaimsJws(token);
        Claims claims = claimsJws.getBody();
        return (String)claims.get("name");
    }

    // 测试
    public static void main(String[] args) {
        String token = JwtUtil.createToken(1L, "张三");
        System.out.println(token);
        System.out.println(JwtUtil.getUserId(token));
        System.out.println(JwtUtil.getUserName(token));
    }
}

