package com.damian.photogram.core.utils;

import com.damian.photogram.core.security.CustomerDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
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

    public String generateToken(CustomerDetails customerDetails) {
        return generateToken(Map.of(), customerDetails);
    }

    public String generateToken(Map<String, Object> claims, CustomerDetails customerDetails) {
        return generateToken(claims, customerDetails.getEmail());
    }

    public String generateToken(String email) {
        return generateToken(Map.of(), email);
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

    public boolean isTokenValid(String token, CustomerDetails customerDetails) {
        final String email = extractEmail(token);
        return (email.equals(customerDetails.getEmail())) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public void printToken(String token) {
        getAllClaims(token).forEach((k, v) -> System.out.println(k + ": " + v));
    }
}