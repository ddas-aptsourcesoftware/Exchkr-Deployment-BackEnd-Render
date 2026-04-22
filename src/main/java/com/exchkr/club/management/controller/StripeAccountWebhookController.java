package com.exchkr.club.management.controller;

import com.exchkr.club.management.config.StripeWebhookConfig;
import com.exchkr.club.management.services.StripeAccountWebhookService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook/stripe/account")
public class StripeAccountWebhookController {

    @Autowired
    private StripeWebhookConfig webhookConfig;

    @Autowired
    private StripeAccountWebhookService accountWebhookService;

    @PostMapping
    public ResponseEntity<Void> handleAccountWebhook(
            @RequestBody String payload,                        // RAW BODY
            @RequestHeader("Stripe-Signature") String sigHeader  // SIGNATURE
    ) {
        try {
            // Verify Stripe signature
            Event event = Webhook.constructEvent(
                    payload,
                    sigHeader,
                    webhookConfig.getAccountWebhookSecret()
            );

            // Delegate event processing
            accountWebhookService.processEvent(event);

            return ResponseEntity.ok().build();

        } catch (SignatureVerificationException e) {
            // ❌ Invalid signature
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            // ❌ Processing failure → Stripe retries
            return ResponseEntity.internalServerError().build();
        }
    }
}
