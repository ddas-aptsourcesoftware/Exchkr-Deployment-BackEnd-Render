package com.exchkr.club.management.controller;

import com.exchkr.club.management.model.api.request.AccountRecoveryRequest;
import com.exchkr.club.management.security.CustomUserDetails; // Import your new class
import com.exchkr.club.management.services.AccountRecoveryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/password")
public class AccountRecoveryController {

    private final AccountRecoveryService accountRecoveryService;

    public AccountRecoveryController(AccountRecoveryService accountRecoveryService) {
        this.accountRecoveryService = accountRecoveryService;
    }

    @PostMapping("/reset")
    public ResponseEntity<String> resetPassword(
            @RequestBody AccountRecoveryRequest request) { 


        // Delegate to service
        accountRecoveryService.resetPassword(request);

        return ResponseEntity.ok("Password updated successfully.");
    }
}