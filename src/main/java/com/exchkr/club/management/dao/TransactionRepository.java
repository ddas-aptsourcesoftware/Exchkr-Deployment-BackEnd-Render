package com.exchkr.club.management.dao;

import com.exchkr.club.management.model.entity.ClubTransaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<ClubTransaction, Long> {

	List<ClubTransaction> findAllByClubIdOrderByTransDateDesc(Long clubId);

	@Query(value = """
			SELECT
			    t.trans_id as "transId",
			    t.club_id as "clubId",
			    t.amount as "amount",
			    t.type as "type",
			    t.category as "category",
			    t.description as "description",
			    t.trans_date as "transDate",
			    t.status as "status",
			    t.done_by_user_id as "doneByUserId",
			    t.paid_to_user_id as "paidToUserId",
			    t.stripe_ref_id as "stripeRefId",
			    t.due_id as "dueId"
			FROM ecm_clubs_transactions t
			WHERE t.club_id = :clubId
			ORDER BY t.trans_date DESC
			LIMIT :limit OFFSET :offset
			""", nativeQuery = true)
	List<Map<String, Object>> fetchTransactionsRaw(@Param("clubId") Long clubId, @Param("limit") int limit,
			@Param("offset") long offset);

	@Query(value = "SELECT count(*) FROM ecm_clubs_transactions WHERE club_id = :clubId", nativeQuery = true)
	long countTransactionsByClub(@Param("clubId") Long clubId);

	@Query(value = """
			SELECT * FROM ecm_clubs_transactions
			WHERE club_id = :clubId
			  AND trans_date >= :fromDate
			  AND trans_date < :toDatePlusOne
			ORDER BY trans_date DESC
			""", nativeQuery = true)
	List<ClubTransaction> findTransactionsByDateRangeInclusive(@Param("clubId") Long clubId,
			@Param("fromDate") Instant fromDate, @Param("toDatePlusOne") Instant toDatePlusOne);

	Optional<ClubTransaction> findByStripeRefId(String stripeRefId);

	@Modifying
	@Query("UPDATE ClubTransaction t SET t.status = :status WHERE t.stripeRefId = :stripeRefId")
	int updateStatusByStripeRefId(String stripeRefId, String status);

	Optional<ClubTransaction> findByDueIdAndStripeRefId(Long dueId, String stripeRefId);

	@Modifying
	@Transactional
	@Query(value = """
			INSERT INTO ecm_clubs_transactions
			(club_id, done_by_user_id, trans_date, description, category, type,
			 amount, status, stripe_ref_id, paid_to_user_id, due_id,
			 platform_fees, payment_gateway_service_charge)
			VALUES
			(:clubId, :userId, NOW(), :description, :category, 'Income',
			 :amount, :paymentStatus, :stripePaymentIntentId, NULL, NULL,
			 :platformFee, :stripeFee)
			""", nativeQuery = true)
	int saveDonationTransaction(@Param("userId") Long userId, @Param("clubId") Long clubId,
			@Param("description") String description, @Param("category") String category,
			@Param("amount") BigDecimal amount, @Param("stripePaymentIntentId") String stripePaymentIntentId,
			@Param("paymentStatus") String paymentStatus, @Param("platformFee") BigDecimal platformFee,
			@Param("stripeFee") BigDecimal stripeFee);

	@Query(value = "SELECT trans_id, category, amount, status, trans_date, description, due_id "
			+ "FROM ecm_clubs_transactions " + "WHERE club_id = :clubId "
			+ "AND (done_by_user_id = :userId OR paid_to_user_id = :userId) " + "ORDER BY trans_date DESC "
			+ "LIMIT 8", nativeQuery = true)
	List<Object[]> getMemberTransactions(@Param("userId") Long userId, @Param("clubId") Long clubId);

	@Query(value = """
			SELECT CAST(COALESCE(SUM(amount), 0) AS NUMERIC)
			FROM ecm_clubs_transactions
			WHERE club_id = :clubId AND status = 'Completed' AND type = :type
			""", nativeQuery = true)
	BigDecimal sumAmountByClubAndType(@Param("clubId") Long clubId, @Param("type") String type);

	@Query("SELECT SUM(t.amount) FROM ClubTransaction t " + "WHERE t.clubId = :clubId " + "AND t.type = :type "
			+ "AND t.status = 'Completed' " + "AND t.transDate BETWEEN :start AND :end")
	BigDecimal sumAmountByClubAndTypeAndDate(@Param("clubId") Long clubId, @Param("type") String type,
			@Param("start") Instant start, @Param("end") Instant end);

	@Query(value = """
			SELECT
			    (
			        SELECT COUNT(*)
			        FROM ecm_reimbursement_requests rr
			        WHERE rr.club_id = :clubId
			          AND rr.status = 'PENDING'
			    ) AS expenseApprovalCount,

			    (
			        SELECT COUNT(*)
			        FROM ecm_member_dues md
			        WHERE md.club_id = :clubId
			          AND md.status IN ('Unpaid', 'Failed')
			          AND (md.total_amount - COALESCE(md.paid_amount, 0)) > 0
			    ) AS duesReminderCount
			""", nativeQuery = true)
	List<Object[]> getPendingActions(@Param("clubId") Long clubId);

	@Query(value = """
			SELECT
			    t.trans_id,
			    t.trans_date,
			    t.description,
			    t.category,
			    t.type,
			    t.amount,
			    t.status,

			    t.done_by_user_id,

			    CASE
			        WHEN t.category = 'Public Donation'
			            THEN d.donator_name
			        ELSE du.full_name
			    END AS done_by_user_name,

			    CASE
			        WHEN t.type = 'Expense' THEN t.paid_to_user_id
			        ELSE NULL
			    END AS paid_to_user_id,

			    CASE
			        WHEN t.type = 'Expense' THEN pu.full_name
			        ELSE NULL
			    END AS paid_to_user_name

			FROM public.ecm_clubs_transactions t

			LEFT JOIN public.ecm_users du
			    ON du.user_id = t.done_by_user_id

			LEFT JOIN public.ecm_users pu
			    ON pu.user_id = t.paid_to_user_id
			   AND t.type = 'Expense'

			LEFT JOIN public.ecm_clubs_donations d
			    ON d.donation_id = t.public_donation_id
			   AND t.category = 'Public Donation'

			WHERE t.club_id = :clubId
			ORDER BY t.trans_date DESC
			LIMIT 6
			""", nativeQuery = true)
	List<Object[]> getRecentActivity(@Param("clubId") Long clubId);

	@Query(value = """
			SELECT
			    combined.category_name AS name,
			    SUM(combined.amount) AS value
			FROM (
			    SELECT t.category AS category_name, t.amount AS amount
			    FROM ecm_clubs_transactions t
			    WHERE t.club_id = :clubId
			      AND t.type = 'Expense'
			      AND t.status = 'Completed'
			      AND t.stripe_ref_id IS NULL

			    UNION ALL

			    SELECT r.category AS category_name, t.amount AS amount
			    FROM ecm_reimbursement_requests r
			    JOIN ecm_clubs_transactions t ON r.stripe_ref_id = t.stripe_ref_id
			    WHERE r.club_id = :clubId
			      AND t.status = 'Completed'
			      AND t.type = 'Expense'
			) combined
			GROUP BY combined.category_name
			""", nativeQuery = true)
	List<Map<String, Object>> getSpendingByCategoryRaw(@Param("clubId") Long clubId);

	@Query(value = """
			SELECT
			    TO_CHAR(trans_date, 'Mon') AS month,
			    SUM(amount) AS amount,
			    DATE_TRUNC('month', trans_date) AS month_start
			FROM ecm_clubs_transactions
			WHERE club_id = :clubId
			  AND status = 'Completed'
			  AND type = 'Expense'
			  AND trans_date >= CURRENT_DATE - INTERVAL '6 months'
			GROUP BY month, month_start
			ORDER BY month_start ASC
			""", nativeQuery = true)
	List<Map<String, Object>> getMonthlySpendingRaw(@Param("clubId") Long clubId);

	@Query("SELECT SUM(t.amount) FROM ClubTransaction t " + "WHERE t.clubId = :clubId " + "AND t.category = :category "
			+ "AND t.type = 'Expense' " + "AND t.status = 'Completed' " + "AND t.stripeRefId IS NULL")
	BigDecimal sumDirectExpensesOnly(@Param("clubId") Long clubId, @Param("category") String category);

	@Query(value = """
			SELECT CAST(COALESCE(SUM(t.amount), 0) AS NUMERIC)
			FROM ecm_clubs_transactions t
			JOIN ecm_reimbursement_requests r ON t.stripe_ref_id = r.stripe_ref_id
			WHERE r.club_id = :clubId
			  AND r.category = :category
			  AND t.status = 'Completed'
			""", nativeQuery = true)
	BigDecimal sumValidatedReimbursementExpenses(@Param("clubId") Long clubId, @Param("category") String category);

}