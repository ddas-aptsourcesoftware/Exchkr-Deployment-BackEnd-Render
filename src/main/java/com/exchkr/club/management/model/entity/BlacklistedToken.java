package com.exchkr.club.management.model.entity;

import java.time.Instant;
import jakarta.persistence.*;


@Entity
@Table(name = "ecm_token_blacklist")
public class BlacklistedToken {
    @Id
    private String jti;

    @Column(nullable = false)
    private Instant expiryDate;
    
    public BlacklistedToken() {}

    public BlacklistedToken(String jti, Instant expiryDate) {
        this.jti = jti;
        this.expiryDate = expiryDate;
    }

	public String getJti() {
		return jti;
	}

	public void setJti(String jti) {
		this.jti = jti;
	}

	public Instant getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(Instant expiryDate) {
		this.expiryDate = expiryDate;
	}

    
}