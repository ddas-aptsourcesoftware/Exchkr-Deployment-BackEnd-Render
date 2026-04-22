package com.exchkr.club.management.model.api.response;

public class StripeAccountResponse {

    private String stripeAccountStatus;
    private Boolean payoutsEnabled;
    private Boolean chargesEnabled;

    // No-arg constructor
    public StripeAccountResponse() {
    }

    // Parameterized constructor
    public StripeAccountResponse(String stripeAccountStatus, Boolean payoutsEnabled, Boolean chargesEnabled) {
        this.stripeAccountStatus = stripeAccountStatus;
        this.payoutsEnabled = payoutsEnabled;
        this.chargesEnabled = chargesEnabled;
    }

    public String getStripeAccountStatus() {
        return stripeAccountStatus;
    }

    public void setStripeAccountStatus(String stripeAccountStatus) {
        this.stripeAccountStatus = stripeAccountStatus;
    }

    public Boolean getPayoutsEnabled() {
        return payoutsEnabled;
    }

    public void setPayoutsEnabled(Boolean payoutsEnabled) {
        this.payoutsEnabled = payoutsEnabled;
    }

    public Boolean getChargesEnabled() {
        return chargesEnabled;
    }

    public void setChargesEnabled(Boolean chargesEnabled) {
        this.chargesEnabled = chargesEnabled;
    }
}
