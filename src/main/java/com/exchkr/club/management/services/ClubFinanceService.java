package com.exchkr.club.management.services;

import com.exchkr.club.management.model.api.request.BudgetPatchRequest;
import com.exchkr.club.management.model.api.request.BudgetSetupRequest;
import com.exchkr.club.management.model.api.request.CreateInvoiceRequest;
import com.exchkr.club.management.model.api.request.DueReminderRequest;
import com.exchkr.club.management.model.api.request.TransactionRequest;
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
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page; 
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map; 

public interface ClubFinanceService {

    /**
     * Retrieves the paginated financial history for a specific club using the Entity.
     */
    Page<ClubTransaction> getPagedHistory(Long clubId, int page, int size);

    /**
     * Records an expense.
     */
    void recordSuccessfulExpense(TransactionRequest request, Long userId, Long clubId); 
    
    /**
     * Updates an existing transaction's status.
     */
    void updateTransactionStatus(String stripeId, String newStatus);
    
    List<ReimbursementListResponse> reimbursementRequestList(Long clubId);
    void reimbursementRequestReject(Long userId, Long clubId, Long reimbursementId, String rejectReason);
    ResponseEntity<Resource> reimbursementReceiptDownload(Long userId, Long clubId, Long reimbursementId);
    void reimbursementRequestApprove(Long userId, Long clubId, Long reimbursementId, String stripeRefId);

    /**
     * Retrieves paginated dues dashboard data.
     */
    Page<MemberDuesDTO> getPagedDues(Long clubId, int page, int size);

    /**
     * Calculates summary metrics for the dashboard cards.
     */
    Map<String, Object> getDuesSummaryMetrics(Long clubId);

    /**
     * Creates an invoice and corresponding member due records.
     */
    void createInvoice(CreateInvoiceRequest request, Long officerId, Long clubId);
    
    /**
     * Calculates summary metrics for the finance cards.
     */
    FinanceSummaryDTO getFinanceSummary(Long clubId);
    
    /**
     * Send due reminder to member.
     */
    void sendDueReminder(Long clubId, DueReminderRequest request);
    
    /**
     * Send bulk due reminders.
     */
    int sendBulkDueReminders(Long clubId, List<DueReminderRequest> requests);
    
    List<PendingActionsResponse> getPendingActions(Long clubId);

    /**
     * Retrieves the most recent activity (Dashboard view).
     */
    List<RecentActivityResponse> getRecentActivity(Long clubId);

    /**
     * Legacy method for fetching full history as a list 
     */
    List<ClubTransaction> getClubHistory(Long clubId);
    
    /**
     * generate Transaction history pdf and then sent back to front end to download
     */
    byte[] generateTransactionPdf(Long officerId, Long clubId, java.time.Instant fromDate, java.time.Instant toDate);
    
    /**
     * to get the spending data by reimbursement categories
     */
    List<CategorySpendingResponse> getSpendingByCategory(Long clubId);
    
    /**
     * to get the monthly expenses data of the club
     */
    List<MonthlySpendingResponse> getMonthlySpendingTrend(Long clubId);
    
    void saveMasterCategories(Long clubId, List<String> categoryNames);

    List<BudgetCategoryMaster> getBudgetCategoryMasterList(Long clubId);
    
    void saveBudget(Long clubId, BudgetSetupRequest request, Long userId);
    void patchBudget(Long clubId, BudgetPatchRequest request, Long userId);
    BudgetSummaryResponse getBudgetSummary(Long clubId);
    
    
    /**
     * Retrieves a non-paginated list of dues filtered by status.
     */
    List<MemberDuesDTO> getDuesByStatus(Long clubId, String status);

    /**
     * Generates a PDF report of member dues.
     */
    byte[] generateDuesPdf(Long officerId, Long clubId, String status);
}