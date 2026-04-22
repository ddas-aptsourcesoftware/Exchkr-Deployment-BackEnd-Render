package com.exchkr.club.management.controller;

import com.exchkr.club.management.model.api.request.*;
import com.exchkr.club.management.model.api.response.BudgetSummaryResponse;
import com.exchkr.club.management.model.api.response.CategorySpendingResponse;
import com.exchkr.club.management.model.api.response.MonthlySpendingResponse;
import com.exchkr.club.management.model.api.response.PendingActionsResponse;
import com.exchkr.club.management.model.api.response.RecentActivityResponse;
import com.exchkr.club.management.model.api.response.ReimbursementListResponse;
import com.exchkr.club.management.model.dto.FinanceSummaryDTO;
import com.exchkr.club.management.model.dto.MemberDuesDTO;
import com.exchkr.club.management.model.entity.BudgetCategoryMaster;
import com.exchkr.club.management.model.entity.ClubTransaction;
import com.exchkr.club.management.security.CustomUserDetails; 
import com.exchkr.club.management.services.ClubFinanceService;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/club/finance")
public class ClubFinanceController {

    private final ClubFinanceService clubFinanceService;

    public ClubFinanceController(ClubFinanceService clubFinanceService) {
        this.clubFinanceService = clubFinanceService;
    }

    @PostMapping("/record-success")
    public ResponseEntity<String> recordSuccess(
            @RequestBody TransactionRequest request, 
            Authentication authentication) {
        
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        
        clubFinanceService.recordSuccessfulExpense(request, user.getUserId(), user.getClubId());
        return ResponseEntity.ok().build();
    }

