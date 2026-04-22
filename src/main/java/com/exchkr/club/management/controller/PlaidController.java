package com.exchkr.club.management.controller;

import com.exchkr.club.management.model.api.request.PlaidExchangeRequest;
import com.exchkr.club.management.security.CustomUserDetails;
import com.exchkr.club.management.services.PlaidService;
import com.plaid.client.model.AccountBase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/plaid")
public class PlaidController {

    private final PlaidService plaidService;

    public PlaidController(PlaidService plaidService) {
        this.plaidService = plaidService;
    }

    private CustomUserDetails getPrincipal(Authentication authentication) {
        return (CustomUserDetails) authentication.getPrincipal();
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getLinkStatus(Authentication authentication) {
        CustomUserDetails user = getPrincipal(authentication);
        return ResponseEntity.ok(plaidService.getLinkStatus(user.getClubId()));
    }

    @PostMapping("/link-token")
    public ResponseEntity<Map<String, String>> getLinkToken(Authentication authentication) throws Exception {
        CustomUserDetails user = getPrincipal(authentication);
        String linkToken = plaidService.createLinkToken(user.getUserId(), user.getClubId());
        return ResponseEntity.ok(Collections.singletonMap("link_token", linkToken));
    }
    
    @PostMapping("/reactivate")
    public ResponseEntity<?> reactivateAccount(Authentication authentication) throws Exception {
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        plaidService.reactivateAccount(user.getClubId());
        return ResponseEntity.ok(Map.of("message", "Account reactivated"));
    }

    @PostMapping("/exchange-token")
    public ResponseEntity<Map<String, String>> exchangeToken(
            @RequestBody PlaidExchangeRequest payload,
            Authentication authentication) throws Exception {

        CustomUserDetails user = getPrincipal(authentication);
        String status = plaidService.exchangePublicToken(payload, user.getUserId(), user.getClubId());
        return ResponseEntity.ok(Collections.singletonMap("status", status));
    }

    @GetMapping("/balance")
    public ResponseEntity<List<AccountBase>> getBalance(Authentication authentication) throws Exception {
        CustomUserDetails user = getPrincipal(authentication);
        return ResponseEntity.ok(plaidService.getAccountBalance(user.getClubId()));
    }

    @PatchMapping("/default-account")
    public ResponseEntity<?> updateDefaultAccount(
            @RequestBody Map<String, String> payload,
            Authentication authentication) {

        CustomUserDetails user = getPrincipal(authentication);
        plaidService.updateDefaultAccount(user.getClubId(), payload.get("accountId"));
        return ResponseEntity.ok(Collections.singletonMap("message", "Default account updated"));
    }

    @PostMapping("/unlink")
    public ResponseEntity<?> unlinkBank(Authentication authentication) throws Exception {
        CustomUserDetails user = getPrincipal(authentication);
        plaidService.unlinkAccount(user.getClubId());
        return ResponseEntity.ok(Collections.singletonMap("message", "Bank unlinked successfully"));
    }
}