package com.exchkr.club.management.dao;

import com.exchkr.club.management.model.entity.Stripe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


@Repository
public interface StripeAccountWebhookRepository extends JpaRepository<Stripe, String> {

    @Query(value = """
        SELECT 'CLUB' AS account_type
        FROM ecm_club_stripe_account
        WHERE stripe_account_id = :stripeAccountId
        UNION ALL
        SELECT 'MEMBER' AS account_type
        FROM ecm_member_stripe_account
        WHERE stripe_account_id = :stripeAccountId
        LIMIT 1
        """, nativeQuery = true)
    String findAccountTypeByStripeAccountId(@Param("stripeAccountId") String stripeAccountId);

    @Modifying
    @Transactional
    @Query(value = """
        UPDATE ecm_club_stripe_account
        SET stripe_account_status = :status,
            updated_on = now()
        WHERE stripe_account_id = :stripeAccountId
        """, nativeQuery = true)
    int updateClubAccountStatus(
            @Param("stripeAccountId") String stripeAccountId,
            @Param("status") String status
    );

    @Modifying
    @Transactional
    @Query(value = """
        UPDATE ecm_member_stripe_account
        SET stripe_account_status = :status,
            updated_on = now()
        WHERE stripe_account_id = :stripeAccountId
        """, nativeQuery = true)
    int updateMemberAccountStatus(
            @Param("stripeAccountId") String stripeAccountId,
            @Param("status") String status
    );

}
