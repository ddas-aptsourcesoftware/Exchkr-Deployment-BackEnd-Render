package com.exchkr.club.management.model.entity;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "ecm_user_clubs_mappings")
@IdClass(UserClubId.class) 
public class UserClub {

    @Id
    private Long userId;

    @Id
    private Long clubId;

    private Long roleId;
    
    private String status;

    private Instant joinedAt;

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getClubId() {
		return clubId;
	}

	public void setClubId(Long clubId) {
		this.clubId = clubId;
	}

	public Long getRoleId() {
		return roleId;
	}

	public void setRoleId(Long roleId) {
		this.roleId = roleId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Instant getJoinedAt() {
	    return joinedAt;
	}

	public void setJoinedAt(Instant joinedAt) {
	    this.joinedAt = joinedAt;
	}

}