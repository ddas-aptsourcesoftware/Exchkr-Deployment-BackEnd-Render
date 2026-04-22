package com.exchkr.club.management.model.entity;

import java.io.Serializable;
import java.util.Objects;

public class UserClubId implements Serializable {

    private Long userId;
    private Long clubId;

    public UserClubId() {}

    public UserClubId(Long userId, Long clubId) {
        this.userId = userId;
        this.clubId = clubId;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getClubId() { return clubId; }
    public void setClubId(Long clubId) { this.clubId = clubId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserClubId that = (UserClubId) o;
        return Objects.equals(userId, that.userId) && 
               Objects.equals(clubId, that.clubId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, clubId);
    }
}