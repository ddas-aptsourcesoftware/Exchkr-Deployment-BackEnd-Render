package com.exchkr.club.management.model.api.response;

import com.exchkr.club.management.model.dto.UserDTO;
import com.exchkr.club.management.dao.UserClubMembershipProjection;
import java.util.List;

public class LoginResponse {
    private String message;
    private UserDTO user; 
    private List<UserClubMembershipProjection> availableClubs; // For Phase 1 of login
    private Long userId; // To pass back to the frontend for Phase 2 of login

    // Constructor for Phase 1 (Credentials OK, pick a club)
    public LoginResponse(String message, List<UserClubMembershipProjection> clubs, Long userId) {
        this.message = message;
        this.availableClubs = clubs;
        this.userId = userId;
    }

    // Constructor for Phase 2 (Club selected, login complete)
    public LoginResponse(String message, UserDTO user) {
        this.message = message;
        this.user = user;
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
}