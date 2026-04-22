package com.exchkr.club.management.model.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "ecm_member_stripe_account")
public class StripeMember {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "stripe_account_id", nullable = false, length = 255)
    private String stripeAccountId;

    @Column(name = "stripe_bank_account_id", length = 255)
    private String stripeBankAccountId;

    @Column(name = "stripe_account_status", nullable = false, length = 20)
    private String stripeAccountStatus = "pending";

    @Column(name = "payouts_enabled")
    private Boolean payoutsEnabled = false;

    @Column(name = "charges_enabled")
    private Boolean chargesEnabled = false;

    @Column(name = "created_on", columnDefinition = "timestamptz", updatable = false)
    private OffsetDateTime createdOn;

    @Column(name = "updated_on", columnDefinition = "timestamptz")
    private OffsetDateTime updatedOn;


    @PrePersist
    public void prePersist() {
        this.createdOn = OffsetDateTime.now();
        this.updatedOn = OffsetDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedOn = OffsetDateTime.now();
    }


    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getStripeAccountId() {
        return stripeAccountId;
    }

    public void setStripeAccountId(String stripeAccountId) {
        this.stripeAccountId = stripeAccountId;
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

    public OffsetDateTime getUpdatedOn() {
        return updatedOn;
    }
}
