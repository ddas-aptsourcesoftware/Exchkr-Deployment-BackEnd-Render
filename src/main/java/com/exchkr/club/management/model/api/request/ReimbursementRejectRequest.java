package com.exchkr.club.management.model.api.request;

import java.math.BigDecimal;

public class ReimbursementRejectRequest {

    private Long reimbursementId;
    private String rejectReason;
    private String stripeRefId;



    public Long getReimbursementId() {
        return reimbursementId;
    }

    public void setReimbursementId(Long reimbursementId) {
        this.reimbursementId = reimbursementId;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public String getStripeRefId() {
        return stripeRefId;
    }

    public void setStripeRefId(String stripeRefId) {
        this.stripeRefId = stripeRefId;
    }
}
