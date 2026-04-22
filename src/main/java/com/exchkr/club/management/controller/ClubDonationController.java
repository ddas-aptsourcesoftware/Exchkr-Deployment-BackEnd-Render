package com.exchkr.club.management.controller;

import com.exchkr.club.management.model.api.request.ClubDonationRequest;
import com.exchkr.club.management.model.api.request.StripeMemberPaymentRequest;
import com.exchkr.club.management.model.api.request.TransactionRequest;
import com.exchkr.club.management.model.api.response.StripeResponse;
import com.exchkr.club.management.security.CustomUserDetails;
import com.exchkr.club.management.services.ClubDonationService;
import com.exchkr.club.management.services.StripeService;

import com.stripe.exception.StripeException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/donation")
public class ClubDonationController {

    private final ClubDonationService clubDonationService;
    private final StripeService stripeService;

    public ClubDonationController(ClubDonationService clubDonationService, StripeService stripeService) {
        this.clubDonationService = clubDonationService;
        this.stripeService = stripeService;
    }


    @GetMapping("/get-universities")
    public ResponseEntity<List<Map<String, Object>>> getPlatformUniversities() {

        List<Map<String, Object>> response =
                clubDonationService.getPlatformUniversities();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-clubs")
    public ResponseEntity<List<Map<String, Object>>> getPlatformClubs(
            @RequestParam String universityName
    ) {
        List<Map<String, Object>> response =
                clubDonationService.getPlatformClubs(universityName);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/donation-payment-intent")
    public ResponseEntity<StripeResponse> memberPaymentIntent(
            @RequestBody StripeMemberPaymentRequest request) {

        try {
            StripeResponse response = stripeService.donationPaymentIntent(
                    request.getClubId(),
                    request.getAmount(),
                    request.getDescription(),
                    request.getPaymentMethodType()
            );

            return ResponseEntity.ok(response);

        } catch (StripeException e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/save-donation")
    public ResponseEntity<String> saveDonationTransaction(
            @RequestBody ClubDonationRequest request
            ) {


        return clubDonationService.saveDonation(
                request.getClubId(),
                request.getAmount(),
                request.getDonatorName(),
                request.getDonatorEmail(),
                request.getPaymentStatus(),
                request.getStripePaymentIntentId()
        );
    }
}
