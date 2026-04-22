package com.exchkr.club.management.services;

import com.stripe.model.Event;

public interface StripePaymentWebhookService {

    /**
     * Process a Stripe payment webhook event
     * @param event Stripe Event object
     */
    void processEvent(Event event);
}
