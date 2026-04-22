package com.exchkr.club.management.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeWebhookConfig {

    @Value("${stripe.webhook.secret.account}")
    private String accountWebhookSecret;

    @Value("${stripe.webhook.secret.payment}")
    private String paymentWebhookSecret;

    public String getAccountWebhookSecret() {
        if (accountWebhookSecret == null || accountWebhookSecret.isBlank()) {
            throw new IllegalStateException("❌ Stripe Account Webhook secret is NOT configured!");
        }
        return accountWebhookSecret;
    }

    public String getPaymentWebhookSecret() {
        if (paymentWebhookSecret == null || paymentWebhookSecret.isBlank()) {
            throw new IllegalStateException("❌ Stripe Payment Webhook secret is NOT configured!");
        }
        return paymentWebhookSecret;
    }
}
