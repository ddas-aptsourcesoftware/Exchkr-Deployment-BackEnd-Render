package com.exchkr.club.management.model.api.request;

import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;


public class MemberReimbursementRequest {


    private BigDecimal amount;
    private String category;
    private String description;

    private String purchaseDate;

    private MultipartFile receiptImageFile;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
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

    public String getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(String purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public MultipartFile getReceiptImageFile() {
        return receiptImageFile;
    }

    public void setReceiptImageFile(MultipartFile receiptImageFile) {
        this.receiptImageFile = receiptImageFile;
    }
}
