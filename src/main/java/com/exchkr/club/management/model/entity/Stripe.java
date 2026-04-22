package com.exchkr.club.management.model.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "ecm_club_stripe_account")
public class Stripe {

    @Id
    @Column(name = "stripe_account_id", nullable = false, length = 255)
    private String stripeAccountId;

    @Column(name = "club_id", nullable = false)
    private Long clubId;

    @Column(name = "stripe_bank_account_id", length = 255)
    private String stripeBankAccountId;

    @Column(name = "stripe_account_status", length = 20, nullable = false)
    private String stripeAccountStatus = "pending";

    @Column(name = "payouts_enabled")
    private Boolean payoutsEnabled = false;

    @Column(name = "charges_enabled")
    private Boolean chargesEnabled = false;

    @Column(name = "created_on", columnDefinition = "timestamp with time zone")
    private OffsetDateTime createdOn = OffsetDateTime.now();

    @Column(name = "updated_on", columnDefinition = "timestamp with time zone")
    private OffsetDateTime updatedOn = OffsetDateTime.now();

    @Column(name = "last_updated_by_user_id")
    private Long lastUpdatedByUserId;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    // Getters and Setters

    public String getStripeAccountId() {
        return stripeAccountId;
    }

    public void setStripeAccountId(String stripeAccountId) {
        this.stripeAccountId = stripeAccountId;
    }

    public Long getClubId() {
        return clubId;
    }

    public void setClubId(Long clubId) {
        this.clubId = clubId;
    }

    public String getStripeBankAccountId() {
        return stripeBankAccountId;
    }

    public void setStripeBankAccountId(String stripeBankAccountId) {
        this.stripeBankAccountId = stripeBankAccountId;
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

    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public OffsetDateTime getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(OffsetDateTime updatedOn) {
        this.updatedOn = updatedOn;
    }

    public Long getLastUpdatedByUserId() {
        return lastUpdatedByUserId;
    }

    public void setLastUpdatedByUserId(Long lastUpdatedByUserId) {
        this.lastUpdatedByUserId = lastUpdatedByUserId;
    }

    public Long getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(Long createdByUserId) {
        this.createdByUserId = createdByUserId;
    }
}
