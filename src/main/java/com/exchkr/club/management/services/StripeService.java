package com.exchkr.club.management.services;

import com.exchkr.club.management.model.api.response.MemberDuesResponse;
import com.exchkr.club.management.model.api.response.StripeAccountResponse;
import com.stripe.exception.StripeException;
import com.exchkr.club.management.model.api.response.StripeResponse;
import org.springframework.http.ResponseEntity;

public interface StripeService {
    StripeResponse createAccountAndOnboard(long userId, long clubId) throws StripeException;

    StripeResponse memberPaymentIntent(long clubId, long amount, String description, String paymentMethodType) throws StripeException;

    StripeResponse clubPaymentIntent(long userId, long amount, String description, String paymentMethodType) throws StripeException;

    StripeResponse getStripeBalance(long clubId) throws StripeException;

    StripeResponse createMemberAccountAndOnboard(long userId) throws StripeException;

    ResponseEntity<StripeAccountResponse> getClubStripeInfo(Long clubId);

    ResponseEntity<StripeAccountResponse> getMemberStripeInfo(long userId);

    StripeResponse donationPaymentIntent(long clubId, long amount, String description, String paymentMethodType) throws StripeException;

}
