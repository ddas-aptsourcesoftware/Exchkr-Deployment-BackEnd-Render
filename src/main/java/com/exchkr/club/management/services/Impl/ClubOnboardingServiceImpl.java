package com.exchkr.club.management.services.Impl;

import com.exchkr.club.management.dao.ClubRepository;
import com.exchkr.club.management.dao.UserRepository;
import com.exchkr.club.management.dao.UserClubRepository;
import com.exchkr.club.management.model.entity.Club;
import com.exchkr.club.management.model.entity.User;
import com.exchkr.club.management.model.entity.UserClub;
import com.exchkr.club.management.model.api.request.ClubOnboardingRequest;
import com.exchkr.club.management.services.ClubOnboardingService;
import com.exchkr.club.management.services.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

@Service
public class ClubOnboardingServiceImpl implements ClubOnboardingService {

    private static final Logger logger = LoggerFactory.getLogger(ClubOnboardingServiceImpl.class);

    private final ClubRepository clubRepository;
    private final UserRepository userRepository;
    private final UserClubRepository userClubRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private static final Long PRIMARY_OFFICER_ROLE_ID = 6L;
    private static final String DEFAULT_ACTIVE_STATUS = "Active";

    public ClubOnboardingServiceImpl(ClubRepository clubRepository, 
                                   UserRepository userRepository, 
                                   UserClubRepository userClubRepository,
                                   PasswordEncoder passwordEncoder, 
                                   EmailService emailService) {
        this.clubRepository = clubRepository;
        this.userRepository = userRepository;
        this.userClubRepository = userClubRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public void onboardClubAndOfficer(ClubOnboardingRequest request) {
        logger.info("Starting onboarding for club: {}", request.getClubName());

        // 1. Check if User Email already exists
        if (userRepository.findUserByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User with this email already exists.");
        }
        
        // 2. NEW CHECK: Check for duplicate Club + School combination
        if (clubRepository.existsByClubNameAndSchoolName(request.getClubName(), request.getSchoolName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Combination of this club name and school name already exists.");
        }

        Instant now = Instant.now();
        
        // 1. Save the Club
        Club club = new Club();
        club.setClubName(request.getClubName());
        club.setSchoolName(request.getSchoolName());
        club.setStatus(DEFAULT_ACTIVE_STATUS); 
        club.setCreatedOn(now);
        club.setLastUpdatedOn(now);
        Club savedClub = clubRepository.save(club);

        // 2. Prepare User (The Officer)
        String initialPassword = UUID.randomUUID().toString().substring(0, 12);
        String[] nameParts = splitFullName(request.getFullName());

        User officer = new User();
        officer.setEmail(request.getEmail());
        officer.setFullName(request.getFullName());
        officer.setFirstName(nameParts[0]);
        officer.setLastName(nameParts[1]);
        
        // FIX: Set the userName to avoid the NOT NULL constraint violation
        officer.setUserName(request.getEmail()); 
        
        officer.setPassword(passwordEncoder.encode(initialPassword));
        officer.setStatus(DEFAULT_ACTIVE_STATUS);
        officer.setCreatedOn(now);
        officer.setLastUpdatedOn(now);

        User savedUser = userRepository.save(officer);

        // 3. Create the Mapping
        UserClub membership = new UserClub();
        membership.setUserId(savedUser.getUserId());
        membership.setClubId(savedClub.getClubId());
        membership.setRoleId(PRIMARY_OFFICER_ROLE_ID);
        membership.setStatus(DEFAULT_ACTIVE_STATUS);
        membership.setJoinedAt(now); 
        
        userClubRepository.save(membership);

        // 4. Notification
        emailService.sendInitialCredentials(
            savedUser.getEmail(), 
            initialPassword, 
            "Welcome! You have been registered as the primary officer for " + savedClub.getClubName()
        );

        logger.info("Onboarding complete for {} (Club ID: {})", savedUser.getEmail(), savedClub.getClubId());
    }

    private String[] splitFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) return new String[]{"", ""};
        String[] parts = fullName.trim().split("\\s+", 2);
        return new String[]{parts[0], parts.length > 1 ? parts[1] : ""};
    }
}