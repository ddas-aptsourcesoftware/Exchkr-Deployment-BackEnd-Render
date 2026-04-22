package com.exchkr.club.management.services.Impl;

import com.exchkr.club.management.dao.UserClubMembershipProjection;
import com.exchkr.club.management.dao.UserRepository;
import com.exchkr.club.management.model.api.request.AuthRequest;
import com.exchkr.club.management.model.entity.User;
import com.exchkr.club.management.model.api.response.LoginResponse;
import com.exchkr.club.management.model.dto.UserDTO;
import com.exchkr.club.management.security.JwtUtil;
import com.exchkr.club.management.services.AuthenticationService;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public AuthenticationServiceImpl(UserRepository userRepository,
                                     JwtUtil jwtUtil,
                                     PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public LoginResponse login(AuthRequest request, HttpServletResponse response) {

        User user = userRepository.findUserByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        List<UserClubMembershipProjection> memberships =
                userRepository.findAllMembershipsByUserId(user.getUserId());

        if (memberships.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not associated with any active clubs.");
        }

        return new LoginResponse("Please select a club", memberships, user.getUserId());
    }

    @Override
    public LoginResponse selectClub(Long userId, Long clubId, HttpServletResponse response) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<UserClubMembershipProjection> memberships =
                userRepository.findAllMembershipsByUserId(userId);

        UserClubMembershipProjection selected = memberships.stream()
                .filter(m -> m.getClubId().equals(clubId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a member of this club"));

        List<String> roles = Collections.singletonList(selected.getRoleName());

        String accessToken = jwtUtil.generateAccessToken(
                user.getEmail(),
                user.getUserId(),
                clubId,
                roles
        );

        String refreshToken = jwtUtil.generateRefreshToken(
                user.getEmail(),
                user.getUserId()
        );

        UserDTO userDTO = UserDTO.fromUser(
                user,
                roles,
                selected.getClubName(),
                clubId,
                selected.getJoinedAt()
        );

        userDTO.setClubId(clubId);
        userDTO.setRoleId(selected.getRoleId());

        return new LoginResponse(
                "Login successful for " + selected.getClubName(),
                userDTO,
                accessToken,
                refreshToken
        );
    }

    @Override
    public LoginResponse refreshToken(String oldRefreshToken, HttpServletResponse response) {

        if (!jwtUtil.isTokenValid(oldRefreshToken)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid or expired refresh token.");
        }

        String userEmail = jwtUtil.extractUsername(oldRefreshToken);

        User user = userRepository.findUserByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<UserClubMembershipProjection> memberships =
                userRepository.findAllMembershipsByUserId(user.getUserId());

        if (memberships.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No active memberships found.");
        }

        UserClubMembershipProjection defaultMembership = memberships.get(0);
        List<String> roles = Collections.singletonList(defaultMembership.getRoleName());

        String newAccessToken = jwtUtil.generateAccessToken(
                user.getEmail(),
                user.getUserId(),
                defaultMembership.getClubId(),
                roles
        );

        String newRefreshToken = jwtUtil.generateRefreshToken(
                user.getEmail(),
                user.getUserId()
        );

        UserDTO userDTO = UserDTO.fromUser(
                user,
                roles,
                defaultMembership.getClubName(),
                defaultMembership.getClubId(),
                defaultMembership.getJoinedAt()
        );

        return new LoginResponse(
                "Token refreshed successfully",
                userDTO,
                newAccessToken,
                newRefreshToken
        );
    }
}