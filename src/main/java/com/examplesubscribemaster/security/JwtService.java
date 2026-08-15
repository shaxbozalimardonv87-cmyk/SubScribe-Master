package com.examplesubscribemaster.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey key;
    private final long accessMinutes;

    public JwtService(@Value("${app.jwt.secret}") String secret, @Value("${app.jwt.access-minutes:15}") long accessMinutes) {
        if (secret.length() < 32) throw new IllegalArgumentException("JWT secret must be at least 32 characters");
        key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessMinutes = accessMinutes;
    }

    public String generateAccessToken(Long id, String email, String role) {
        Instant n = Instant.now();
        return Jwts.builder().subject(email).claim("userId", id).claim("role", role).issuedAt(Date.from(n)).expiration(Date.from(n.plusSeconds(accessMinutes * 60))).signWith(key).compact();
    }

    public String extractUsername(String t) {
        return claims(t).getSubject();
    }

    public Claims claims(String t) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(t).getPayload();
    }

    public boolean isValid(String t) {
        try {
            return claims(t).getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
