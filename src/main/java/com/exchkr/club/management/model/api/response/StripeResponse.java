package com.exchkr.club.management.model.api.response;


public class StripeResponse {

    // For onboarding
    private String onboardingUrl;

    // For PaymentIntent
    private String clientSecret;
    private String paymentIntentId;
    private String status;
    private Long amount;
    private String currency;
    private Double balanceDifference;

    // For Connected Account Balance (USD)
    private Double stripeBalance;
    private String message;


    public StripeResponse() {}

    // Constructor for onboarding
    public StripeResponse(String onboardingUrl) {
        this.onboardingUrl = onboardingUrl;
    }

    // Constructor for PaymentIntent
    public StripeResponse(String clientSecret, String paymentIntentId, String status, Long amount, String currency) {
        this.clientSecret = clientSecret;
        this.paymentIntentId = paymentIntentId;
        this.status = status;
        this.amount = amount;
        this.currency = currency;
    }

    // Existing getters & setters...
    public String getOnboardingUrl() {
        return onboardingUrl;
    }

    public void setOnboardingUrl(String onboardingUrl) {
        this.onboardingUrl = onboardingUrl;
    }

    // New getters & setters for PaymentIntent
    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getPaymentIntentId() {
        return paymentIntentId;
    }

    public void setPaymentIntentId(String paymentIntentId) {
        this.paymentIntentId = paymentIntentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Double getStripeBalance() {
        return stripeBalance;
    }

    public void setStripeBalance(Double stripeBalance) {
        this.stripeBalance = stripeBalance;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Double getBalanceDifference() {
        return balanceDifference;
    }

    public void setBalanceDifference(Double balanceDifference) {
        this.balanceDifference = balanceDifference;
    }
}
