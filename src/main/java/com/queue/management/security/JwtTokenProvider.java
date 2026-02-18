package com.queue.management.security;

import com.queue.management.enums.CounterName;
import com.queue.management.enums.UserType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    // Generate signing key from secret
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Generate JWT token for STUDENT
    public String generateTokenForStudent(String rollNumber, String name) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userType", UserType.STUDENT.name());
        claims.put("name", name);
        return buildToken(claims, rollNumber);
    }

    // Generate JWT token for COUNTER STAFF
    public String generateTokenForStaff(String staffId,
                                        String name,
                                        CounterName assignedCounter) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userType", UserType.COUNTER_STAFF.name());
        claims.put("name", name);
        claims.put("assignedCounter", assignedCounter.name());
        return buildToken(claims, staffId);
    }

    // Build the actual JWT token (NEW 0.12.x API)
    private String buildToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    // Extract username from token
    public String getUsernameFromToken(String token) {
        return getClaims(token).getSubject();
    }

    // Extract user type from token
    public UserType getUserTypeFromToken(String token) {
        String userType = getClaims(token).get("userType", String.class);
        return UserType.valueOf(userType);
    }

    // Extract assigned counter from token (for staff)
    public CounterName getAssignedCounterFromToken(String token) {
        String counter = getClaims(token).get("assignedCounter", String.class);
        if (counter == null) return null;
        return CounterName.valueOf(counter);
    }

    // Extract name from token
    public String getNameFromToken(String token) {
        return getClaims(token).get("name", String.class);
    }

    // Validate token
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    // Check if token is expired
    public boolean isTokenExpired(String token) {
        Date expiry = getClaims(token).getExpiration();
        return expiry.before(new Date());
    }

    // Get all claims from token (NEW 0.12.x API)
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}