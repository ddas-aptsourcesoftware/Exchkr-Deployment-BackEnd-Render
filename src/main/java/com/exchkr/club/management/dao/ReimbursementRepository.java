package com.exchkr.club.management.dao;

import com.exchkr.club.management.model.api.response.ReimbursementListResponse;
import com.exchkr.club.management.model.entity.Reimbursement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface ReimbursementRepository extends JpaRepository<Reimbursement, Long> {

	@Modifying
	@Transactional
	@Query(value = """
			INSERT INTO ecm_reimbursement_requests
			(club_id, submitted_by_member_id, receipt_file_name, receipt_file_system_name,
			 amount_usd, purchase_date, category, description, status, submitted_at)
			VALUES (:clubId, :userId, :fileName, :systemFileName,
			        :amount, :purchaseDate, :category, :description, 'PENDING', now())
			""", nativeQuery = true)
	void memberReimbursementRequest(Long clubId, Long userId, String fileName, String systemFileName, BigDecimal amount,
			LocalDate purchaseDate, String category, String description);

	@Query(value = """
			SELECT
			    r.reimbursement_id AS reimbursementId,
			    r.submitted_by_member_id AS submittedByMemberId,
			    u.full_name AS memberName,
			    u.user_name AS memberEmail,
			    r.receipt_file_name AS receiptFileName,
			    r.receipt_file_system_name AS receiptFileSystemName,
			    r.amount_usd AS amountUsd,
			    r.purchase_date AS purchaseDate,
			    r.category,
			    r.description
			FROM ecm_reimbursement_requests r
			JOIN ecm_users u
			  ON u.user_id = r.submitted_by_member_id
			WHERE r.club_id = :clubId
			  AND r.status = 'PENDING'
			ORDER BY r.submitted_at ASC
			""", nativeQuery = true)
	List<ReimbursementListResponse> reimbursementRequestList(@Param("clubId") Long clubId);

	@Modifying
	@Transactional
	@Query(value = """
			UPDATE ecm_reimbursement_requests
			SET
			    status = 'REJECTED',
			    rejected_by_officer_id = :userId,
			    rejected_at = now(),
			    reject_reason = :rejectReason
			WHERE
			    reimbursement_id = :reimbursementId
			    AND club_id = :clubId
			    AND status = 'PENDING'
			""", nativeQuery = true)
	int reimbursementRequestReject(@Param("clubId") Long clubId, @Param("userId") Long userId,
			@Param("reimbursementId") Long reimbursementId, @Param("rejectReason") String rejectReason);

	@Query(value = """
			SELECT
			    receipt_file_system_name,
			    receipt_file_name
			FROM public.ecm_reimbursement_requests
			WHERE reimbursement_id = :reimbursementId
			  AND club_id = :clubId
			""", nativeQuery = true)
	List<Object[]> reimbursementReceiptDownload(@Param("clubId") Long clubId,
			@Param("reimbursementId") Long reimbursementId);

	@Modifying
	@Transactional
	@Query(value = """
			UPDATE ecm_reimbursement_requests
			SET
			    status = 'APPROVED',
			    approved_by_officer_id = :userId,
			    approved_at = now(),
			    stripe_ref_id = :stripeRefId
			WHERE
			    reimbursement_id = :reimbursementId
			    AND club_id = :clubId
			    AND status = 'PENDING'
			""", nativeQuery = true)
	int reimbursementRequestApprove(@Param("clubId") Long clubId, @Param("userId") Long userId,
			@Param("reimbursementId") Long reimbursementId, @Param("stripeRefId") String stripeRefId);

	@Query(value = "SELECT COUNT(*) FROM ecm_reimbursement_requests WHERE club_id = :clubId AND status = 'PENDING'", nativeQuery = true)
	long countPendingReimbursements(@Param("clubId") Long clubId);

	@Query("SELECT COUNT(r) FROM Reimbursement r WHERE r.clubId = :clubId AND r.status = 'Pending' AND r.submittedAt BETWEEN :start AND :end")
	long countPendingReimbursementsByDate(@Param("clubId") Long clubId, @Param("start") java.time.OffsetDateTime start,
			@Param("end") java.time.OffsetDateTime end);

	@Query(value = "SELECT r.amount_usd AS amount, " + "r.category AS reimbursementCategory, "
			+ "r.reject_reason AS rejectReason, " + "u.user_name AS toEmail, " + "u.full_name AS memberName "
			+ "FROM ecm_reimbursement_requests r " + "JOIN ecm_users u ON u.user_id = r.submitted_by_member_id "
			+ "WHERE r.reimbursement_id = :reimbursementId " + "AND r.status = 'REJECTED'", nativeQuery = true)
	Map<String, Object> getRejectedReimbursementInfo(@Param("reimbursementId") Long reimbursementId);

	@Query(value = "SELECT r.amount_usd AS amount, " + "r.category AS reimbursementCategory, "
			+ "u.user_name AS toEmail, " + "u.full_name AS memberName " + "FROM ecm_reimbursement_requests r "
			+ "JOIN ecm_users u ON u.user_id = r.submitted_by_member_id "
			+ "WHERE r.reimbursement_id = :reimbursementId " + "AND r.status = 'APPROVED'", nativeQuery = true)
	Map<String, Object> getApprovedReimbursementInfo(@Param("reimbursementId") Long reimbursementId);

	@Query("SELECT r FROM Reimbursement r WHERE r.stripeRefId = :stripeRefId")
	Optional<Reimbursement> findByStripeRefId(@Param("stripeRefId") String stripeRefId);

	@Modifying
	@Transactional
	@Query(value = """
			UPDATE ecm_reimbursement_requests
			SET status = :status
			WHERE stripe_ref_id = :stripeRefId
			""", nativeQuery = true)
	void updateStatusByStripeRef(@Param("stripeRefId") String stripeRefId, @Param("status") String status);

	@Query(value = "SELECT category_id AS categoryId, category_name AS categoryName "
			+ "FROM ecm_budget_category_master " + "WHERE club_id = :clubId", nativeQuery = true)
	List<Object[]> getClubBudgetCategories(@Param("clubId") Long clubId);


}
