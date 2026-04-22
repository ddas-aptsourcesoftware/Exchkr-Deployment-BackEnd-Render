package com.exchkr.club.management.controller;

import com.exchkr.club.management.dao.TokenBlacklistRepository;
import com.exchkr.club.management.model.api.request.AuthRequest;
import com.exchkr.club.management.model.entity.BlacklistedToken;
import com.exchkr.club.management.model.api.response.LoginResponse;
import com.exchkr.club.management.services.AuthenticationService;
import com.exchkr.club.management.security.JwtUtil;

import com.auth0.jwt.JWT;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@CrossOrigin
@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    private final AuthenticationService authenticationService;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistRepository tokenBlacklistRepository;

    public AuthenticationController(
            AuthenticationService authenticationService,
            JwtUtil jwtUtil,
            TokenBlacklistRepository tokenBlacklistRepository
    ) {
        this.authenticationService = authenticationService;
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistRepository = tokenBlacklistRepository;
    }

    /**
     * Authenticates a user using email and password.
     * <p>
     * On successful authentication, access and refresh tokens are generated.
     * Tokens are returned via HTTP-only cookies and a login response payload.
     * </p>
     * Used during initial user login.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody AuthRequest request,
            HttpServletResponse response) {

        logger.info("Received login request for email: {}", request.getEmail());

        LoginResponse loginResponse = authenticationService.login(request, response);

        logger.info("Login successful for email: {}", request.getEmail());

        return ResponseEntity.ok(loginResponse);
    }
    
    
    @PostMapping("/select-club")
    public ResponseEntity<LoginResponse> selectClub(
            @RequestParam Long userId,
            @RequestParam Long clubId,
            HttpServletResponse response) {
        
        logger.info("User {} selecting club context: {}", userId, clubId);
        LoginResponse loginResponse = authenticationService.selectClub(userId, clubId, response);
        return ResponseEntity.ok(loginResponse);
    }

    /**
     * Issues a new access token using a valid refresh token.
     * <p>
     * The refresh token is read from an HTTP-only cookie.
     * If the token is valid and not expired, a new access token
     * (and optionally a new refresh token) is generated.
     * </p>
     * Used to maintain user sessions without requiring re-login.
     */
    @GetMapping("/refresh-token")
    public ResponseEntity<LoginResponse> refreshToken(
            @CookieValue(name = "refreshToken", required = false) String refreshTokenCookie,
            HttpServletResponse response) {

        if (refreshTokenCookie == null || refreshTokenCookie.isEmpty()) {
            logger.warn("Refresh token cookie not found in request.");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token required.");
        }

        logger.info("Received refresh token request from cookie.");

        LoginResponse loginResponse =
                authenticationService.refreshToken(refreshTokenCookie, response);

        logger.info("Token refreshed successfully");

        return ResponseEntity.ok(loginResponse);
    }

    /**
     * Logs the user out by invalidating the access token and clearing cookies.
     * <p>
     * If the access token is still valid, its JTI is stored in a database-backed
     * blacklist until the token naturally expires.
     * </p>
     * Both access and refresh token cookies are cleared from the client.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "accessToken", required = false) String accessToken,
            HttpServletResponse response
    ) {

        if (accessToken != null && jwtUtil.isTokenValid(accessToken)) {

            String jti = jwtUtil.extractJti(accessToken);
            Date expiresAt = JWT.decode(accessToken).getExpiresAt();

            // Persist token JTI to database blacklist to prevent reuse
            if (jti != null && expiresAt != null) {
                Instant expiryInstant = expiresAt.toInstant();

                // Only blacklist tokens that have not yet expired
                if (expiryInstant.isAfter(Instant.now())) {
                    BlacklistedToken blacklistedToken =
                            new BlacklistedToken(jti, expiryInstant);
                    tokenBlacklistRepository.save(blacklistedToken);

                    logger.info("Token with JTI {} blacklisted in database.", jti);
                }
            }
        }

        // Clear access token cookie
        Cookie clearAccess = new Cookie("accessToken", "");
        clearAccess.setHttpOnly(true);
        clearAccess.setSecure(true);
        clearAccess.setPath("/");
        clearAccess.setMaxAge(0);

        // Clear refresh token cookie
        Cookie clearRefresh = new Cookie("refreshToken", "");
        clearRefresh.setHttpOnly(true);
        clearRefresh.setSecure(true);
        clearRefresh.setPath("/");
        clearRefresh.setMaxAge(0);

        response.addCookie(clearAccess);
        response.addCookie(clearRefresh);

        return ResponseEntity.noContent().build();
    }
}
