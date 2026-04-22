package com.exchkr.club.management.controller;

import com.exchkr.club.management.model.api.request.StripeOnboardRequest;
import com.exchkr.club.management.model.api.request.StripeMemberOnboardRequest;
import com.exchkr.club.management.model.api.request.StripeMemberPaymentRequest;
import com.exchkr.club.management.model.api.request.StripeClubPaymentRequest;
import com.exchkr.club.management.model.api.request.StripeBalanceRequest;
import com.exchkr.club.management.model.api.response.MemberDuesResponse;
import com.exchkr.club.management.model.api.response.StripeAccountResponse;
import com.exchkr.club.management.model.api.response.StripeResponse;
import com.exchkr.club.management.security.CustomUserDetails;
import com.exchkr.club.management.services.StripeService;
import com.stripe.exception.StripeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stripe")
public class StripeController {

    @Autowired
    private StripeService stripeService;

    @PostMapping("/account-onboarding")
    public ResponseEntity<StripeResponse> createAccountAndOnboard(
            @RequestBody StripeOnboardRequest request) {

        try {
            StripeResponse response = stripeService.createAccountAndOnboard(
                    request.getUserId(),
                    request.getClubId()
            );

            return ResponseEntity.ok(response);

        } catch (StripeException e) {
            return ResponseEntity.status(500).build();
        }
    }


    @PostMapping("/member-payment-intent")
    public ResponseEntity<StripeResponse> memberPaymentIntent(
            @RequestBody StripeMemberPaymentRequest request, @AuthenticationPrincipal CustomUserDetails user) {

        try {
            StripeResponse response = stripeService.memberPaymentIntent(
                    user.getClubId(),
                    request.getAmount(),
                    request.getDescription(),
                    request.getPaymentMethodType()
            );

            return ResponseEntity.ok(response);

        } catch (StripeException e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/club-payment-intent")
    public ResponseEntity<StripeResponse> clubPaymentIntent(
            @RequestBody StripeClubPaymentRequest request) {

        try {
            StripeResponse response = stripeService.clubPaymentIntent(
                    request.getUserId(),
                    request.getAmount(),
                    request.getDescription(),
                    request.getPaymentMethodType()
            );

            return ResponseEntity.ok(response);

        } catch (StripeException e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/stripe-balance")
    public ResponseEntity<StripeResponse> getStripeBalance(
            @AuthenticationPrincipal CustomUserDetails user ) {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            StripeResponse response = stripeService.getStripeBalance(user.getClubId());

            return ResponseEntity.ok(response);

        } catch (StripeException e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/member-stripe-onboarding")
    public ResponseEntity<StripeResponse> createMemberAccountAndOnboard(
            @RequestBody StripeMemberOnboardRequest request) {

        try {
            StripeResponse response = stripeService.createMemberAccountAndOnboard(
                    request.getUserId()
            );

            return ResponseEntity.ok(response);

        } catch (StripeException e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/club-stripe-info")
    public ResponseEntity<StripeAccountResponse> getClubStripeInfo(
            @AuthenticationPrincipal CustomUserDetails user) {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Delegate to service to fetch transactions
        return stripeService.getClubStripeInfo(user.getClubId());
    }


    @GetMapping("/member-stripe-info")
    public ResponseEntity<StripeAccountResponse> getMemberStripeInfo(
            @AuthenticationPrincipal CustomUserDetails user) {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Delegate to service to fetch transactions
        return stripeService.getMemberStripeInfo(user.getUserId());
    }
}
