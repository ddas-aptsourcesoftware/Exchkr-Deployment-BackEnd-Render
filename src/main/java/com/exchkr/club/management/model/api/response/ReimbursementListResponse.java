package com.exchkr.club.management.model.api.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ReimbursementListResponse {

    private Long reimbursementId;
    private Long submittedByMemberId;
    private String memberName;
    private String memberEmail;
    private String receiptFileName;
    private String receiptFileSystemName;
    private BigDecimal amountUsd;
    private LocalDate purchaseDate;
    private String category;
    private String description;

    public ReimbursementListResponse(
            Long reimbursementId,
            Long submittedByMemberId,
            String memberName,
            String memberEmail,
            String receiptFileName,
            String receiptFileSystemName,
            BigDecimal amountUsd,
            LocalDate purchaseDate,
            String category,
            String description
    ) {
        this.reimbursementId = reimbursementId;
        this.submittedByMemberId = submittedByMemberId;
        this.memberName = memberName;
        this.memberEmail = memberEmail;
        this.receiptFileName = receiptFileName;
        this.receiptFileSystemName = receiptFileSystemName;
        this.amountUsd = amountUsd;
        this.purchaseDate = purchaseDate;
        this.category = category;
        this.description = description;
    }

    public Long getReimbursementId() {
        return reimbursementId;
    }

    public void setReimbursementId(Long reimbursementId) {
        this.reimbursementId = reimbursementId;
    }

    public Long getSubmittedByMemberId() {
        return submittedByMemberId;
    }

    public void setSubmittedByMemberId(Long submittedByMemberId) {
        this.submittedByMemberId = submittedByMemberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getMemberEmail() {
        return memberEmail;
    }

    public void setMemberEmail(String memberEmail) {
        this.memberEmail = memberEmail;
    }

    public String getReceiptFileName() {
        return receiptFileName;
    }

    public void setReceiptFileName(String receiptFileName) {
        this.receiptFileName = receiptFileName;
    }

    public String getReceiptFileSystemName() {
        return receiptFileSystemName;
    }

    public void setReceiptFileSystemName(String receiptFileSystemName) {
        this.receiptFileSystemName = receiptFileSystemName;
    }

    public BigDecimal getAmountUsd() {
        return amountUsd;
    }

    public void setAmountUsd(BigDecimal amountUsd) {
        this.amountUsd = amountUsd;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