    /**
     * Fetches paged transaction history.
     * Replaces the old List<ClubTransaction> version.
     */
    @GetMapping("/history")
    public ResponseEntity<Page<ClubTransaction>> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        Page<ClubTransaction> result = clubFinanceService.getPagedHistory(user.getClubId(), page, size);
        
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/create-invoice")
    public ResponseEntity<Void> createInvoice(
            @RequestBody @Validated CreateInvoiceRequest request, 
            Authentication authentication) {
        
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        
        clubFinanceService.createInvoice(request, user.getUserId(), user.getClubId());
        
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    
    @GetMapping("/member-dues")
    public ResponseEntity<Page<MemberDuesDTO>> getMemberDues(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            Authentication authentication) {
        
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        Page<MemberDuesDTO> result = clubFinanceService.getPagedDues(user.getClubId(), page, size);
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/dues-summary")
    public ResponseEntity<Map<String, Object>> getDuesSummary(Authentication authentication) {
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(clubFinanceService.getDuesSummaryMetrics(user.getClubId()));
    }
    
    @GetMapping("/finance-summary")
    public ResponseEntity<FinanceSummaryDTO> getFinanceSummary(Authentication authentication) {
    	CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        Long clubId = user.getClubId(); 
        
        FinanceSummaryDTO summary = clubFinanceService.getFinanceSummary(clubId);
        return ResponseEntity.ok(summary);
    }

    
    @PostMapping("/member-dues/remind")
    public ResponseEntity<Map<String, String>> sendReminder(
            @Valid @RequestBody DueReminderRequest request,
            Authentication authentication) {
        
    	CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        Long clubId = user.getClubId();
        
        clubFinanceService.sendDueReminder(clubId, request);
        
        return ResponseEntity.ok(Map.of(
            "message", "Reminder email successfully sent to " + request.memberId()
        ));
    }
    
    @PostMapping("/member-dues/remind-bulk")
    public ResponseEntity<Map<String, Object>> sendBulkReminders(
            @Valid @RequestBody List<DueReminderRequest> requests,
            Authentication authentication) {
        
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        Long clubId = user.getClubId();
        
        int sentCount = clubFinanceService.sendBulkDueReminders(clubId, requests);
        
        return ResponseEntity.ok(Map.of(
            "message", String.format("Successfully sent %d reminder(s)", sentCount),
            "totalRequested", requests.size()
        ));
    }
    
    
    @PostMapping("/download-dues-pdf")
    public ResponseEntity<byte[]> downloadDuesPdf(
            @RequestBody DuesReportRequest request, 
            Authentication authentication) {
        
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        
        // Default to "All" if no status is provided in the payload
        String statusFilter = (request.status() != null) ? request.status() : "All";
        
        byte[] pdfContent = clubFinanceService.generateDuesPdf(user.getUserId(), user.getClubId(), statusFilter);

        String filename = String.format("dues_report_%s.pdf", statusFilter.toLowerCase());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfContent);
    }
    
    
    @PostMapping("/download-trans-pdf")
    public ResponseEntity<byte[]> downloadTransactionPdf(
            @RequestBody TransactionReportRequest request, 
            Authentication authentication) {
        
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        
        byte[] pdfContent = clubFinanceService.generateTransactionPdf(
        		user.getUserId(),
                user.getClubId(), 
                request.fromDate(), 
                request.toDate()    
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=transactions.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfContent);
    }


    @GetMapping("/reimbursement-request-list")
    public ResponseEntity<List<ReimbursementListResponse>> reimbursementRequestList(
            @AuthenticationPrincipal CustomUserDetails user) {

        List<ReimbursementListResponse> reimbursements =
        		clubFinanceService.reimbursementRequestList(user.getClubId());

        return ResponseEntity.ok(reimbursements);
    }

    @PostMapping("/reimbursement-request-reject")
    public ResponseEntity<Void> reimbursementRequestReject(
            @RequestBody ReimbursementRejectRequest request, @AuthenticationPrincipal CustomUserDetails user) {

    	clubFinanceService.reimbursementRequestReject(
                user.getUserId(),
                user.getClubId(),
                request.getReimbursementId(),
                request.getRejectReason()
        );

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reimbursement-receipt-download")
    public ResponseEntity<Resource> reimbursementReceiptDownload(
            @RequestBody ReimbursementReceiptDownloadRequest request, @AuthenticationPrincipal CustomUserDetails user
    ) {

        return clubFinanceService.reimbursementReceiptDownload(
                user.getUserId(),
                user.getClubId(),
                request.getReimbursementId()
        );
    }

    @PostMapping("/reimbursement-request-approve")
    public ResponseEntity<Void> reimbursementRequestApprove(
            @RequestBody ReimbursementRejectRequest request, @AuthenticationPrincipal CustomUserDetails user) {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        clubFinanceService.reimbursementRequestApprove(
                user.getUserId(),
                user.getClubId(),
                request.getReimbursementId(),
                request.getStripeRefId()
        );

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pending-actions")
    public ResponseEntity<List<PendingActionsResponse>> getPendingActions(
            @AuthenticationPrincipal CustomUserDetails user) {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<PendingActionsResponse> pendingActions =
                clubFinanceService.getPendingActions(user.getClubId());

        return ResponseEntity.ok(pendingActions);
    }

    @GetMapping("/recent-activity")
    public ResponseEntity<List<RecentActivityResponse>> getRecentActivity(
            @AuthenticationPrincipal CustomUserDetails user) {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<RecentActivityResponse> recentActivity =
                clubFinanceService.getRecentActivity(user.getClubId());

        return ResponseEntity.ok(recentActivity);
    }


    @GetMapping("/spending-by-category")
    public ResponseEntity<List<CategorySpendingResponse>> getSpendingByCategory(Authentication authentication) {
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        List<CategorySpendingResponse> spending = clubFinanceService.getSpendingByCategory(user.getClubId());
        return ResponseEntity.ok(spending);
    }
    
    
    @GetMapping("/monthly-spending")
    public ResponseEntity<List<MonthlySpendingResponse>> getMonthlySpending(Authentication authentication) {
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        List<MonthlySpendingResponse> trend = clubFinanceService.getMonthlySpendingTrend(user.getClubId());
        return ResponseEntity.ok(trend);
    }
    
    
    /**
     * Creates new master categories for a club.
     * Supports single or multiple categories (comma-separated list sent from UI).
     */
    @PostMapping("/budget-categories")
    public ResponseEntity<Void> createCategories(
            @RequestBody List<String> categories, 
            Authentication authentication) {
        
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        
        clubFinanceService.saveMasterCategories(user.getClubId(), categories);
        
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Retrieves the master category list for the club to populate UI dropdowns.
     */
    @GetMapping("/budget-categories")
    public ResponseEntity<List<BudgetCategoryMaster>> getMasterCategories(Authentication authentication) {
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        
        // This returns the full list of category names and their IDs for the specific club
        List<BudgetCategoryMaster> categories = clubFinanceService.getBudgetCategoryMasterList(user.getClubId());
        
        return ResponseEntity.ok(categories);
    }
    
    
    /**
     * Set up the annual budget and category allocations for the club.
     */
    @PostMapping("/budget-setup")
    public ResponseEntity<Void> setupBudget(
            @RequestBody @Valid BudgetSetupRequest request,
            Authentication authentication) {
        
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        
        // Passing the request, officer ID, and club ID to the service
        clubFinanceService.saveBudget(user.getClubId(), request, user.getUserId());
        
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    
    
    /**
     * Update the annual budget and category allocations for the club.
     */
    @PatchMapping("/budget")
    public ResponseEntity<Void> updateBudget(
            @RequestBody BudgetPatchRequest request,
            Authentication authentication) {
        
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        clubFinanceService.patchBudget(user.getClubId(), request, user.getUserId());
        
        return ResponseEntity.ok().build();
    }

    /**
     * Retrieves the current budget configuration and spending progress.
     */
    @GetMapping("/budget-summary")
    public ResponseEntity<BudgetSummaryResponse> getBudgetSummary(Authentication authentication) {
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        
        BudgetSummaryResponse summary = clubFinanceService.getBudgetSummary(user.getClubId());
        return ResponseEntity.ok(summary);
    }

}