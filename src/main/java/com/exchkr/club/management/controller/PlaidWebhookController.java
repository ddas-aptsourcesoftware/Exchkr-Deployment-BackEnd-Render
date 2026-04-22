package com.exchkr.club.management.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exchkr.club.management.dao.PlaidAccountRepository;

import jakarta.transaction.Transactional;

@RestController
@RequestMapping("/api/webhook/plaid")
public class PlaidWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(PlaidWebhookController.class);

    @Autowired
    private PlaidAccountRepository plaidAccountRepository;

    @PostMapping
    public ResponseEntity<Void> handlePlaidWebhook(@RequestBody Map<String, Object> payload) {
        String webhookType = (String) payload.get("webhook_type");
        String webhookCode = (String) payload.get("webhook_code");
        String itemId = (String) payload.get("item_id");

        logger.info("Received Plaid Webhook: Type={}, Code={}, ItemID={}", webhookType, webhookCode, itemId);

        if (itemId == null) {
            return ResponseEntity.ok().build();
        }

        // Handle ITEM level errors that require user intervention
        switch (webhookCode) {
            case "ERROR":
            case "ITEM_LOGIN_REQUIRED":
            case "PENDING_EXPIRATION":
            case "ITEM_LOCKED":
                deactivatePlaidAccount(itemId, webhookCode);
                break;
                
            default:
                logger.debug("Plaid webhook code {} ignored.", webhookCode);
                break;
        }

        return ResponseEntity.ok().build();
    }

    @Transactional
    private void deactivatePlaidAccount(String itemId, String reason) {
        plaidAccountRepository.findByItemId(itemId).ifPresentOrElse(account -> {
            if (account.isActiveInd()) {
                account.setActiveInd(false);
                // The @PreUpdate in your entity will handle the updatedAt timestamp
                plaidAccountRepository.save(account);
                logger.warn("Plaid Item {} deactivated. Reason: {}", itemId, reason);
                
                // TODO: Trigger a notification to the club officer/user to re-authenticate
            }
        }, () -> logger.error("Received webhook for Item ID {} but no record found in DB", itemId));
    }
}