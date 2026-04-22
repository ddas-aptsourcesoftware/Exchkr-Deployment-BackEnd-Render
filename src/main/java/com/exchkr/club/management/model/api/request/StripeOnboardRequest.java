package com.exchkr.club.management.model.api.request;

public class StripeOnboardRequest {

    private Long userId;
    private Long clubId;

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
}
