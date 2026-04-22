package com.exchkr.club.management.services;

import com.exchkr.club.management.model.api.request.TransactionRequest;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ClubDonationService {

    List<Map<String, Object>> getPlatformUniversities();

    List<Map<String, Object>> getPlatformClubs(String universityName);

    ResponseEntity<String> saveDonation(
            Long clubId, BigDecimal amount, String donatorName,
            String donatorEmail, String paymentStatus, String stripePaymentIntentId );
}