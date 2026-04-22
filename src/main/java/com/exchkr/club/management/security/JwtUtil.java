package com.exchkr.club.management.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.UUID;


@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    private final String secretKey;
    private final Algorithm algorithm;

    // Expiration Constants - Public for use in AuthenticationServiceImpl
    public static final long ACCESS_EXPIRATION_MS = 1000 * 60 * 60 * 24; 
    public static final long REFRESH_EXPIRATION_MS = 1000 * 60 * 60 * 24 * 7; 

    public JwtUtil(@Value("${jwt.secret.key}") String secretKey) {
        this.secretKey = secretKey;
        this.algorithm = Algorithm.HMAC256(secretKey);
    }

    public String generateAccessToken(String email, Long userId, Long clubId, List<String> roles) {
        String jti = UUID.randomUUID().toString();
        return JWT.create()
                .withJWTId(jti)
                .withSubject(email)
                .withClaim("userId", userId)
                .withClaim("clubId", clubId)
                .withClaim("roles", roles)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + ACCESS_EXPIRATION_MS))
                .sign(this.algorithm);
    }


    public String generateRefreshToken(String email, Long userId) {
        logger.info("Generating refresh token for email: {}", email);
        return JWT.create()
        		.withJWTId(UUID.randomUUID().toString())
                .withSubject(email)
                .withClaim("userId", userId)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION_MS))
                .sign(this.algorithm);
    }

    public boolean isTokenValid(String token) {
        try {
            DecodedJWT decodedJWT = JWT.require(this.algorithm) 
                    .build()
                    .verify(token);
            logger.info("Token is valid, expiry date: {}", decodedJWT.getExpiresAt());
            return !decodedJWT.getExpiresAt().before(new Date()); 
        } catch (Exception e) {
            logger.error("Invalid or expired token", e);
            return false;
        }
    }

    public String extractUsername(String token) {
        DecodedJWT decodedJWT = JWT.require(this.algorithm)
                .build()
                .verify(token);
        String username = decodedJWT.getSubject();
        logger.info("Extracted username from token: {}", username);
        return username;
    }
    
    public List<String> extractRoles(String token) {
        DecodedJWT decodedJWT = JWT.require(this.algorithm)
                .build()
                .verify(token);
        return decodedJWT.getClaim("roles").asList(String.class);
    }
    
    public String extractJti(String token) {
        DecodedJWT decodedJWT = JWT.require(this.algorithm)
                .build()
                .verify(token);
        return decodedJWT.getId();
    }
    
    public Long extractClubId(String token) {
        DecodedJWT decodedJWT = JWT.require(this.algorithm).build().verify(token);
        return decodedJWT.getClaim("clubId").asLong();
    }
    
    public Long extractUserId(String token) {
        DecodedJWT decodedJWT = JWT.require(this.algorithm)
                .build()
                .verify(token);
        return decodedJWT.getClaim("userId").asLong();
    }

}