package com.exchkr.club.management.services;

import com.exchkr.club.management.dao.TokenBlacklistRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.Instant;

@Service
public class TokenCleanupService {

    private final TokenBlacklistRepository repository;

    public TokenCleanupService(TokenBlacklistRepository repository) {
        this.repository = repository;
    }

    // Runs every hour to remove tokens that are already expired
    @Scheduled(cron = "0 0 * * * *")
    public void removeExpiredTokens() {
        repository.deleteAllByExpiryDateBefore(Instant.now());
    }
}