package com.exchkr.club.management.dao;

import java.time.Instant;

import com.exchkr.club.management.model.entity.User;

public interface UserClubMembershipProjection {
    Long getClubId();
    Long getRoleId();
    
 
    String getClubName();
    String getRoleName();
    
    User getUser();
    Instant getJoinedAt();
}