package com.exchkr.club.management.services.Impl;

import com.exchkr.club.management.dao.UserRepository;
import com.exchkr.club.management.model.entity.User;
import com.exchkr.club.management.model.api.request.AccountRecoveryRequest;
import com.exchkr.club.management.services.AccountRecoveryService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
public class AccountRecoveryServiceImpl implements AccountRecoveryService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountRecoveryServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void resetPassword(AccountRecoveryRequest request) {
        // 1. Fetch User (Verification)
        User user = userRepository.findUserByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));

        // 2. Validate Current Password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "The current password provided is incorrect.");
        }

        // 3. Encode and Update by ID
        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());

        userRepository.updatePasswordByUserId(user.getUserId(), encodedNewPassword, Instant.now());
    }
}