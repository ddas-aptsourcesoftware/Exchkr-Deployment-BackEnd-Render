package com.exchkr.club.management.dao;

import com.exchkr.club.management.model.entity.PlaidAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlaidAccountRepository extends JpaRepository<PlaidAccount, UUID> {
    
    @Query("SELECT COUNT(pa) > 0 FROM PlaidAccount pa WHERE pa.clubId = :clubId AND pa.activeInd = true")
    boolean existsByClubIdAndActiveIndTrue(@Param("clubId") Long clubId);

    // Used for standard operations where only a healthy connection matters
    Optional<PlaidAccount> findByClubIdAndActiveIndTrue(Long clubId);
    
    // "Update Mode" logic to find an account regardless of status
    Optional<PlaidAccount> findByClubId(Long clubId);
    
    // Used by Webhook Controller to find which account to deactivate/update
    Optional<PlaidAccount> findByItemId(String itemId);
}