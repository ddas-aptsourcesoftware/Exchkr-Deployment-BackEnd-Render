package com.exchkr.club.management.services;

import com.stripe.model.Event;

public interface StripeAccountWebhookService {

    /**
     * Process a Stripe account webhook event
     * @param event Stripe Event object
     */
    void processEvent(Event event);
}
