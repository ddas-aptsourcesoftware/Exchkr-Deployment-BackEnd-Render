package com.exchkr.club.management.model.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "ecm_invoice_headers")
public class InvoiceHeader {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "invoice_id")
	private Long invoiceId;

	@Column(name = "club_id", nullable = false)
	private Long clubId;

	@Column(name = "invoice_title")
	private String invoiceTitle;

	@Column(name = "invoice_due_date")
	private Instant invoiceDueDate;

	@Column(name = "invoice_total_amount")
	private BigDecimal invoiceTotalAmount;

	@Column(name = "platform_fees")
	private BigDecimal platformFees;

	@Column(name = "payment_gateway_service_charge")
	private BigDecimal stripeFees;

	@Column(name = "additional_note")
	private String additionalNote;

	@Column(name = "created_by", nullable = false)
	private Long createdBy;

	public InvoiceHeader() {
	}

	public Long getInvoiceId() {
		return invoiceId;
	}

	public void setInvoiceId(Long invoiceId) {
		this.invoiceId = invoiceId;
	}

	public Long getClubId() {
		return clubId;
	}

	public void setClubId(Long clubId) {
		this.clubId = clubId;
	}

	public String getInvoiceTitle() {
		return invoiceTitle;
	}

	public void setInvoiceTitle(String invoiceTitle) {
		this.invoiceTitle = invoiceTitle;
	}

	public Instant getInvoiceDueDate() {
		return invoiceDueDate;
	}

	public void setInvoiceDueDate(Instant invoiceDueDate) {
		this.invoiceDueDate = invoiceDueDate;
	}

	public BigDecimal getInvoiceTotalAmount() {
		return invoiceTotalAmount;
	}

	public void setInvoiceTotalAmount(BigDecimal invoiceTotalAmount) {
		this.invoiceTotalAmount = invoiceTotalAmount;
	}

	public BigDecimal getPlatformFees() {
		return platformFees;
	}

	public void setPlatformFees(BigDecimal platformFees) {
		this.platformFees = platformFees;
	}

	public BigDecimal getStripeFees() {
		return stripeFees;
	}

	public void setStripeFees(BigDecimal stripeFees) {
		this.stripeFees = stripeFees;
	}

	public String getAdditionalNote() {
		return additionalNote;
	}

	public void setAdditionalNote(String additionalNote) {
		this.additionalNote = additionalNote;
	}

	public Long getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(Long createdBy) {
		this.createdBy = createdBy;
	}
}