package com.exchkr.club.management.services.Impl;

import com.exchkr.club.management.dao.MemberDuesRepository;
import com.exchkr.club.management.dao.ReimbursementRepository;
import com.exchkr.club.management.dao.TransactionRepository;
import com.exchkr.club.management.model.api.request.TransactionRequest;
import com.exchkr.club.management.model.api.response.ClubCategoriesResponse;
import com.exchkr.club.management.model.api.response.MembersTransactionsResponse;
import com.exchkr.club.management.model.api.response.MemberDuesResponse;
import com.exchkr.club.management.model.api.response.RecentDuesResponse;
import com.exchkr.club.management.model.entity.ClubTransaction;
import com.exchkr.club.management.model.entity.MemberDue;
import com.exchkr.club.management.services.MemberFinanceService;
import jakarta.transaction.Transactional;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MemberFinanceServiceImpl implements MemberFinanceService {

    private static final Logger logger = LoggerFactory.getLogger(MemberFinanceServiceImpl.class);


    private final ReimbursementRepository reimbursementRepository;
    private final TransactionRepository transactionRepository;
    private final MemberDuesRepository duesRepository;


    public MemberFinanceServiceImpl(ReimbursementRepository reimbursementRepository, TransactionRepository transactionRepository, MemberDuesRepository duesRepository) {
        this.reimbursementRepository = reimbursementRepository;
        this.transactionRepository = transactionRepository;
        this.duesRepository = duesRepository;

    }

    @Value("${files.exchkr-dir}")
    private String basePath;
    
    @Value("${platform.fee.percentage}")
	private double platformFeePercent;

	@Value("${stripe.fee.card.percentage}")
	private double stripeFeePercent;

	@Value("${stripe.fee.card.fixed}")
	private double stripeFeeFixedCents;
    
    
 // below method helps to calculate fees 
 	private Map<String, BigDecimal> calculateFees(BigDecimal netAmount) {
 	    Map<String, BigDecimal> results = new HashMap<>();
 	    
 	    if (netAmount.compareTo(BigDecimal.ZERO) <= 0) {
 	        results.put("gross", BigDecimal.ZERO);
 	        results.put("platform", BigDecimal.ZERO);
 	        results.put("stripe", BigDecimal.ZERO);
 	        results.put("totalFees", BigDecimal.ZERO);
 	        return results;
 	    }

 	    // 1. Calculate Gross Total
 	    double pPercent = platformFeePercent / 100.0;
 	    double sPercent = stripeFeePercent / 100.0;
 	    double sFixed = stripeFeeFixedCents / 100.0; 
 	    double totalPercentage = pPercent + sPercent;

 	    double grossDouble = (netAmount.doubleValue() + sFixed) / (1 - totalPercentage);
 	    BigDecimal grossAmount = BigDecimal.valueOf(grossDouble).setScale(2, RoundingMode.HALF_UP);

 	    // 2. Derive Fees from the Gross Amount
 	    BigDecimal platformFee = grossAmount.multiply(BigDecimal.valueOf(pPercent))
 	                                        .setScale(2, RoundingMode.HALF_UP);
 	                                        
 	    BigDecimal stripeFee = grossAmount.multiply(BigDecimal.valueOf(sPercent))
 	                                      .add(BigDecimal.valueOf(sFixed))
 	                                      .setScale(2, RoundingMode.HALF_UP);

 	    // 3. Total sum of both fees
 	    BigDecimal totalFees = platformFee.add(stripeFee);

 	    results.put("gross", grossAmount);
 	    results.put("platform", platformFee);
 	    results.put("stripe", stripeFee);
 	    results.put("totalFees", totalFees);

 	    return results;
 	}


    /**
     * Processes a member's payment for a specific due record.
     */
 	@Override
 	@Transactional
 	public void processMemberDuesPayment(TransactionRequest request, Long userId, Long clubId) {
 	    // 1. Validate
 	    MemberDue due = duesRepository.findById(request.getDueId())
 	            .filter(d -> d.getAssignedUserId().equals(userId) && d.getClubId().equals(clubId))
 	            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied"));

 	    // 2. Idempotency Check
 	    ClubTransaction tx = transactionRepository.findByDueIdAndStripeRefId(request.getDueId(), request.getStripePaymentIntentId())
 	            .orElseGet(ClubTransaction::new);

 	    if ("Completed".equalsIgnoreCase(tx.getStatus())) {
 	        logger.info("Transaction {} already completed. Skipping.", request.getStripePaymentIntentId());
 	        return;
 	    }

 	    // 3. NEW: Calculate Fees Snapshot
 	    // Use the base amount from the due record to derive the fees
 	    Map<String, BigDecimal> feeData = calculateFees(due.getTotalAmount());

 	    // 4. Set Ledger Details
 	    tx.setClubId(clubId);
 	    tx.setDoneByUserId(userId);
 	    tx.setDueId(request.getDueId());
 	    tx.setTransDate(java.time.Instant.now());
 	    tx.setDescription(due.getDescription() != null ? due.getDescription() : "Dues Payment");
 	    tx.setCategory("Dues");
 	    tx.setType("Income");
 	    tx.setAmount(request.getAmount());
 	    
 	    // Set the specific fee columns
 	    tx.setPlatformFees(feeData.get("platform"));
 	    tx.setPaymentGatewayServiceCharge(feeData.get("stripe"));
 	    
 	    tx.setStatus(request.getPaymentStatus() != null ? request.getPaymentStatus() : "Processing");
 	    tx.setStripeRefId(request.getStripePaymentIntentId());

 	    transactionRepository.save(tx);

 	    due.setStatus(tx.getStatus());
 	    duesRepository.save(due);

 	    logger.info("Transaction saved with Platform Fee: {} and Stripe Fee: {}", 
 	                tx.getPlatformFees(), tx.getPaymentGatewayServiceCharge());
 	}



    @Override
    @Transactional
    public void memberReimbursementRequest(Long userId,
                                           Long clubId,
                                           BigDecimal amount,
                                           String category,
                                           String description,
                                           String purchaseDate,
                                           MultipartFile receiptImageFile) {


        // Ensure upload folder exists
        File directory = new File(basePath);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create receipt folder");
        }

        // File processing
        String originalFileName = receiptImageFile.getOriginalFilename();
        if (originalFileName == null || !originalFileName.contains(".")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file name");
        }
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String systemFileName = System.currentTimeMillis() + extension;
        Path filePath = Paths.get(basePath, systemFileName);

        try {
            receiptImageFile.transferTo(filePath);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save file");
        }

        // Parse purchase date
        LocalDate purchaseLocalDate;
        try {
            purchaseLocalDate = LocalDate.parse(purchaseDate);
        } catch (Exception ex) {
            // Inline rollback: delete the file if date parsing fails
            try { Files.deleteIfExists(filePath); } catch (Exception ignored) {}
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid purchase date format");
        }

        // Insert reimbursement record
        try {
            reimbursementRepository.memberReimbursementRequest( clubId, userId, originalFileName, systemFileName, amount, purchaseLocalDate, category, description);
        } catch (Exception ex) {
            // Inline rollback: delete the file if DB insert fails
            try { Files.deleteIfExists(filePath); } catch (Exception ignored) {}
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save reimbursement record");
        }
    }




    @Override
    @Transactional
    public ResponseEntity<String> saveDonationTransaction(
            Long userId,
            Long clubId,
            TransactionRequest request
    ) {
        try {
            int rows = transactionRepository.saveDonationTransaction(
                    userId,
                    clubId,
                    request.getDescription(),
                    request.getCategory(),
                    request.getAmount(),
                    request.getStripePaymentIntentId(),
                    request.getPaymentStatus(),
                    request.getPlatformFee(),
                    request.getStripeFee()
            );

            if (rows == 1) {
                return ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body("Donation transaction created successfully");
            }

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Donation transaction creation failed");

        } catch (Exception ex) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error while creating donation transaction");
        }
    }


    @Override
    @Transactional
    public ResponseEntity<MembersTransactionsResponse> getMemberTransactions(
            Long userId,
            Long clubId
    ) {
        try {

            List<Object[]> rows = transactionRepository.getMemberTransactions(userId, clubId);
            List<MembersTransactionsResponse.Transaction> transactions = new ArrayList<>();

            if (rows != null && !rows.isEmpty()) {
                for (Object[] row : rows) {

                    Instant instant = (Instant) row[4];
                    String transDate = instant.atZone(ZoneId.systemDefault())
                            .toLocalDate()
                            .toString();

                    MembersTransactionsResponse.Transaction tx =
                            new MembersTransactionsResponse.Transaction(
                                    ((Number) row[0]).longValue(), // trans_id
                                    (String) row[1],               // category
                                    (BigDecimal) row[2],           // amount
                                    (String) row[3],               // status
                                    transDate,                     // trans_date
                                    (String) row[5],               // description
                                    row[6] != null                 // due_id
                                            ? ((Number) row[6]).longValue()
                                            : null
                            );

                    transactions.add(tx);
                }
            }

            MembersTransactionsResponse response =
                    new MembersTransactionsResponse(transactions);

            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @Transactional
    public ResponseEntity<MemberDuesResponse> getMemberDue(Long userId, Long clubId) {
        try {
            // Fetch the row (native query returns List<Object[]>)
            List<Object[]> rows = duesRepository.getMemberDue(clubId, userId);

            MemberDuesResponse response;

            if (rows.isEmpty()) {
                // No record found, return totalAmount = 0
                response = new MemberDuesResponse();
                response.setTotalAmount(BigDecimal.ZERO);
            } else {
                Object[] row = rows.get(0);
                Long dueId = row[0] != null ? ((Number) row[0]).longValue() : null;
                String description = row[1] != null ? row[1].toString() : null;
                BigDecimal totalAmount = row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO;

                response = new MemberDuesResponse(dueId, description, totalAmount);
            }

            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            // Return 500 on unexpected errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @Transactional
    public ResponseEntity<List<RecentDuesResponse>> getRecentMemberDue(Long userId, Long clubId) {

        try {
            List<Object[]> rows = duesRepository.getRecentMemberDue(clubId, userId);

            if (rows == null || rows.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }

            List<RecentDuesResponse> responseList = new ArrayList<>();

            for (Object[] row : rows) {
                RecentDuesResponse response = new RecentDuesResponse();

                response.setDueId(
                        row[0] != null ? ((Number) row[0]).longValue() : null
                );
                response.setDescription(
                        row[1] != null ? row[1].toString() : null
                );
                response.setTotalAmount(
                        row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO
                );
                response.setDueDate(
                        row[3] != null ? (LocalDate) row[3] : null
                );

                responseList.add(response);
            }

            return ResponseEntity.ok(responseList);

        } catch (Exception ex) {
            return ResponseEntity.ok(Collections.emptyList());
        }
    }


    @Override
    @Transactional
    public ResponseEntity<Resource> dueReceiptDownload(
            Long userId,
            Long clubId,
            Long dueId
    ) {

        List<String> results = duesRepository.dueReceiptDownload(userId, clubId, dueId);

        if (results == null || results.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Invoice receipt not found"
            );
        }

        // Only one invoice_file_name expected
        String invoiceFileName = results.get(0);

        Path filePath = Paths.get(basePath, invoiceFileName);

        if (!Files.exists(filePath)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Invoice file not found in system"
            );
        }

        Resource resource;
        try {
            resource = new UrlResource(filePath.toUri());
        } catch (MalformedURLException ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to load invoice file"
            );
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + invoiceFileName + "\""
                )
                .body(resource);
    }

    @Override
    public ResponseEntity<ClubCategoriesResponse> getClubBudgetCategories(Long clubId) {
        try {

            List<Object[]> rows = reimbursementRepository.getClubBudgetCategories(clubId);
            List<ClubCategoriesResponse.Category> categories = new ArrayList<>();

            if (rows != null && !rows.isEmpty()) {
                for (Object[] row : rows) {

                    ClubCategoriesResponse.Category category =
                            new ClubCategoriesResponse.Category(
                                    ((Number) row[0]).longValue(), // category_id
                                    (String) row[1]                // category_name
                            );

                    categories.add(category);
                }
            }

            ClubCategoriesResponse response = new ClubCategoriesResponse(categories);

            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


}