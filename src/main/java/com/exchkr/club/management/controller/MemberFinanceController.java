package com.exchkr.club.management.controller;

import com.exchkr.club.management.model.api.request.ReimbursementReceiptDownloadRequest;
import com.exchkr.club.management.model.api.request.TransactionRequest;
import com.exchkr.club.management.model.api.request.MemberReimbursementRequest;
import com.exchkr.club.management.model.api.response.ClubCategoriesResponse;
import com.exchkr.club.management.model.api.response.MemberDuesResponse;
import com.exchkr.club.management.model.api.response.MembersTransactionsResponse;
import com.exchkr.club.management.model.api.response.RecentDuesResponse;
import com.exchkr.club.management.security.CustomUserDetails;
import com.exchkr.club.management.services.MemberFinanceService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/member/finance")
public class MemberFinanceController {

    private final MemberFinanceService memberFinanceService;

    public MemberFinanceController(MemberFinanceService memberFinanceService) {
        this.memberFinanceService = memberFinanceService;
    }

    /**
     * Securely processes a member's due payment.
     * Uses CustomUserDetails to ensure the user can only pay for their own account.
     */
    @PostMapping("/pay-due")
    public ResponseEntity<String> payDue(
            @RequestBody TransactionRequest request,
            Authentication authentication) {

        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

        memberFinanceService.processMemberDuesPayment(request, user.getUserId(), user.getClubId());
        
        return ResponseEntity.ok("Dues payment processed successfully.");
    }

    /**
     * Endpoint for members to upload reimbursement requests.
     */
    @PostMapping(value = "/reimbursement-request", consumes = "multipart/form-data")
    public ResponseEntity<String> requestReimbursement(
            @ModelAttribute MemberReimbursementRequest request,
            Authentication authentication) {

        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

        memberFinanceService.memberReimbursementRequest(
                user.getUserId(),
                user.getClubId(),
                request.getAmount(),
                request.getCategory(),
                request.getDescription(),
                request.getPurchaseDate(),
                request.getReceiptImageFile()
        );

        return ResponseEntity.ok("Reimbursement request submitted successfully.");
    }
    @PostMapping("/save-donation")
    public ResponseEntity<String> saveDonationTransaction(
            @RequestBody TransactionRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {

        if (user == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Unauthorized");
        }

        return memberFinanceService.saveDonationTransaction(
                user.getUserId(),
                user.getClubId(),
                request
        );
    }


    @GetMapping("/member-transactions")
    public ResponseEntity<MembersTransactionsResponse> getMemberTransactions(
            @AuthenticationPrincipal CustomUserDetails user) {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Delegate to service to fetch transactions
        return memberFinanceService.getMemberTransactions(user.getUserId(), user.getClubId());
    }

    @GetMapping("/member-due")
    public ResponseEntity<MemberDuesResponse> getMemberDue(
            @AuthenticationPrincipal CustomUserDetails user) {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Delegate to service to fetch transactions
        return memberFinanceService.getMemberDue(user.getUserId(), user.getClubId());
    }

    @GetMapping("/recent-member-due")
    public ResponseEntity<List<RecentDuesResponse>> getRecentMemberDue(
            @AuthenticationPrincipal CustomUserDetails user) {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return memberFinanceService.getRecentMemberDue(
                user.getUserId(),
                user.getClubId()
        );
    }

    @PostMapping("/due-receipt-download")
    public ResponseEntity<Resource> dueReceiptDownload(
            @RequestBody TransactionRequest request, @AuthenticationPrincipal CustomUserDetails user
    ) {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return memberFinanceService.dueReceiptDownload(
                user.getUserId(),
                user.getClubId(),
                request.getDueId()
        );
    }

    @GetMapping("/club-budget-categories")
    public ResponseEntity<ClubCategoriesResponse> getClubBudgetCategories(
            @AuthenticationPrincipal CustomUserDetails user) {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Delegate to service to fetch transactions
        return memberFinanceService.getClubBudgetCategories(user.getClubId());
    }

}