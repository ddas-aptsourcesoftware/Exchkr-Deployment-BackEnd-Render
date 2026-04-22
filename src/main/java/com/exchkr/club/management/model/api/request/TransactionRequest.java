package com.exchkr.club.management.model.api.request;

import java.math.BigDecimal;

public class TransactionRequest {
	private BigDecimal amount;
	private String description;
	private String category;
	private String stripePaymentIntentId;
	private String type;
	private String paymentStatus;
	private Long recipientId;
	private Long dueId;
    private BigDecimal platformFee;
    private BigDecimal stripeFee;

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getStripePaymentIntentId() {
		return stripePaymentIntentId;
	}

	public void setStripePaymentIntentId(String stripePaymentIntentId) {
		this.stripePaymentIntentId = stripePaymentIntentId;
	}

	public Long getRecipientId() {
		return recipientId;
	}

	public void setRecipientId(Long recipientId) {
		this.recipientId = recipientId;
	}

	public Long getDueId() {
		return dueId;
	}

	public void setDueId(Long dueId) {
		this.dueId = dueId;
	}

	public String getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(String paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

    public BigDecimal getPlatformFee() {
        return platformFee;
    }

    public void setPlatformFee(BigDecimal platformFee) {
        this.platformFee = platformFee;
    }

    public BigDecimal getStripeFee() {
        return stripeFee;
    }

    public void setStripeFee(BigDecimal stripeFee) {
        this.stripeFee = stripeFee;
    }
}