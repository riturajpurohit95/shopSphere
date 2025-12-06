package com.ShopSphere.shop_sphere.security;
 
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;
 
//@Component
//public class JwtTokenUtil {
// 
//    private final String SECRET = "THISISAVERYLONGSECRETKEYFORJWT256ENCRYPTION1234567890";
//    private final long EXPIRATION = 1000 * 60 * 60 * 10; // 10 hours
// 
//    public String generateToken(int userId, String role, String email) {
//        return Jwts.builder()
//                .claim("userId", userId)
//                .claim("role", role)
//                .claim("email", email)
//                .setIssuedAt(new Date())
//                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
//                .signWith(SignatureAlgorithm.HS256, SECRET)
//                .compact();
//    }
// 
//    public Claims validate(String token) {
//        return Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token).getBody();
//    }
//}


 
@Component
public class JwtTokenUtil {
 
    private static final String SECRET = "THISISAVERYLONGSECRETKEYFORJWT256ENCRYPTION1234567890";
    private static final long EXPIRATION = 1000 * 60 * 60 * 10; // 10 hours
 
    private SecretKey SECRET_KEY;
 
    @PostConstruct
    public void init() {
        SECRET_KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }
 
    // ------------------ Generate Token -------------------
    public String generateToken(int userId, String role, String email) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("role", role)
                .claim("email", email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }
 
    // ------------------ Validate Token -------------------
    public Claims validate(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)   // must match signing key
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
 
    // ------------------ Extract Role --------------------
    public String extractRole(String token) {
        return validate(token).get("role", String.class);
    }
 
    // ------------------ Extract User ID -------------------
    public Integer extractUserId(String token) {
        return validate(token).get("userId", Integer.class);
    }
 
    // ------------------ Extract Email ---------------------
    public String extractEmail(String token) {
        return validate(token).get("email", String.class);
    }
}
 