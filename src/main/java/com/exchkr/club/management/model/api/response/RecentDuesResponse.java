package com.exchkr.club.management.model.api.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public class RecentDuesResponse {

    private Long dueId;
    private String description;
    private BigDecimal totalAmount;
    private LocalDate dueDate;

    public RecentDuesResponse() {}

    public RecentDuesResponse(Long dueId, String description, BigDecimal totalAmount, LocalDate dueDate) {
        this.dueId = dueId;
        this.description = description;
        this.totalAmount = totalAmount;
        this.dueDate = dueDate;
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

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
}
