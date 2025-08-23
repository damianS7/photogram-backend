package com.damian.photogram.core.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtUtil {
    @Value("${jwt.secret}")
    private String SECRET_KEY;

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(getAllClaims(token));
    }

    private Claims getAllClaims(String token) {
        return Jwts.parserBuilder()
                   .setSigningKey(getSigningKey())
                   .build()
                   .parseClaimsJws(token)
                   .getBody();
    }

    public String generateToken(String email, Date expiration) {
        return Jwts.builder()
                   .setClaims(Map.of())
                   .setSubject(email)
                   .setIssuedAt(new Date())
                   .setExpiration(expiration) // 1 hora
                   .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                   .compact();
    }

    public String generateToken(Map<String, Object> claims, String email) {
        return Jwts.builder()
                   .setClaims(claims)
                   .setSubject(email)
                   .setIssuedAt(new Date())
                   .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hora
                   .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                   .compact();
    }

    public boolean isTokenExpired(String token) {
        try {
            Date expiration = extractClaim(token, Claims::getExpiration);
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public void printToken(String token) {
        getAllClaims(token).forEach((k, v) -> System.out.println(k + ": " + v));
    }

    public boolean isTokenValid(String token) {
        try {
            getAllClaims(token);
        } catch (ExpiredJwtException e) {
            return true;
        } catch (MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            return false;
        }
        return true;
    }
}