package com.exchkr.club.management.dao;

import com.exchkr.club.management.model.dto.MemberDuesDTO;
import com.exchkr.club.management.model.entity.MemberDue;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface MemberDuesRepository extends JpaRepository<MemberDue, Long> {

	@Query("SELECT d FROM MemberDue d WHERE d.assignedUserId = :userId AND d.status != 'Paid'")
	Optional<MemberDue> findActiveDueByUserId(@Param("userId") Long userId);

	@Query(value = """
			SELECT
			    u.full_name as "fullName",
			    u.email as "email",
			    d.assigned_user_id as "assignedUserId",
			    d.status as "status",
			    d.paid_amount as "paidAmount",
			    (d.total_amount - d.paid_amount) as "remainingAmount",
			    d.last_payment_date as "lastPaymentDate",
			    d.due_id as "dueId"
			FROM ecm_member_dues d
			JOIN ecm_users u ON d.assigned_user_id = u.user_id
			WHERE d.club_id = :clubId
			ORDER BY d.last_payment_date DESC
			LIMIT :limit OFFSET :offset
			""", nativeQuery = true)
	List<Map<String, Object>> fetchDuesRaw(@Param("clubId") Long clubId, @Param("limit") int limit,
			@Param("offset") long offset);

	// NEEDED FOR PAGINATION
	@Query(value = "SELECT count(*) FROM ecm_member_dues WHERE club_id = :clubId", nativeQuery = true)
	long countDuesByClub(@Param("clubId") Long clubId);

	@Query(value = """
	        SELECT
	            CAST(COALESCE(SUM(paid_amount), 0) AS NUMERIC) as "duesCollected",
	            CAST(COUNT(*) FILTER (WHERE total_amount - paid_amount <= 0) AS INTEGER) as "paidInFull",
	            CAST(COUNT(*) FILTER (WHERE status = 'Unpaid' AND (total_amount - paid_amount) > 0) AS INTEGER) as "needReminder",
	            CAST(
	                CASE 
	                    WHEN COUNT(*) = 0 THEN 0 
	                    ELSE (COUNT(*) FILTER (WHERE total_amount - paid_amount <= 0) * 100.0 / COUNT(*)) 
	                END AS NUMERIC(5,2)
	            ) as "collectionRate"
	        FROM ecm_member_dues
	        WHERE club_id = :clubId
	        """, nativeQuery = true)
	Map<String, Object> getDuesSummaryMetrics(@Param("clubId") Long clubId);

	@Query(value = """
			SELECT due_id,
			       description,
			       total_amount
			FROM ecm_member_dues
			WHERE club_id = :clubId
			  AND assigned_user_id = :userId
			  AND status IN ('Unpaid', 'Failed')
			ORDER BY due_date ASC
			LIMIT 1
			""", nativeQuery = true)
	List<Object[]> getMemberDue(@Param("clubId") Long clubId, @Param("userId") Long userId);

	@Query(value = """
			SELECT due_id,
			       description,
			       total_amount,
			       due_date
			FROM ecm_member_dues
			WHERE club_id = :clubId
			  AND assigned_user_id = :userId
			  AND status IN ('Unpaid', 'Failed')
			ORDER BY due_date ASC
			LIMIT 2
			""", nativeQuery = true)
	List<Object[]> getRecentMemberDue(@Param("clubId") Long clubId, @Param("userId") Long userId);

	@Query("SELECT d FROM MemberDue d " + "WHERE d.dueId = :dueId " + "AND d.clubId = :clubId "
			+ "AND d.assignedUserId = :userId " + "AND d.paidAmount < d.totalAmount")
	Optional<MemberDue> findUnpaidDueForReminder(@Param("dueId") Long dueId, @Param("clubId") Long clubId,
			@Param("userId") Long userId);

	@Query(value = """
			SELECT
			    imm.invoice_file_name
			FROM public.ecm_member_dues md
			JOIN public.ecm_invoice_member_mapping imm
			    ON imm.invoice_id = md.invoice_id
			WHERE md.due_id = :dueId
			  AND imm.club_id = :clubId
			  AND imm.member_id = :userId
			""", nativeQuery = true)
	List<String> dueReceiptDownload(@Param("userId") Long userId, @Param("clubId") Long clubId,
			@Param("dueId") Long dueId);

	/**
     * Fetches all member dues for a specific club filtered by status.
     * Used for PDF generation and status-specific reports.
     */
    @Query(value = """
            SELECT
                u.full_name as "fullName",
                u.email as "email",
                d.assigned_user_id as "assignedUserId",
                d.status as "status",
                d.paid_amount as "paidAmount",
                (d.total_amount - d.paid_amount) as "remainingAmount",
                d.last_payment_date as "lastPaymentDate",
                d.due_id as "dueId"
            FROM ecm_member_dues d
            JOIN ecm_users u ON d.assigned_user_id = u.user_id
            WHERE d.club_id = :clubId 
              AND d.status = :status
            ORDER BY u.full_name ASC
            """, nativeQuery = true)
    List<Map<String, Object>> fetchDuesByStatusRaw(@Param("clubId") Long clubId, @Param("status") String status);

}