package com.nghiashop.ecome_backend.config;

import java.security.Key;
import java.util.Date;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

@Component
public class JwtUtil {

    private static final String SECRET_KEY = "ecome_secret_key_ecome_secret_key_123456";
    private static final long EXPIRATION = 86400000; // 24 hours

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        try {
            return extractAllClaims(token).getSubject();
        } catch (ExpiredJwtException e) {
            System.out.println("❌ Token expired: " + e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            System.out.println("❌ Malformed token: " + e.getMessage());
            throw e;
        } catch (SignatureException e) {
            System.out.println("❌ Invalid signature: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.out.println("❌ Token extraction error: " + e.getMessage());
            throw e;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    private Boolean isTokenExpired(String token) {
        try {
            Date expiration = extractExpiration(token);
            boolean expired = expiration.before(new Date());
            if (expired) {
                System.out.println("❌ Token is expired. Expiration: " + expiration);
            }
            return expired;
        } catch (Exception e) {
            System.out.println("❌ Error checking expiration: " + e.getMessage());
            return true;
        }
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String email = extractEmail(token);
            boolean isValid = email.equals(userDetails.getUsername()) && !isTokenExpired(token);
            
            if (isValid) {
                System.out.println("✅ Token is valid for user: " + email);
            } else {
                System.out.println("❌ Token validation failed. Email match: " + 
                    email.equals(userDetails.getUsername()) + ", Expired: " + isTokenExpired(token));
            }
            
            return isValid;
        } catch (Exception e) {
            System.out.println("❌ Token validation error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}