package com.exchkr.club.management.model.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "ecm_clubs_transactions")
public class ClubTransaction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "trans_id")
	private Long transId;

	@Column(name = "club_id", nullable = false)
	private Long clubId;

	@Column(name = "done_by_user_id", nullable = false)
	private Long doneByUserId;

	@Column(name = "trans_date", nullable = false)
	private Instant transDate;

	@Column(nullable = false)
	private String description;

	@Column(nullable = false)
	private String category;

	@Column(nullable = false)
	private String type;

	@Column(nullable = false)
	private BigDecimal amount;

	@Column(nullable = false)
	private String status;

	@Column(name = "stripe_ref_id")
	private String stripeRefId;

	@Column(name = "paid_to_user_id")
	private Long paidToUserId;

	@Column(name = "due_id")
	private Long dueId;

	@Column(name = "platform_fees")
	private BigDecimal platformFees;

	@Column(name = "payment_gateway_service_charge")
	private BigDecimal paymentGatewayServiceCharge;

	public BigDecimal getPlatformFees() {
		return platformFees;
	}

	public void setPlatformFees(BigDecimal platformFees) {
		this.platformFees = platformFees;
	}

	public BigDecimal getPaymentGatewayServiceCharge() {
		return paymentGatewayServiceCharge;
	}

	public void setPaymentGatewayServiceCharge(BigDecimal charge) {
		this.paymentGatewayServiceCharge = charge;
	}

	public Long getTransId() {
		return transId;
	}

	public Long getPaidToUserId() {
		return paidToUserId;
	}

	public void setPaidToUserId(Long paidToUserId) {
		this.paidToUserId = paidToUserId;
	}

	public void setTransId(Long transId) {
		this.transId = transId;
	}

	public Long getClubId() {
		return clubId;
	}

	public void setClubId(Long clubId) {
		this.clubId = clubId;
	}

	public Long getDoneByUserId() {
		return doneByUserId;
	}

	public void setDoneByUserId(Long doneByUserId) {
		this.doneByUserId = doneByUserId;
	}

	public Instant getTransDate() {
		return transDate;
	}

	public void setTransDate(Instant transDate) {
		this.transDate = transDate;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStripeRefId() {
		return stripeRefId;
	}

	public void setStripeRefId(String stripeRefId) {
		this.stripeRefId = stripeRefId;
	}

	public Long getDueId() {
		return dueId;
	}

	public void setDueId(Long dueId) {
		this.dueId = dueId;
	}

}