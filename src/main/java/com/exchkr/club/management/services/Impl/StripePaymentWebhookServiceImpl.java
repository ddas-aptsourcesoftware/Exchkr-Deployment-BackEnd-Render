package com.exchkr.club.management.services.Impl;

import com.exchkr.club.management.model.entity.ClubTransaction;
import com.exchkr.club.management.model.entity.MemberDue;
import com.exchkr.club.management.model.entity.Reimbursement;
import com.exchkr.club.management.dao.TransactionRepository;
import com.exchkr.club.management.dao.ClubBudgetRepository;
import com.exchkr.club.management.dao.MemberDuesRepository;
import com.exchkr.club.management.dao.ReimbursementRepository;
import com.exchkr.club.management.services.StripePaymentWebhookService;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.ApiResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

@Service
public class StripePaymentWebhookServiceImpl implements StripePaymentWebhookService {

	private static final Logger logger = LoggerFactory.getLogger(StripePaymentWebhookServiceImpl.class);

	@Autowired
	private TransactionRepository transactionRepository;

	@Autowired
	private MemberDuesRepository duesRepository;

	@Autowired
	private ReimbursementRepository reimbursementRepository;

	@Autowired
	private ClubBudgetRepository budgetRepository;

	@Override
	@Transactional
	public void processEvent(Event event) {
		try {
			switch (event.getType()) {
			case "payment_intent.succeeded":
			case "payment_intent.payment_failed":
			case "payment_intent.processing":
				handlePaymentIntent(event);
				break;
			default:
				logger.info("ℹ️ Payment event ignored: {}", event.getType());
			}
		} catch (Exception e) {
			logger.error("❌ Error processing Stripe event: {}", event.getType(), e);
			throw e;
		}
	}

	private void handlePaymentIntent(Event event) {
		logger.debug("Raw Stripe Payload: {}", event.getDataObjectDeserializer().getRawJson());

		logger.info("Received Stripe event. Type: {}, EventID: {}", event.getType(), event.getId());

		PaymentIntent pi = ApiResource.GSON.fromJson(event.getDataObjectDeserializer().getRawJson(),
				PaymentIntent.class);

		if (pi == null) {
			logger.error("Failed to deserialize PaymentIntent from event {}", event.getId());
			return;
		}

		String stripeRefId = pi.getId();
		logger.info("Looking up transaction with StripeRefId: {}", stripeRefId);

		Optional<ClubTransaction> txOpt = transactionRepository.findByStripeRefId(stripeRefId);

		if (txOpt.isEmpty()) {
			logger.warn("No transaction found for StripeRefId: {}. Webhook will be ignored.", stripeRefId);
			return;
		}

		logger.info("Transaction found for StripeRefId: {}", stripeRefId);

		ClubTransaction tx = txOpt.get();

		logger.info("Current TX State → ID: {}, OldStatus: {}, Amount: {}, DueId: {}", tx.getTransId(),
				tx.getStatus(), tx.getAmount(), tx.getDueId());

		String oldStatus = tx.getStatus();
		String newStatus = mapStripeEventToStatus(event.getType());

		if (newStatus == null) {
			logger.info("Event type {} not mapped to internal status. Skipping update.", event.getType());
			return;
		}

		logger.info("Updating Transaction status from {} → {}", oldStatus, newStatus);

		// 1. Update the Ledger (Transaction table)
		tx.setStatus(newStatus);
		tx.setTransDate(Instant.now());
		transactionRepository.save(tx);

		logger.info("Transaction saved successfully for StripeRefId: {}", stripeRefId);

		// 2. Branching Logic: Member Dues vs Reimbursement
		if (tx.getDueId() != null) {
			updateDueRecord(tx.getDueId(), oldStatus, newStatus, tx.getAmount());
		}
//		} else if ("Reimbursement".equalsIgnoreCase(tx.getCategory()) && "Completed".equals(newStatus)) {
//			updateReimbursementRecord(stripeRefId, tx.getAmount());
//		}
		logger.info("Finished processing StripeRefId: {}", stripeRefId);

	}

