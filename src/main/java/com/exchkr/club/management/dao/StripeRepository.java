package com.exchkr.club.management.dao;

import com.exchkr.club.management.model.api.response.StripeAccountResponse;
import com.exchkr.club.management.model.entity.Stripe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface StripeRepository extends JpaRepository<Stripe, String> {

    @Query(
            value = "SELECT * FROM ecm_club_stripe_account " +
                    "WHERE club_id = :clubId AND stripe_account_id IS NOT NULL " +
                    "LIMIT 1",
            nativeQuery = true
    )
    Optional<Stripe> findStripeAccountByClubId(@Param("clubId") Long clubId);


    @Modifying
    @Transactional
    @Query(
            value = """
        INSERT INTO ecm_club_stripe_account
        (club_id, stripe_account_id, stripe_account_status, payouts_enabled, charges_enabled, created_on, updated_on)
        VALUES
        (:clubId, :stripeAccountId, 'Pending', false, false, now(), now())
        """,
            nativeQuery = true
    )
    void createAccount(
            @Param("clubId") Long clubId,
            @Param("stripeAccountId") String stripeAccountId
    );

    @Query(
            value = """
        SELECT stripe_account_id
        FROM ecm_club_stripe_account
        WHERE club_id = :clubId
          AND stripe_account_id IS NOT NULL
          AND stripe_account_status = 'Enabled'
        LIMIT 1
    """,
            nativeQuery = true
    )
    Optional<String> getClubStripeAccountId(@Param("clubId") Long clubId);

    @Query(
            value = """
        SELECT stripe_account_id
        FROM ecm_member_stripe_account
        WHERE user_id = :userId
          AND stripe_account_id IS NOT NULL
          AND stripe_account_status = 'Enabled'
        LIMIT 1
    """,
            nativeQuery = true
    )
    Optional<String> getMemberStripeAccountId(@Param("userId") Long userId);

    @Query(
            value = """
            SELECT stripe_account_id
            FROM ecm_member_stripe_account
            WHERE user_id = :userId
              AND stripe_account_id IS NOT NULL
            LIMIT 1
        """,
            nativeQuery = true
    )
    Optional<String> findStripeAccountIdByUserId(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query(
            value = """
        INSERT INTO ecm_member_stripe_account
        (user_id, stripe_account_id, stripe_account_status, payouts_enabled, charges_enabled, created_on, updated_on)
        VALUES
        (:userId, :stripeAccountId, 'Pending', false, false, now(), now())
        """,
            nativeQuery = true
    )
    void createMemberAccount(
            @Param("userId") Long userId,
            @Param("stripeAccountId") String stripeAccountId
    );

    @Query(
            value = """
        SELECT 
            stripe_account_status,
            payouts_enabled,
            charges_enabled
        FROM ecm_club_stripe_account
        WHERE club_id = :clubId
          AND stripe_account_id IS NOT NULL
        """,
            nativeQuery = true
    )
    List<Object[]> getClubStripeInfo(@Param("clubId") Long clubId);


    @Query(
            value = """
        SELECT 
            stripe_account_status,
            payouts_enabled,
            charges_enabled
        FROM ecm_member_stripe_account
        WHERE user_id = :userId
          AND stripe_account_id IS NOT NULL
        """,
            nativeQuery = true
    )
    List<Object[]> getMemberStripeInfo(@Param("userId") Long userId);

}
