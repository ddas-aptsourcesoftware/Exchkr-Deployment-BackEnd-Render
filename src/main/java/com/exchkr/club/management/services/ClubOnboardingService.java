package com.exchkr.club.management.services;

import com.exchkr.club.management.model.api.request.ClubOnboardingRequest;

public interface ClubOnboardingService {
    void onboardClubAndOfficer(ClubOnboardingRequest request);
}