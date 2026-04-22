package com.exchkr.club.management.model.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import com.exchkr.club.management.model.entity.User;

public class UserDTO {
    private Long userId;
    private Long clubId;
    private Long roleId;
    private String email;
    private String firstName;
    private String lastName;
    private String userName;
    private String clubName;
    private LocalDate clubJoinedOn; 
    private List<String> roles;
    private String status; 

    /**
     * Maps from the JPA User entity and the membership joinedAt timestamp.
     */
    public static UserDTO fromUser(User user, List<String> roles, String clubName, Long clubId, Instant joinedAt) {
        UserDTO dto = new UserDTO();
        dto.userId = user.getUserId();
        dto.email = user.getEmail();
        dto.firstName = user.getFirstName();
        dto.lastName = user.getLastName();
        dto.userName = user.getUserName();
        dto.clubName = clubName;
        dto.status = user.getStatus(); 
        dto.clubId = clubId;
        dto.roles = roles;

        if (joinedAt != null) {
            dto.clubJoinedOn = LocalDate.ofInstant(joinedAt, ZoneId.systemDefault());
        }
        
        return dto;
    }

   
    public Long getUserId() { return userId; }
    public Long getClubId() { return clubId; }
    public Long getRoleId() { return roleId; }
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getUserName() { return userName; }
    public String getClubName() { return clubName; }
    public LocalDate getClubJoinedOn() { return clubJoinedOn; }
    public List<String> getRoles() { return roles; }
    public String getStatus() { return status; }


    public void setUserId(Long userId) { this.userId = userId; }
    public void setClubId(Long clubId) { this.clubId = clubId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
    public void setEmail(String email) { this.email = email; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setClubName(String clubName) { this.clubName = clubName; }
    public void setClubJoinedOn(LocalDate clubJoinedOn) { this.clubJoinedOn = clubJoinedOn; }
    public void setRoles(List<String> roles) { this.roles = roles; }
    public void setStatus(String status) { this.status = status; }
}