	private String mapStripeEventToStatus(String eventType) {
		return switch (eventType) {
		case "payment_intent.succeeded" -> "Completed";
		case "payment_intent.payment_failed" -> "Failed";
		case "payment_intent.processing" -> "Processing";
		default -> null; // We currently dont care about other statuses for these specific updates
		};
	}

//	private void updateReimbursementRecord(String stripeRefId, BigDecimal amount) {
//		// 1. Initial lookup
//		Optional<Reimbursement> reimbursementOpt = reimbursementRepository.findByStripeRefId(stripeRefId);
//
//		// 2. Retry Logic: If not found immediately, wait for the initiating transaction
//		// to commit
//		if (reimbursementOpt.isEmpty()) {
//			try {
//				logger.info("⏱️ Reimbursement record not found for Stripe Ref: {}. Waiting 1.5s for DB commit...",
//						stripeRefId);
//				Thread.sleep(1500); // 1.5 second delay
//				reimbursementOpt = reimbursementRepository.findByStripeRefId(stripeRefId);
//			} catch (InterruptedException e) {
//				logger.error("❌ Retry sleep interrupted", e);
//				Thread.currentThread().interrupt();
//			}
//		}
//
//		// 3. Process the record if found
//		if (reimbursementOpt.isPresent()) {
//			Reimbursement reimbursement = reimbursementOpt.get();
//
//			// Idempotency check: Avoid double-processing if Stripe sends the webhook twice
//			if ("PAID".equalsIgnoreCase(reimbursement.getStatus())) {
//				logger.info("ℹ️ Reimbursement ID: {} is already PAID. Skipping.", reimbursement.getReimbursementId());
//				return;
//			}
//
//			// 4. Update Reimbursement Status
//			reimbursement.setStatus("PAID");
//			reimbursementRepository.save(reimbursement);
//			logger.info("✅ Reimbursement ID: {} marked as PAID via Webhook", reimbursement.getReimbursementId());
//
//			// 5. Update the Budget Category Spent Amount
//			int currentYear = java.time.LocalDate.now().getYear();
//			try {
//				budgetRepository.updateSpentAmount(reimbursement.getClubId(), currentYear, reimbursement.getCategory(),
//						amount);
//				logger.info("📊 Budget updated: Club {}, Category '{}', Added: {}", reimbursement.getClubId(),
//						reimbursement.getCategory(), amount);
//			} catch (Exception e) {
//				// Log error but don't fail the payment processing
//				logger.error("❌ Failed to update budget for reimbursement ID: {}", reimbursement.getReimbursementId(),
//						e);
//			}
//
//		} else {
//			// 6. Hard Failure: Tell Stripe to retry later
//			logger.error("❌ Webhook Error: No matching Reimbursement found for Stripe Ref: {} after retry.",
//					stripeRefId);
//			// Throwing an exception here ensures Stripe receives a 500 error and retries
//			// later
//			throw new RuntimeException("Reimbursement record [" + stripeRefId + "] not found/committed yet.");
//		}
//	}

	private void updateDueRecord(Long dueId, String oldStatus, String newStatus, BigDecimal transAmount) {
		MemberDue due = duesRepository.findById(dueId)
				.orElseThrow(() -> new RuntimeException("Due record not found for ID: " + dueId));

		// Logic: Only add amount if we are transitioning to 'Completed' from a
		// non-completed state
		if ("Completed".equals(newStatus) && !"Completed".equals(oldStatus)) {
			BigDecimal currentPaid = (due.getPaidAmount() != null) ? due.getPaidAmount() : BigDecimal.ZERO;
			BigDecimal updatedTotalPaid = currentPaid.add(transAmount);

			due.setPaidAmount(updatedTotalPaid);
			due.setLastPaymentDate(Instant.now());

			// Check if fully paid or partial
			if (updatedTotalPaid.compareTo(due.getTotalAmount()) >= 0) {
				due.setStatus("Paid");
			} else {
				due.setStatus("Partial");
			}
		} else if ("Processing".equals(newStatus)) {
			due.setStatus("Processing");
		} else if ("Failed".equals(newStatus)) {
			// If the transaction failed, and the due isn't already 'Paid' from another
			// source, reset it
			if (!"Paid".equals(due.getStatus()) && !"Partial".equals(due.getStatus())) {
				due.setStatus("Unpaid");
			}
		}

		duesRepository.save(due);
		logger.info("Updated Due ID: {} status to {}", dueId, due.getStatus());
	}
}