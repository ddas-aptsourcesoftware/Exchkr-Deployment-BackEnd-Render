package com.exchkr.club.management.model.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import com.exchkr.club.management.config.StringStatusToSmallIntConverter;

@Entity
@Table(name = "ecm_users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "user_pwd", nullable = false)
    private String password;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "created_on", updatable = false)
    private Instant createdOn;

    @Column(name = "last_updated_on")
    private Instant lastUpdatedOn;

    @Transient
    private List<String> roles;
    
    @Column(name = "active_ind") 
    @Convert(converter = StringStatusToSmallIntConverter.class) 
    private String status;
    
    @Column(name = "plaid_access_token")
    private String plaidAccessToken;

    public User() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFullName() { return fullName; }
    public void setFullName(String newFullName) { this.fullName = newFullName; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public Instant getCreatedOn() { return createdOn; }
    public void setCreatedOn(Instant createdOn) { this.createdOn = createdOn; }
    public Instant getLastUpdatedOn() { return lastUpdatedOn; }
    public void setLastUpdatedOn(Instant lastUpdatedOn) { this.lastUpdatedOn = lastUpdatedOn; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPlaidAccessToken() { return plaidAccessToken; }
    public void setPlaidAccessToken(String plaidAccessToken) { this.plaidAccessToken = plaidAccessToken; }
}