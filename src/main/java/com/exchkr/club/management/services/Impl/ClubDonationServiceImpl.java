package com.exchkr.club.management.services.Impl;

import com.exchkr.club.management.dao.ClubDonationRepository;

import com.exchkr.club.management.model.api.request.TransactionRequest;
import com.exchkr.club.management.services.ClubDonationService;


import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class ClubDonationServiceImpl implements ClubDonationService {



    private final ClubDonationRepository clubDonationRepository;



    public ClubDonationServiceImpl( ClubDonationRepository clubDonationRepository) {

        this.clubDonationRepository=clubDonationRepository;
    }




    @Override
    public List<Map<String, Object>> getPlatformUniversities() {

        List<String> schools = clubDonationRepository.getPlatformUniversities();

        if (schools == null || schools.isEmpty()) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> response = new ArrayList<>();

        long idCounter = 1L;
        for (String schoolName : schools) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", idCounter++);
            map.put("universityName", schoolName);
            response.add(map);
        }

        return response;
    }

    @Override
    public List<Map<String, Object>> getPlatformClubs(String universityName) {

        List<Object[]> clubs = clubDonationRepository.getPlatformClubs(universityName);

        if (clubs == null || clubs.isEmpty()) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> response = new ArrayList<>();

        for (Object[] row : clubs) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", ((Number) row[0]).longValue()); // club_id
            map.put("clubName", (String) row[1]);         // club_name
            response.add(map);
        }

        return response;
    }

    @Override
    @Transactional
    public ResponseEntity<String> saveDonation(
            Long clubId,
            BigDecimal amount,
            String donatorName,
            String donatorEmail,
            String paymentStatus,
            String stripePaymentIntentId
    ) {

        try {
            // Insert donation
            Long donationId = clubDonationRepository.saveDonation(
                    clubId,
                    donatorName,
                    donatorEmail,
                    amount,
                    stripePaymentIntentId
            );


            if (donationId == null) {
                throw new IllegalStateException("Failed to create donation record");
            }

            // Insert transaction
            int rows = clubDonationRepository.saveDonationTransaction(
                    clubId,
                    amount,
                    paymentStatus,
                    stripePaymentIntentId,
                    donationId
            );


            if (rows != 1) {
                throw new IllegalStateException("Failed to create donation transaction");
            }


            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Donation transaction created successfully");

        } catch (Exception ex) {
            ex.printStackTrace(); // VERY important for debugging

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error while creating donation transaction");
        }
    }


}