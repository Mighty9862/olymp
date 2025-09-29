package org.example.util;

import org.example.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secretKey;

    private static final long EXPIRATION_TIME = 86400000; // 24 часа

    public String generateToken(String email, Role role) {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role.name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public String extractEmail(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());
        Claims claims = Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseSignedClaims(token)
                .getBody();
        return claims.getSubject();
    }

    public Role extractRole(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());
        Claims claims = Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseSignedClaims(token)
                .getBody();
        return Role.valueOf(claims.get("role", String.class));
    }

    public boolean isTokenValid(String token, String email) {
        return (email.equals(extractEmail(token)) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());
        return Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseSignedClaims(token)
                .getBody()
                .getExpiration()
                .before(new Date());
    }
}