package com.exchkr.club.management.model.dto;

import java.math.BigDecimal;
import java.time.Instant;

public class MemberDuesDTO {
    private String fullName;
    private String email; 
    private String status; 
    private BigDecimal amountPaid;
    private BigDecimal amountOwed; 
    private Instant lastPaymentDate;
	private Long dueId;
	private Long memberId;

    // 1. IMPORTANT: Constructor for JPQL Projection
    // The order of parameters here MUST match the SELECT order in MemberDuesRepository
    public MemberDuesDTO(String fullName, String email, String status, 
                         BigDecimal amountPaid, BigDecimal amountOwed, Instant lastPaymentDate, Long dueId, Long memberId) {
        this.fullName = fullName;
        this.email = email;
        this.status = status;
        this.amountPaid = amountPaid;
        this.amountOwed = amountOwed;
        this.lastPaymentDate = lastPaymentDate;
        this.dueId = dueId;
        this.memberId = memberId;}

    // 2. Default constructor (Good practice for JSON serialization)
    public MemberDuesDTO() {}

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }
    
    public BigDecimal getAmountOwed() { return amountOwed; }
    public void setAmountOwed(BigDecimal amountOwed) { this.amountOwed = amountOwed; }
    
    public Instant getLastPaymentDate() { return lastPaymentDate; }
    public void setLastPaymentDate(Instant lastPaymentDate) { this.lastPaymentDate = lastPaymentDate; }
    
    public Long getDueId() {
		return dueId;
	}

	public void setDueId(Long dueId) {
		this.dueId = dueId;
	}

	public Long getMemberId() {
		return memberId;
	}

	public void setMemberId(Long memberId) {
		this.memberId = memberId;
	}
}