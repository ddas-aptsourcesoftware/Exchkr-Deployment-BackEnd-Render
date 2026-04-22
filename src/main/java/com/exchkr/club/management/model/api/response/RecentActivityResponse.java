package com.exchkr.club.management.model.api.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;


public class RecentActivityResponse {

    private Long transId;
    private OffsetDateTime transDate;
    private String description;
    private String category;
    private String type;
    private BigDecimal amount;
    private String status;
    private Long doneByUserId;
    private String doneByUserName;
    private Long paidToUserId;
    private String paidToUserName;

    public Long getTransId() {
        return transId;
    }

    public void setTransId(Long transId) {
        this.transId = transId;
    }

    public OffsetDateTime getTransDate() {
        return transDate;
    }

    public void setTransDate(OffsetDateTime transDate) {
        this.transDate = transDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getDoneByUserId() {
        return doneByUserId;
    }

    public void setDoneByUserId(Long doneByUserId) {
        this.doneByUserId = doneByUserId;
    }

    public String getDoneByUserName() {
        return doneByUserName;
    }

    public void setDoneByUserName(String doneByUserName) {
        this.doneByUserName = doneByUserName;
    }

    public Long getPaidToUserId() {
        return paidToUserId;
    }

    public void setPaidToUserId(Long paidToUserId) {
        this.paidToUserId = paidToUserId;
    }

    public String getPaidToUserName() {
        return paidToUserName;
    }

    public void setPaidToUserName(String paidToUserName) {
        this.paidToUserName = paidToUserName;
    }
}
