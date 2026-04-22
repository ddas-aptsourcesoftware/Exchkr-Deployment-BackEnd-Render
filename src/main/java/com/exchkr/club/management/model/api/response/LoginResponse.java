package com.exchkr.club.management.model.api.response;

import com.exchkr.club.management.model.dto.UserDTO;
import com.exchkr.club.management.dao.UserClubMembershipProjection;
import java.util.List;

public class LoginResponse {
    private String message;
    private UserDTO user;
    private List<UserClubMembershipProjection> availableClubs; // For Phase 1 of login
    private Long userId; // To pass back to the frontend for Phase 2 of login

    private String accessToken;
    private String refreshToken;


    // Constructor for Phase 1 (Credentials OK, pick a club)
    public LoginResponse(String message, List<UserClubMembershipProjection> clubs, Long userId) {
        this.message = message;
        this.availableClubs = clubs;
        this.userId = userId;
    }

    // Constructor for Phase 2 (Club selected, login complete)
    public LoginResponse(String message, UserDTO user, String accessToken, String refreshToken) {
        this.message = message;
        this.user = user;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public LoginResponse() {}

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public UserDTO getUser() { return user; }
    public void setUser(UserDTO user) { this.user = user; }
    public List<UserClubMembershipProjection> getAvailableClubs() { return availableClubs; }
    public void setAvailableClubs(List<UserClubMembershipProjection> availableClubs) { this.availableClubs = availableClubs; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}