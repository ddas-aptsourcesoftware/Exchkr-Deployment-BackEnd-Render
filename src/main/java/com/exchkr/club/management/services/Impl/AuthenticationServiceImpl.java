package com.exchkr.club.management.services.Impl;

import com.exchkr.club.management.dao.UserClubMembershipProjection;
import com.exchkr.club.management.dao.UserRepository;
import com.exchkr.club.management.dao.UserWithClubProjection;
import com.exchkr.club.management.model.api.request.AuthRequest;
import com.exchkr.club.management.model.entity.User;
import com.exchkr.club.management.model.api.response.LoginResponse;
import com.exchkr.club.management.model.dto.UserDTO; 
import com.exchkr.club.management.security.JwtUtil;
import com.exchkr.club.management.services.AuthenticationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    
    private final boolean cookieSecure;

    public AuthenticationServiceImpl(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder, @Value("${app.security.cookie-secure}") boolean cookieSecure) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.cookieSecure = cookieSecure;
    }

    private void setTokenCookie(HttpServletResponse response, String name, String token, long maxAgeMs) {
        int maxAgeSeconds = (int) (maxAgeMs / 1000); 

        Cookie cookie = new Cookie(name, token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");      
        cookie.setMaxAge(maxAgeSeconds); 

        cookie.setSecure(this.cookieSecure);  

        response.addCookie(cookie);
        logger.info("Set cookie: {} (Secure: false) for local HTTP.", name);
    }

    @Override
    public LoginResponse login(AuthRequest request, HttpServletResponse response) {
        User user = userRepository.findUserByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
             throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        // Instead of generating a token, find all associated clubs
        List<UserClubMembershipProjection> memberships = userRepository.findAllMembershipsByUserId(user.getUserId());

        if (memberships.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not associated with any active clubs.");
        }

        // Return a response that tells the Frontend: "Pick one of these clubs"
        return new LoginResponse("Please select a club", memberships, user.getUserId());
    }
    
    @Override
    public LoginResponse selectClub(Long userId, Long clubId, HttpServletResponse response) {
        // 1. Fetch user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // 2. Fetch specific membership to get the Role and Club Name
        List<UserClubMembershipProjection> memberships = userRepository.findAllMembershipsByUserId(userId);
        
        UserClubMembershipProjection selected = memberships.stream()
                .filter(m -> m.getClubId().equals(clubId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a member of this club"));

        List<String> roles = Collections.singletonList(selected.getRoleName());

        // 3. Generate tokens with clubId embedded
        String accessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getUserId(), clubId, roles);
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail(), user.getUserId());

        setTokenCookie(response, "accessToken", accessToken, JwtUtil.ACCESS_EXPIRATION_MS);
        setTokenCookie(response, "refreshToken", refreshToken, JwtUtil.REFRESH_EXPIRATION_MS);

        // 4. Return UserDTO with selected club context
        UserDTO userDTO = UserDTO.fromUser(user, roles, selected.getClubName(), clubId, selected.getJoinedAt());
        userDTO.setClubId(clubId);
        userDTO.setRoleId(selected.getRoleId());

        return new LoginResponse("Login successful for " + selected.getClubName(), userDTO);
    }
    

    @Override
    public LoginResponse refreshToken(String oldRefreshToken, HttpServletResponse response) {
        if (!jwtUtil.isTokenValid(oldRefreshToken)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid or expired refresh token.");
        }

        String userEmail = jwtUtil.extractUsername(oldRefreshToken);
        User user = userRepository.findUserByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // IMPORTANT: Since a refresh token doesn't know which club was active, 
        // we either need the frontend to pass the active clubId, 
        // or we default to the first one (not ideal), or we extract it from the expired access token.
        
        // For now, let's assume we fetch the "primary" or first active membership
        List<UserClubMembershipProjection> memberships = userRepository.findAllMembershipsByUserId(user.getUserId());
        if (memberships.isEmpty()) {
             throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No active memberships found.");
        }
        
        // Logic: Pick the first membership or implement a "Last Used Club" logic in DB
        UserClubMembershipProjection defaultMembership = memberships.get(0); 
        List<String> roles = Collections.singletonList(defaultMembership.getRoleName());

        String newAccessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getUserId(), defaultMembership.getClubId(), roles);
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getEmail(), user.getUserId());

        setTokenCookie(response, "accessToken", newAccessToken, JwtUtil.ACCESS_EXPIRATION_MS);
        setTokenCookie(response, "refreshToken", newRefreshToken, JwtUtil.REFRESH_EXPIRATION_MS);

        return new LoginResponse(
            "Token refreshed successfully",
            UserDTO.fromUser(user, roles, defaultMembership.getClubName(), defaultMembership.getClubId(), defaultMembership.getJoinedAt())
        );
    }
}