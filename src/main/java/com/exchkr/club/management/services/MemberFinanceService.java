package com.exchkr.club.management.services;

import com.exchkr.club.management.model.api.request.TransactionRequest;
import java.math.BigDecimal;
import java.util.List;

import com.exchkr.club.management.model.api.response.ClubCategoriesResponse;
import com.exchkr.club.management.model.api.response.MemberDuesResponse;
import com.exchkr.club.management.model.api.response.MembersTransactionsResponse;
import com.exchkr.club.management.model.api.response.RecentDuesResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface MemberFinanceService {


    /**
     * Updated to accept the request DTO which contains dueId and stripe details.
     */
    void processMemberDuesPayment(TransactionRequest request, Long userId, Long clubId);

    void memberReimbursementRequest(Long userId, Long clubId, BigDecimal amount, 
                                    String category, String description, 
                                    String purchaseDate, MultipartFile receiptImageFile);

    ResponseEntity<String> saveDonationTransaction(
            Long userId,
            Long clubId,
            TransactionRequest request
    );

    ResponseEntity<MembersTransactionsResponse> getMemberTransactions(Long userId, Long clubId);

    ResponseEntity<MemberDuesResponse> getMemberDue(Long userId, Long clubId);

    ResponseEntity<List<RecentDuesResponse>> getRecentMemberDue(Long userId, Long clubId);

    ResponseEntity<Resource> dueReceiptDownload(Long userId, Long clubId, Long dueId);

    ResponseEntity<ClubCategoriesResponse> getClubBudgetCategories(Long clubId);
}