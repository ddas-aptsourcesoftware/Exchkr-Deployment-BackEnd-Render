package com.exchkr.club.management.model.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "ecm_reimbursement_requests")
public class Reimbursement {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reim_seq")
    @SequenceGenerator(
            name = "reim_seq",
            sequenceName = "ecm_reimbursement_requests_s",
            allocationSize = 1
    )
    @Column(name = "reimbursement_id")
    private Long reimbursementId;

    @Column(name = "club_id", nullable = false)
    private Long clubId;

    @Column(name = "submitted_by_member_id", nullable = false)
    private Long submittedByMemberId;

    @Column(name = "receipt_file_name", nullable = false)
    private String receiptFileName;

    @Column(name = "receipt_file_system_name", nullable = false)
    private String receiptFileSystemName;

    @Column(name = "amount_usd", nullable = false, precision = 14, scale = 2)
    private BigDecimal amountUsd;

    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;

    @Column(name = "category", length = 50, nullable = false)
    private String category;

    @Column(name = "description")
    private String description;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "submitted_at", nullable = false)
    private OffsetDateTime submittedAt;

    @Column(name = "rejected_by_officer_id")
    private Long rejectedByOfficerId;

    @Column(name = "rejected_at")
    private OffsetDateTime rejectedAt;

    @Column(name = "stripe_ref_id")
    private String stripeRefId;

	public Long getReimbursementId() {
		return reimbursementId;
	}

	public Long getClubId() {
		return clubId;
	}

	public void setClubId(Long clubId) {
		this.clubId = clubId;
	}

	public Long getSubmittedByMemberId() {
		return submittedByMemberId;
	}

	public void setSubmittedByMemberId(Long submittedByMemberId) {
		this.submittedByMemberId = submittedByMemberId;
	}

	public String getReceiptFileName() {
		return receiptFileName;
	}

	public void setReceiptFileName(String receiptFileName) {
		this.receiptFileName = receiptFileName;
	}

	public String getReceiptFileSystemName() {
		return receiptFileSystemName;
	}

	public void setReceiptFileSystemName(String receiptFileSystemName) {
		this.receiptFileSystemName = receiptFileSystemName;
	}

	public BigDecimal getAmountUsd() {
		return amountUsd;
	}

	public void setAmountUsd(BigDecimal amountUsd) {
		this.amountUsd = amountUsd;
	}

	public LocalDate getPurchaseDate() {
		return purchaseDate;
	}

	public void setPurchaseDate(LocalDate purchaseDate) {
		this.purchaseDate = purchaseDate;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public OffsetDateTime getSubmittedAt() {
		return submittedAt;
	}

	public void setSubmittedAt(OffsetDateTime submittedAt) {
		this.submittedAt = submittedAt;
	}

	public Long getRejectedByOfficerId() {
		return rejectedByOfficerId;
	}

	public void setRejectedByOfficerId(Long rejectedByOfficerId) {
		this.rejectedByOfficerId = rejectedByOfficerId;
	}

	public OffsetDateTime getRejectedAt() {
		return rejectedAt;
	}

	public void setRejectedAt(OffsetDateTime rejectedAt) {
		this.rejectedAt = rejectedAt;
	}

	public String getStripeRefId() {
		return stripeRefId;
	}

	public void setStripeRefId(String stripeRefId) {
		this.stripeRefId = stripeRefId;
	}

	public void setReimbursementId(Long reimbursementId) {
		this.reimbursementId = reimbursementId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
