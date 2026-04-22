package com.exchkr.club.management.controller;

import com.exchkr.club.management.model.api.request.ClubOnboardingRequest;
import com.exchkr.club.management.services.ClubOnboardingService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;


@RestController
@RequestMapping("/api/admin/onboarding")
public class ClubOnboardingController {

    private final ClubOnboardingService onboardingService;

    public ClubOnboardingController(ClubOnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    /**
     * Registers a new club and its initial officer.
     * <p>
     * This endpoint is used during the administrative onboarding process
     * to create a club entity along with its primary officer account
     * in a single transactional operation.
     * </p>
     * On successful onboarding, the club and officer are persisted and
     * the system is ready for initial access.
     */
    @PostMapping("/club")
    public ResponseEntity<Void> registerClub(
            @RequestBody ClubOnboardingRequest request) {

        onboardingService.onboardClubAndOfficer(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
