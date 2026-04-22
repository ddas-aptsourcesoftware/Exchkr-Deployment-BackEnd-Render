package com.exchkr.club.management.dao;

import com.exchkr.club.management.model.entity.Reimbursement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;


import java.math.BigDecimal;
import java.util.List;


@Repository
public interface ClubDonationRepository extends JpaRepository<Reimbursement, Long> {

    @Query(
            value = """
        SELECT DISTINCT school_name
        FROM public.ecm_clubs
        WHERE active_ind = 1
        ORDER BY school_name
    """,
            nativeQuery = true
    )
    List<String> getPlatformUniversities();

    @Query(
            value = """
        SELECT club_id, club_name
        FROM public.ecm_clubs
        WHERE active_ind = 1
          AND school_name = :universityName
        ORDER BY club_name
    """,
            nativeQuery = true
    )
    List<Object[]> getPlatformClubs(@Param("universityName") String universityName);


    @Query(
            value = """
    INSERT INTO public.ecm_clubs_donations
    (club_id, donator_name, donator_email, amount_usd, donation_date, stripe_ref_id, is_visible_to_club)
    VALUES
    (:clubId, :donatorName, :donatorEmail, :amount, now(), :stripeRefId, 1)
    RETURNING donation_id
    """,
            nativeQuery = true
    )
    Long saveDonation(
            @Param("clubId") Long clubId,
            @Param("donatorName") String donatorName,
            @Param("donatorEmail") String donatorEmail,
            @Param("amount") BigDecimal amount,
            @Param("stripeRefId") String stripeRefId
    );

    @Modifying
    @Query(
            value = """
        INSERT INTO public.ecm_clubs_transactions
        (club_id, trans_date, description, category, type, amount, status, stripe_ref_id, public_donation_id)
        VALUES
        (:clubId, now(), 'Club donation', 'Public Donation', 'Income',
         :amount, :paymentStatus, :stripeRefId, :donationId)
        """,
            nativeQuery = true
    )
    int saveDonationTransaction(
            @Param("clubId") Long clubId,
            @Param("amount") BigDecimal amount,
            @Param("paymentStatus") String paymentStatus,
            @Param("stripeRefId") String stripeRefId,
            @Param("donationId") Long donationId
    );


}
