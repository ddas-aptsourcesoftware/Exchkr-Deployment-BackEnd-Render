package com.exchkr.club.management.model.api.response;

public class PendingActionsResponse {
    private String actionType;
    private Long pendingCount;

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public Long getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(Long pendingCount) {
        this.pendingCount = pendingCount;
    }
}
