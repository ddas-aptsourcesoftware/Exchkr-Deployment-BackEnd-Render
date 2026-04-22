package com.exchkr.club.management.dao;

import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import com.exchkr.club.management.model.entity.BlacklistedToken;

public interface TokenBlacklistRepository extends JpaRepository<BlacklistedToken, String> {
	
    @Modifying
    @Transactional
    void deleteAllByExpiryDateBefore(Instant now);
}