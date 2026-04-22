package com.exchkr.club.management.model.api.response;

import java.math.BigDecimal;

public class MemberDuesResponse {
    private Long dueId;
    private String description;
    private BigDecimal totalAmount;

    public MemberDuesResponse() {}

    public MemberDuesResponse(Long dueId, String description, BigDecimal totalAmount) {
        this.dueId = dueId;
        this.description = description;
        this.totalAmount = totalAmount;
    }

    public Long getDueId() {
        return dueId;
    }

    public void setDueId(Long dueId) {
        this.dueId = dueId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}
