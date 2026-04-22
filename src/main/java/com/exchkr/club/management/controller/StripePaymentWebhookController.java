package com.exchkr.club.management.controller;

import com.exchkr.club.management.config.StripeWebhookConfig;
import com.exchkr.club.management.services.StripePaymentWebhookService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook/stripe/payment")
public class StripePaymentWebhookController {

    @Autowired
    private StripeWebhookConfig webhookConfig;

    @Autowired
    private StripePaymentWebhookService paymentWebhookService;

    @PostMapping
    public ResponseEntity<Void> handlePaymentWebhook(
            @RequestBody String payload,                        // ✅ CHANGE #1: RAW BODY
            @RequestHeader("Stripe-Signature") String sigHeader  // ✅ Stripe signature
    ) {
        try {
            // Verify Stripe signature
            Event event = Webhook.constructEvent(
                    payload,
                    sigHeader,
                    webhookConfig.getPaymentWebhookSecret()
            );

            // Delegate processing to service layer
            paymentWebhookService.processEvent(event);

            // ✅ 200 OK → Stripe marks webhook as delivered
            return ResponseEntity.ok().build();

        } catch (SignatureVerificationException e) {
            // ❌ Invalid signature → Stripe retries or flags endpoint
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            // ❌ Processing failure → Stripe retries
            return ResponseEntity.internalServerError().build();
        }
    }
}
