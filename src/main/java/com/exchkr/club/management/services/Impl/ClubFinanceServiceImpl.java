package com.exchkr.club.management.services.Impl;

import com.exchkr.club.management.dao.BudgetCategoryMasterRepository;
import com.exchkr.club.management.dao.ClubBudgetRepository;
import com.exchkr.club.management.dao.InvoiceDetailRepository;
import com.exchkr.club.management.dao.InvoiceHeaderRepository;
import com.exchkr.club.management.dao.InvoiceMemberMappingRepository;
import com.exchkr.club.management.dao.MemberDuesRepository;
import com.exchkr.club.management.dao.ReimbursementRepository;
import com.exchkr.club.management.dao.TransactionRepository;
import com.exchkr.club.management.dao.UserClubMembershipProjection;
import com.exchkr.club.management.dao.UserClubRepository;
import com.exchkr.club.management.dao.UserRepository;
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
import com.exchkr.club.management.model.entity.BudgetCategory;
import com.exchkr.club.management.model.entity.BudgetCategoryMaster;
import com.exchkr.club.management.model.entity.ClubBudget;
import com.exchkr.club.management.model.entity.ClubTransaction;
import com.exchkr.club.management.model.entity.InvoiceDetail;
import com.exchkr.club.management.model.entity.InvoiceHeader;
import com.exchkr.club.management.model.entity.InvoiceMemberMapping;
import com.exchkr.club.management.model.entity.MemberDue;
import com.exchkr.club.management.model.entity.User;
import com.exchkr.club.management.services.ClubFinanceService;
import com.exchkr.club.management.services.EmailService;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.ByteArrayOutputStream;
import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Element;
import com.lowagie.text.Chunk;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfWriter;

import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ClubFinanceServiceImpl implements ClubFinanceService {

	private static final Logger logger = LoggerFactory.getLogger(ClubFinanceServiceImpl.class);

	private final TransactionRepository transactionRepository;
	private final MemberDuesRepository duesRepository;
	private final ReimbursementRepository reimbursementRepository;
	private final InvoiceHeaderRepository headerRepo;
	private final InvoiceDetailRepository detailRepo;
	private final InvoiceMemberMappingRepository mappingRepo;
	private final UserRepository userRepository;
	private final UserClubRepository userClubRepository;
	private final EmailService emailService;
	private final BudgetCategoryMasterRepository masterRepository;
	private final ClubBudgetRepository budgetRepository;

	public ClubFinanceServiceImpl(TransactionRepository transactionRepository, MemberDuesRepository duesRepository,
			ReimbursementRepository reimbursementRepository, InvoiceHeaderRepository headerRepo,
			InvoiceDetailRepository detailRepo, InvoiceMemberMappingRepository mappingRepo,
			UserRepository userRepository, UserClubRepository userClubRepository, EmailService emailService,
			BudgetCategoryMasterRepository masterRepository, ClubBudgetRepository budgetRepository) {
		this.transactionRepository = transactionRepository;
		this.duesRepository = duesRepository;
		this.reimbursementRepository = reimbursementRepository;
		this.headerRepo = headerRepo;
		this.detailRepo = detailRepo;
		this.mappingRepo = mappingRepo;
		this.userRepository = userRepository;
		this.userClubRepository = userClubRepository;
		this.emailService = emailService;
		this.masterRepository = masterRepository;
		this.budgetRepository = budgetRepository;

	}

	@Value("${files.exchkr-dir}")
	private String basePath;

	@Value("${platform.fee.percentage}")
	private double platformFeePercent;

	@Value("${stripe.fee.card.percentage}")
	private double stripeFeePercent;

	@Value("${stripe.fee.card.fixed}")
	private double stripeFeeFixedCents;

	/**
	 * Records a manual or successful expense directly to the ledger. Identity and
	 * Club context are provided by the Security Principal.
	 */
	@Override
	@Transactional
	public void recordSuccessfulExpense(TransactionRequest request, Long userId, Long clubId) {
		if (request.getStripePaymentIntentId() != null
				&& transactionRepository.findByStripeRefId(request.getStripePaymentIntentId()).isPresent()) {
			logger.warn("Transaction with Stripe ID {} already recorded.", request.getStripePaymentIntentId());
			return;
		}

		ClubTransaction tx = new ClubTransaction();
		tx.setClubId(clubId);
		tx.setDoneByUserId(userId);
		tx.setTransDate(Instant.now());
		tx.setDescription(request.getDescription());
		tx.setCategory(request.getCategory());
		tx.setType("Expense");
		tx.setAmount(request.getAmount());
		tx.setStatus(request.getPaymentStatus());
		tx.setStripeRefId(request.getStripePaymentIntentId());
		tx.setPaidToUserId(request.getRecipientId());
		tx.setPlatformFees(request.getPlatformFee());
		tx.setPaymentGatewayServiceCharge(request.getStripeFee());

		// Save will trigger PostgreSQL to generate the ID
		transactionRepository.save(tx);
	}

	/**
	 * Fetches transaction history for a specific club context. Secure because
	 * clubId is retrieved from the signed JWT.
	 */

	/**
	 * 
	 * Fetches complete transaction history for a specific club context.
	 * 
	 */

	@Override

	public List<ClubTransaction> getClubHistory(Long clubId) {

		return transactionRepository.findAllByClubIdOrderByTransDateDesc(clubId);

	}

	/**
	 * 
	 * Fetches paged transaction history for a specific club context.
	 * 
	 */

	@Override
	public Page<ClubTransaction> getPagedHistory(Long clubId, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		long offset = pageable.getOffset();

		// 1. Fetch raw rows
		List<Map<String, Object>> rawRows = transactionRepository.fetchTransactionsRaw(clubId, size, offset);
		long totalCount = transactionRepository.countTransactionsByClub(clubId);

		// 2. Map raw rows to ClubTransaction entity
		List<ClubTransaction> transactions = rawRows.stream().map(row -> {
			ClubTransaction t = new ClubTransaction();

			t.setTransId(row.get("transId") != null ? ((Number) row.get("transId")).longValue() : null);
			t.setClubId(row.get("clubId") != null ? ((Number) row.get("clubId")).longValue() : null);
			t.setAmount(row.get("amount") != null ? new java.math.BigDecimal(row.get("amount").toString())
					: java.math.BigDecimal.ZERO);
			t.setType((String) row.get("type"));
			t.setCategory((String) row.get("category"));
			t.setDescription((String) row.get("description"));
			t.setStatus((String) row.get("status"));
			t.setStripeRefId((String) row.get("stripeRefId"));
			t.setDoneByUserId(row.get("doneByUserId") != null ? ((Number) row.get("doneByUserId")).longValue() : null);
			t.setPaidToUserId(row.get("paidToUserId") != null ? ((Number) row.get("paidToUserId")).longValue() : null);
			t.setDueId(row.get("dueId") != null ? ((Number) row.get("dueId")).longValue() : null);

			Object dateObj = row.get("transDate");
			if (dateObj != null) {
				if (dateObj instanceof java.sql.Timestamp ts) {
					t.setTransDate(ts.toInstant());
				} else if (dateObj instanceof java.time.Instant inst) {
					t.setTransDate(inst);
				} else if (dateObj instanceof java.time.LocalDateTime ldt) {
					t.setTransDate(ldt.atZone(java.time.ZoneId.systemDefault()).toInstant());
				}
			}

			return t;
		}).toList();

		return new org.springframework.data.domain.PageImpl<>(transactions, pageable, totalCount);
	}

	@Override
	public byte[] generateTransactionPdf(Long officerId, Long clubId, Instant fromDate, Instant toDate) {

		// Convert toDate to the start of the next day to include all transactions on
		// toDate
		Instant toDatePlusOne = toDate.atZone(ZoneId.systemDefault()).toLocalDate().plusDays(1)
				.atStartOfDay(ZoneId.systemDefault()).toInstant();

		// Fetch transactions
		List<ClubTransaction> transactions = transactionRepository.findTransactionsByDateRangeInclusive(clubId,
				fromDate, toDatePlusOne);

		String clubName = userClubRepository.findMembershipDetail(officerId, clubId)
				.map(UserClubMembershipProjection::getClubName).orElse("Exchkr Club");

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Document document = new Document(PageSize.A4);

		try {
			PdfWriter.getInstance(document, out);
			document.open();

			// Fonts
			Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
			Font fontClubName = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
			Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
			Font rowFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

			// Header Table
			PdfPTable headerTable = new PdfPTable(2);
			headerTable.setWidthPercentage(100);
			headerTable.setSpacingAfter(10f);

			PdfPCell leftCell = new PdfPCell();
			leftCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
			try {
				java.io.InputStream is = getClass().getResourceAsStream("/static/images/EXCHKR.png");
				if (is != null) {
					com.lowagie.text.Image logo = com.lowagie.text.Image.getInstance(is.readAllBytes());
					logo.scaleToFit(100, 50);
					leftCell.addElement(logo);
				} else {
					leftCell.addElement(new Paragraph(clubName, fontClubName));
				}
			} catch (Exception e) {
				logger.warn("Could not load logo: {}. Using club name text.", e.getMessage());
				leftCell.addElement(new Paragraph(clubName, fontClubName));
			}
			headerTable.addCell(leftCell);

			PdfPCell rightCell = new PdfPCell(new Phrase("Transaction Report", fontTitle));
			rightCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
			rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			rightCell.setVerticalAlignment(Element.ALIGN_BOTTOM);
			headerTable.addCell(rightCell);

			document.add(headerTable);

			// Period / Subtitle
			DateTimeFormatter displayFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
					.withZone(ZoneId.systemDefault());
			Paragraph period = new Paragraph(
					"Period: " + displayFormat.format(fromDate) + " to " + displayFormat.format(toDate));
			period.setAlignment(Element.ALIGN_RIGHT);
			document.add(period);
			document.add(new Paragraph(" ")); // Spacer

			// Main Transaction Table
			PdfPTable table = new PdfPTable(new float[] { 15, 30, 15, 12, 13, 15 });
			table.setWidthPercentage(100);

			String[] headers = { "Date", "Description", "Category", "Type", "Amount", "Status" };
			for (String header : headers) {
				PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
				cell.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setPadding(5);
				table.addCell(cell);
			}

			// Data Rows
			for (ClubTransaction t : transactions) {
				table.addCell(new Phrase(displayFormat.format(t.getTransDate()), rowFont));
				table.addCell(new Phrase(t.getDescription() != null ? t.getDescription() : "", rowFont));
				table.addCell(new Phrase(t.getCategory() != null ? t.getCategory() : "", rowFont));

				PdfPCell typeCell = new PdfPCell(new Phrase(t.getType(), rowFont));
				typeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
				table.addCell(typeCell);

				String amt = t.getAmount() != null ? "$" + t.getAmount().setScale(2, RoundingMode.HALF_UP) : "$0.00";
				PdfPCell amtCell = new PdfPCell(new Phrase(amt, rowFont));
				amtCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table.addCell(amtCell);

				PdfPCell statusCell = new PdfPCell(new Phrase(t.getStatus(), rowFont));
				statusCell.setHorizontalAlignment(Element.ALIGN_CENTER);
				table.addCell(statusCell);
			}

			document.add(table);
			document.close();

		} catch (Exception e) {
			logger.error("Error generating Transaction PDF: {}", e.getMessage());
			throw new RuntimeException("Failed to generate PDF report");
		}

		return out.toByteArray();
	}

	/**
	 * Used for background updates where we only have the Stripe reference.
	 */
	@Override
	@Transactional
	public void updateTransactionStatus(String stripeId, String newStatus) {
		transactionRepository.updateStatusByStripeRefId(stripeId, newStatus);
	}

	@Override
	public Page<MemberDuesDTO> getPagedDues(Long clubId, int page, int size) {
		// 1. Setup Pageable for manual count and metadata
		Pageable pageable = PageRequest.of(page, size);

		// 2. Calculate offset manually for Postgres compatibility
		long offset = pageable.getOffset();

		// 3. Fetch raw results and total count
		List<Map<String, Object>> rawRows = duesRepository.fetchDuesRaw(clubId, size, offset);
		long totalCount = duesRepository.countDuesByClub(clubId);

		// 4. Map raw DB types to MemberDuesDTO safely
		List<MemberDuesDTO> dtoList = rawRows.stream().map(row -> {
			// Paid and remaining amounts
			BigDecimal paid = row.get("paidAmount") != null ? new BigDecimal(row.get("paidAmount").toString())
					: BigDecimal.ZERO;
			BigDecimal remaining = row.get("remainingAmount") != null
					? new BigDecimal(row.get("remainingAmount").toString())
					: BigDecimal.ZERO;

			// Handle lastPaymentDate safely for different PostgreSQL / JDBC versions
			Object lastPaymentObj = row.get("lastPaymentDate");
			Instant lastDate = null;

			if (lastPaymentObj != null) {
				if (lastPaymentObj instanceof java.sql.Timestamp ts) {
					lastDate = ts.toInstant();
				} else if (lastPaymentObj instanceof Instant inst) {
					lastDate = inst;
				} else if (lastPaymentObj instanceof java.time.LocalDateTime ldt) {
					lastDate = ldt.atZone(ZoneId.systemDefault()).toInstant();
				} else {
					throw new IllegalStateException(
							"Unexpected type for lastPaymentDate: " + lastPaymentObj.getClass());
				}
			}

			// Convert IDs to Long
			Long dueId = row.get("dueId") != null ? ((Number) row.get("dueId")).longValue() : null;

			Long memberId = row.get("assignedUserId") != null ? ((Number) row.get("assignedUserId")).longValue() : null;

			return new MemberDuesDTO((String) row.get("fullName"), (String) row.get("email"),
					(String) row.get("status"), paid, remaining, lastDate, dueId, memberId);
		}).toList();

		// 5. Return as Spring Data Page
		return new PageImpl<>(dtoList, pageable, totalCount);
	}

	@Override
	public List<MemberDuesDTO> getDuesByStatus(Long clubId, String status) {
		// Fetch raw rows from repository
		List<Map<String, Object>> rawRows = duesRepository.fetchDuesByStatusRaw(clubId, status);

		return rawRows.stream().map(row -> mapToMemberDuesDTO(row)).toList();
	}

	@Override
	public byte[] generateDuesPdf(Long officerId, Long clubId, String status) {
		// 1. Fetch Data
		List<MemberDuesDTO> duesList;
		if ("All".equalsIgnoreCase(status)) {
			duesList = getPagedDues(clubId, 0, Integer.MAX_VALUE).getContent();
		} else {
			duesList = getDuesByStatus(clubId, status);
		}

		// 2. Fetch Club Name
		String clubName = userClubRepository.findMembershipDetail(officerId, clubId)
				.map(UserClubMembershipProjection::getClubName).orElse("Exchkr Club");

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Document document = new Document(PageSize.A4);

		try {
			PdfWriter.getInstance(document, out);
			document.open();

			// Fonts
			Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
			Font fontClubName = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
			Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
			Font rowFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

			// --- HEADER SECTION (Logo & Title) ---
			PdfPTable headerTable = new PdfPTable(2);
			headerTable.setWidthPercentage(100);
			headerTable.setSpacingAfter(10f);

			// Left Side: Logo or Club Name
			PdfPCell leftCell = new PdfPCell();
			leftCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
			try {
				java.io.InputStream is = getClass().getResourceAsStream("/static/images/EXCHKR.png");
				if (is != null) {
					com.lowagie.text.Image logo = com.lowagie.text.Image.getInstance(is.readAllBytes());
					logo.scaleToFit(100, 50);
					leftCell.addElement(logo);
				} else {
					leftCell.addElement(new Paragraph(clubName, fontClubName));
				}
			} catch (Exception e) {
				logger.warn("Could not load logo for Dues PDF. Using club name text.");
				leftCell.addElement(new Paragraph(clubName, fontClubName));
			}
			headerTable.addCell(leftCell);

			// Right Side: Title
			PdfPCell rightCell = new PdfPCell(new Phrase("Member Dues Report", fontTitle));
			rightCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
			rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			rightCell.setVerticalAlignment(Element.ALIGN_BOTTOM);
			headerTable.addCell(rightCell);

			document.add(headerTable);

			// Report Metadata (Filter Status and Date)
			Paragraph info = new Paragraph("Status Filter: " + status + " | Date: " + java.time.LocalDate.now());
			info.setAlignment(Element.ALIGN_RIGHT);
			document.add(info);
			document.add(new Paragraph(" ")); // Spacer

			// --- MAIN TABLE ---
			PdfPTable table = new PdfPTable(new float[] { 25, 30, 15, 15, 15 });
			table.setWidthPercentage(100);

			String[] headers = { "Member", "Email", "Paid", "Owed", "Status" };
			for (String h : headers) {
				PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
				cell.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setPadding(5);
				table.addCell(cell);
			}

			BigDecimal totalOwed = BigDecimal.ZERO;

			for (MemberDuesDTO due : duesList) {
				table.addCell(new Phrase(due.getFullName(), rowFont));
				table.addCell(new Phrase(due.getEmail(), rowFont));

				PdfPCell paid = new PdfPCell(
						new Phrase("$" + due.getAmountPaid().setScale(2, RoundingMode.HALF_UP), rowFont));
				paid.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table.addCell(paid);

				PdfPCell owed = new PdfPCell(
						new Phrase("$" + due.getAmountOwed().setScale(2, RoundingMode.HALF_UP), rowFont));
				owed.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table.addCell(owed);

				PdfPCell stat = new PdfPCell(new Phrase(due.getStatus(), rowFont));
				stat.setHorizontalAlignment(Element.ALIGN_CENTER);
				table.addCell(stat);

				totalOwed = totalOwed.add(due.getAmountOwed());
			}

			document.add(table);

			// --- SUMMARY FOOTER ---
			Paragraph summary = new Paragraph(
					"\nTotal Outstanding for this view: $" + totalOwed.setScale(2, RoundingMode.HALF_UP));
			summary.setAlignment(Element.ALIGN_RIGHT);
			document.add(summary);

			document.close();
		} catch (Exception e) {
			logger.error("Error generating Dues PDF: {}", e.getMessage());
			throw new RuntimeException("Failed to generate Dues PDF report");
		}
		return out.toByteArray();
	}

	/**
	 * Helper to map raw SQL Map to MemberDuesDTO
	 */
	private MemberDuesDTO mapToMemberDuesDTO(Map<String, Object> row) {
		MemberDuesDTO dto = new MemberDuesDTO();
		dto.setFullName((String) row.get("fullName"));
		dto.setEmail((String) row.get("email"));
		dto.setStatus((String) row.get("status"));

		dto.setAmountPaid(
				row.get("paidAmount") != null ? new BigDecimal(row.get("paidAmount").toString()) : BigDecimal.ZERO);

		dto.setAmountOwed(row.get("remainingAmount") != null ? new BigDecimal(row.get("remainingAmount").toString())
				: BigDecimal.ZERO);

		dto.setDueId(row.get("dueId") != null ? ((Number) row.get("dueId")).longValue() : null);
		dto.setMemberId(row.get("assignedUserId") != null ? ((Number) row.get("assignedUserId")).longValue() : null);

		// Handle Date
		Object dateObj = row.get("lastPaymentDate");
		if (dateObj instanceof java.sql.Timestamp ts)
			dto.setLastPaymentDate(ts.toInstant());

		return dto;
	}

	@Override
	public Map<String, Object> getDuesSummaryMetrics(Long clubId) {
		Map<String, Object> metrics = duesRepository.getDuesSummaryMetrics(clubId);

		if (metrics == null || metrics.isEmpty()) {
			metrics = new HashMap<>();
			metrics.put("duesCollected", 0);
			metrics.put("paidInFull", 0);
			metrics.put("needReminder", 0);
			metrics.put("collectionRate", 0.00);
		}

		return metrics;
	}

	private OffsetDateTime[] getCurrentSemesterRange() {
		int month = java.time.LocalDate.now().getMonthValue();
		int year = java.time.LocalDate.now().getYear();

		OffsetDateTime start;
		OffsetDateTime end;

		// Define semesters: Spring (Jan-May), Fall (Aug-Dec)
		if (month >= 1 && month <= 5) {
			start = OffsetDateTime.of(year, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
			end = OffsetDateTime.of(year, 5, 31, 23, 59, 59, 999000000, ZoneOffset.UTC);
		} else if (month >= 8 && month <= 12) {
			start = OffsetDateTime.of(year, 8, 1, 0, 0, 0, 0, ZoneOffset.UTC);
			end = OffsetDateTime.of(year, 12, 31, 23, 59, 59, 999000000, ZoneOffset.UTC);
		} else {
			// Summer/Winter Gap fallback
			start = OffsetDateTime.of(year, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
			end = OffsetDateTime.now(ZoneOffset.UTC);
		}

		return new OffsetDateTime[] { start, end };
	}

	@Override
	public FinanceSummaryDTO getFinanceSummary(Long clubId) {
		OffsetDateTime[] odtRange = getCurrentSemesterRange();

		// Total Income & Expenses (Only 'Completed')
		BigDecimal totalIncome = transactionRepository.sumAmountByClubAndTypeAndDate(clubId, "Income",
				odtRange[0].toInstant(), odtRange[1].toInstant());

		BigDecimal totalExpenses = transactionRepository.sumAmountByClubAndTypeAndDate(clubId, "Expense",
				odtRange[0].toInstant(), odtRange[1].toInstant());

		// Pending Count (Includes PENDING and PROCESSING)
		long pendingCount = reimbursementRepository.countPendingReimbursementsByDate(clubId, odtRange[0], odtRange[1]);

		return new FinanceSummaryDTO(totalIncome != null ? totalIncome : BigDecimal.ZERO,
				totalExpenses != null ? totalExpenses : BigDecimal.ZERO, pendingCount);
	}

	@Transactional(readOnly = true)
	@Override
	public void sendDueReminder(Long clubId, DueReminderRequest request) {
		// 1. Verify Member is Active in this specific Club using UserClubRepository
		// This ensures the user is currently associated with the club context
		UserClubMembershipProjection membership = userClubRepository.findMembershipDetail(request.memberId(), clubId)
				.orElseThrow(
						() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Member is not active in this club"));

		// Extract the User entity from the projection
		User member = membership.getUser();

		// 2. Validate Due belongs to User/Club and is not fully paid
		// findUnpaidDueForReminder checks: dueId, clubId, userId, and (paid < total)
		MemberDue due = duesRepository.findUnpaidDueForReminder(request.dueId(), clubId, request.memberId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
						"No unpaid record found for this due ID"));

		// 3. Calculate remaining amount
		String remainingAmount = due.getTotalAmount().subtract(due.getPaidAmount()).toString();

		// 4. Trigger the email service
		emailService.sendDueReminderEmail(member.getEmail(), member.getFirstName(), due.getDescription(),
				remainingAmount);
	}

	@Transactional(readOnly = true)
	@Override
	public int sendBulkDueReminders(Long clubId, List<DueReminderRequest> requests) {
		int successCount = 0;

		for (DueReminderRequest request : requests) {
			try {
				// 1. Verify Membership
				UserClubMembershipProjection membership = userClubRepository
						.findMembershipDetail(request.memberId(), clubId)
						.orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Member not active"));

				User member = membership.getUser();

				// 2. Validate Due & Unpaid Status
				MemberDue due = duesRepository.findUnpaidDueForReminder(request.dueId(), clubId, request.memberId())
						.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
								"Due not found or already paid"));

				// 3. Calculate remaining amount
				BigDecimal remaining = due.getTotalAmount()
						.subtract(due.getPaidAmount() != null ? due.getPaidAmount() : BigDecimal.ZERO);

				// 4. Trigger Email
				emailService.sendDueReminderEmail(member.getEmail(), member.getFirstName(), due.getDescription(),
						remaining.toString());

				successCount++;
				logger.info("Reminder sent for DueID: {} to User: {}", request.dueId(), request.memberId());

			} catch (Exception e) {
				logger.error("Failed to send reminder for DueID: {}. Error: {}", request.dueId(), e.getMessage());
			}
		}

		return successCount;
	}

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
		BigDecimal platformFee = grossAmount.multiply(BigDecimal.valueOf(pPercent)).setScale(2, RoundingMode.HALF_UP);

		BigDecimal stripeFee = grossAmount.multiply(BigDecimal.valueOf(sPercent)).add(BigDecimal.valueOf(sFixed))
				.setScale(2, RoundingMode.HALF_UP);

		// 3. Total sum of both fees
		BigDecimal totalFees = platformFee.add(stripeFee);

		results.put("gross", grossAmount);
		results.put("platform", platformFee);
		results.put("stripe", stripeFee);
		results.put("totalFees", totalFees);

		return results;
	}

	@Override
	@Transactional
	public void createInvoice(CreateInvoiceRequest request, Long officerId, Long clubId) {
		try {
			// 1. Calculate base Amount
			BigDecimal baseAmount = request.getLineItems().stream().map(CreateInvoiceRequest.LineItemRequest::getAmount)
					.reduce(BigDecimal.ZERO, BigDecimal::add);

			// Get the map of results
			Map<String, BigDecimal> feeData = calculateFees(baseAmount);

			// 2. Calculate Gross Amount (Including fees)
			BigDecimal grossAmount = feeData.get("gross");
			BigDecimal platformFee = feeData.get("platform");
			BigDecimal stripeFee = feeData.get("stripe");
			BigDecimal totalFees = feeData.get("totalFees");

			// 2. Fetch Club Name for the Header
			String clubName = userClubRepository.findMembershipDetail(officerId, clubId)
					.map(UserClubMembershipProjection::getClubName).orElse("Exchkr Club");

			// 3. Save Invoice Header
			InvoiceHeader header = new InvoiceHeader();
			header.setClubId(clubId);
			header.setInvoiceTitle(request.getInvoiceTitle());
			header.setInvoiceTotalAmount(grossAmount);
			header.setPlatformFees(platformFee);
			header.setStripeFees(stripeFee);
			header.setInvoiceDueDate(java.time.LocalDate.parse(request.getDueDate())
					.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
			header.setAdditionalNote(request.getAdditionalNotes());
			header.setCreatedBy(officerId);
			header = headerRepo.save(header);

			// 4. Save Invoice Details (Line Items)
			for (CreateInvoiceRequest.LineItemRequest item : request.getLineItems()) {
				InvoiceDetail detail = new InvoiceDetail();
				detail.setInvoiceId(header.getInvoiceId());
				detail.setLineItemDescription(item.getDescription());
				detail.setLineItemAmount(item.getAmount().doubleValue());
				detail.setCreatedBy(officerId);
				detailRepo.save(detail);
			}

			// Ensure Directory Exists
			Path dirPath = Paths.get(basePath);
			if (!Files.exists(dirPath)) {
				Files.createDirectories(dirPath);
			}

			// 5. Loop through members: Generate unique PDF and send Email per member
			for (Long memberId : request.getSelectedMemberIds()) {
				User member = userRepository.findById(memberId).orElseThrow(
						() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found: " + memberId));

				String memberName = member.getFirstName() + " "
						+ (member.getLastName() != null ? member.getLastName() : "");

				// Generate unique filename for this specific member's invoice
				String fileName = "INV_" + memberId + "_" + UUID.randomUUID().toString().substring(0, 8) + ".pdf";
				String fullPath = Paths.get(basePath, fileName).toString();

				// GENERATE PERSONALIZED PDF
				generateInvoicePdf(header, request, baseAmount, grossAmount, totalFees, fileName, memberName, clubName); // Save
																															// mapping
				InvoiceMemberMapping mapping = new InvoiceMemberMapping();
				mapping.setInvoiceId(header.getInvoiceId());
				mapping.setClubId(clubId);
				mapping.setMemberId(memberId);
				mapping.setInvoiceFileName(fileName);
				mapping.setCreatedBy(officerId);
				mappingRepo.save(mapping);

				// Save member due record
				MemberDue due = new MemberDue();
				due.setClubId(clubId);
				due.setInvoiceId(header.getInvoiceId());
				due.setAssignedUserId(memberId);
				due.setCreatedByUserId(officerId);
				due.setDescription(request.getInvoiceTitle());
				due.setTotalAmount(baseAmount);
				due.setDueDate(java.time.LocalDate.parse(request.getDueDate()));
				due.setStatus("Unpaid");
				duesRepository.save(due);

				// Send Email with the personalized PDF attachment
				emailService.sendInvoiceEmail(member.getEmail(), memberName, request.getInvoiceTitle(), fullPath);
			}
		} catch (Exception e) {
			logger.error("Failed to create invoice: {}", e.getMessage());
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Invoice creation failed");
		}
	}

	private void generateInvoicePdf(InvoiceHeader header, CreateInvoiceRequest request, BigDecimal baseAmount,
			BigDecimal grossAmount, BigDecimal totalFees, String fileName, String memberName, String clubName) {

// Define Paths
		Path filePath = Paths.get(basePath, fileName);

		try (java.io.FileOutputStream fos = new java.io.FileOutputStream(filePath.toFile())) {
			com.lowagie.text.Document document = new com.lowagie.text.Document(com.lowagie.text.PageSize.A4);
			com.lowagie.text.pdf.PdfWriter.getInstance(document, fos);
			document.open();

// --- 1. LOGO & HEADER SECTION ---
			com.lowagie.text.pdf.PdfPTable headerTable = new com.lowagie.text.pdf.PdfPTable(2);
			headerTable.setWidthPercentage(100);

// Left Side: Logo or Club Name
			com.lowagie.text.pdf.PdfPCell logoCell = new com.lowagie.text.pdf.PdfPCell();
			logoCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);

			try {
				java.io.InputStream is = getClass().getResourceAsStream("/static/images/EXCHKR.png");
				if (is != null) {
					com.lowagie.text.Image logo = com.lowagie.text.Image.getInstance(is.readAllBytes());
					logo.scaleToFit(100, 50);
					logoCell.addElement(logo);
				} else {
					logoCell.addElement(new com.lowagie.text.Paragraph(clubName, new com.lowagie.text.Font(
							com.lowagie.text.Font.HELVETICA, 12, com.lowagie.text.Font.BOLD)));
				}
			} catch (Exception e) {
				logger.warn("Could not load logo: {}. Using club name text.", e.getMessage());
				logoCell.addElement(new com.lowagie.text.Paragraph(clubName));
			}
			headerTable.addCell(logoCell);

// Right Side: INVOICE Title
			com.lowagie.text.pdf.PdfPCell titleCell = new com.lowagie.text.pdf.PdfPCell();
			titleCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
			com.lowagie.text.Paragraph titlePara = new com.lowagie.text.Paragraph("INVOICE",
					new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 18, com.lowagie.text.Font.BOLD));
			titlePara.setAlignment(com.lowagie.text.Element.ALIGN_RIGHT);
			titleCell.addElement(titlePara);
			headerTable.addCell(titleCell);

			document.add(headerTable);

// Reference Info
			document.add(new com.lowagie.text.Paragraph("Invoice: " + header.getInvoiceTitle()));
			document.add(new com.lowagie.text.Paragraph("Issued By: " + clubName));
			document.add(new com.lowagie.text.Chunk(new com.lowagie.text.pdf.draw.LineSeparator()));
			document.add(new com.lowagie.text.Paragraph(" "));

// --- 2. INFORMATION GRID (Bill To & Dates) ---
			com.lowagie.text.pdf.PdfPTable infoTable = new com.lowagie.text.pdf.PdfPTable(2);
			infoTable.setWidthPercentage(100);

// Member Info
			com.lowagie.text.pdf.PdfPCell leftCell = new com.lowagie.text.pdf.PdfPCell();
			leftCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
			leftCell.addElement(new com.lowagie.text.Paragraph("Bill To:",
					new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.BOLD)));
			leftCell.addElement(new com.lowagie.text.Paragraph(memberName));
			infoTable.addCell(leftCell);

// Dates
			com.lowagie.text.pdf.PdfPCell rightCell = new com.lowagie.text.pdf.PdfPCell();
			rightCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
			com.lowagie.text.Paragraph dates = new com.lowagie.text.Paragraph();
			dates.setAlignment(com.lowagie.text.Element.ALIGN_RIGHT);
			dates.add(new com.lowagie.text.Chunk("Due Date: " + request.getDueDate() + "\n"));
			dates.add(new com.lowagie.text.Chunk("Date Issued: " + java.time.LocalDate.now()));
			rightCell.addElement(dates);
			infoTable.addCell(rightCell);

			document.add(infoTable);
			document.add(new com.lowagie.text.Paragraph(" "));

// --- 3. MAIN TABLE (Itemized List + Summary) ---
			com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(2);
			table.setWidthPercentage(100);
			table.setSpacingBefore(10f);

			com.lowagie.text.Font boldFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 12,
					com.lowagie.text.Font.BOLD);

// Table Header
			com.lowagie.text.pdf.PdfPCell h1 = new com.lowagie.text.pdf.PdfPCell(
					new com.lowagie.text.Paragraph("Description", boldFont));
			com.lowagie.text.pdf.PdfPCell h2 = new com.lowagie.text.pdf.PdfPCell(
					new com.lowagie.text.Paragraph("Amount", boldFont));
			h1.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
			h2.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
			h2.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_RIGHT);
			table.addCell(h1);
			table.addCell(h2);

// Line Items
			for (CreateInvoiceRequest.LineItemRequest item : request.getLineItems()) {
				table.addCell(item.getDescription());
				com.lowagie.text.pdf.PdfPCell cell = new com.lowagie.text.pdf.PdfPCell(
						new com.lowagie.text.Paragraph("$" + item.getAmount()));
				cell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_RIGHT);
				table.addCell(cell);
			}

// --- SUMMARY ROWS ---

// 1. Subtotal (Base Amount)
			com.lowagie.text.pdf.PdfPCell subTotalLabel = new com.lowagie.text.pdf.PdfPCell(
					new com.lowagie.text.Paragraph("Subtotal", boldFont));
			subTotalLabel.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_RIGHT);
			subTotalLabel.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
			table.addCell(subTotalLabel);

			com.lowagie.text.pdf.PdfPCell subTotalVal = new com.lowagie.text.pdf.PdfPCell(
					new com.lowagie.text.Paragraph("$" + baseAmount.toString()));
			subTotalVal.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_RIGHT);
			subTotalVal.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
			table.addCell(subTotalVal);

// 2. Additional Charges (Fees)
			com.lowagie.text.pdf.PdfPCell feeLabel = new com.lowagie.text.pdf.PdfPCell(
					new com.lowagie.text.Paragraph("Additional Charges (Platform fee + stripe fee)*", boldFont));
			feeLabel.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_RIGHT);
			feeLabel.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
			table.addCell(feeLabel);

			com.lowagie.text.pdf.PdfPCell feeVal = new com.lowagie.text.pdf.PdfPCell(
					new com.lowagie.text.Paragraph("$" + totalFees.toString()));
			feeVal.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_RIGHT);
			feeVal.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
			table.addCell(feeVal);

// 3. Final Total
			com.lowagie.text.pdf.PdfPCell totalLabel = new com.lowagie.text.pdf.PdfPCell(
					new com.lowagie.text.Paragraph("TOTAL", boldFont));
			totalLabel.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
			totalLabel.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_RIGHT);
			table.addCell(totalLabel);

			com.lowagie.text.pdf.PdfPCell totalVal = new com.lowagie.text.pdf.PdfPCell(
					new com.lowagie.text.Paragraph("$" + grossAmount.toString(), boldFont));
			totalVal.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
			totalVal.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_RIGHT);
			table.addCell(totalVal);

			document.add(table);

// --- 4. FOOTER & DISCLAIMER ---
			document.add(new com.lowagie.text.Paragraph(" "));

// Notes Section
			if (request.getAdditionalNotes() != null && !request.getAdditionalNotes().isEmpty()) {
				document.add(new com.lowagie.text.Paragraph("Notes:",
						new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.ITALIC)));
				document.add(new com.lowagie.text.Paragraph(request.getAdditionalNotes()));
				document.add(new com.lowagie.text.Paragraph(" "));
			}

// Disclaimer
//			com.lowagie.text.Font smallFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 8,
//					com.lowagie.text.Font.ITALIC);
//			document.add(new com.lowagie.text.Paragraph(
//					"*Processing fees are estimated and may vary based on the payment method used.", smallFont));

			document.close();
		} catch (Exception e) {
			logger.error("Error generating PDF {}: {}", fileName, e.getMessage());
			throw new RuntimeException("PDF Generation failed: " + e.getMessage());
		}
	}

	// --- Helper Methods (Stripe Logic) ---

	private String extractStatusFromPayload(String payload) {
		// Logic to parse JSON and return status (e.g., payment_intent.succeeded)
		return null;
	}

	private String extractIdFromPayload(String payload) {
		// Logic to parse JSON and return the ID (e.g., pi_12345)
		return null;
	}

	public List<ReimbursementListResponse> reimbursementRequestList(Long clubId) {
		List<ReimbursementListResponse> reimbursements = reimbursementRepository.reimbursementRequestList(clubId);

		if (reimbursements == null || reimbursements.isEmpty()) {
			return List.of();
		}

		return reimbursements;
	}

	@Override
	@Transactional
	public void reimbursementRequestReject(Long userId, Long clubId, Long reimbursementId, String rejectReason) {

		int updated = reimbursementRepository.reimbursementRequestReject(clubId, userId, reimbursementId, rejectReason);

		if (updated == 0) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Reimbursement request cannot be rejected");
		}

		// Fetch rejected reimbursement info
		Map<String, Object> data = reimbursementRepository.getRejectedReimbursementInfo(reimbursementId);

		if (data == null || data.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to fetch rejected reimbursement details");
		}

		// Map values safely
		String toEmail = (String) data.get("toemail");
		String memberName = (String) data.get("membername");
		String reimbursementCategory = (String) data.get("reimbursementcategory");
		BigDecimal amount = (BigDecimal) data.get("amount");
		String finalRejectReason = (String) data.get("rejectreason");

		// Send email
		emailService.sendReimbursementRejectionEmail(toEmail, memberName, reimbursementCategory, amount,
				finalRejectReason);
	}

	@Override
	@Transactional
	public ResponseEntity<Resource> reimbursementReceiptDownload(Long userId, Long clubId, Long reimbursementId) {

		List<Object[]> results = reimbursementRepository.reimbursementReceiptDownload(clubId, reimbursementId);

		if (results == null || results.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reimbursement receipt not found");
		}

		Object[] row = results.get(0); // Take the first (and only) row
		String systemFileName = String.valueOf(row[0]);
		String originalFileName = String.valueOf(row[1]);

		Path filePath = Paths.get(basePath, systemFileName);

		if (!Files.exists(filePath)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Receipt file not found in system");
		}

		Resource resource;
		try {
			resource = new UrlResource(filePath.toUri());
		} catch (MalformedURLException ex) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to load receipt file");
		}

		return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + originalFileName + "\"")
				.body(resource);
	}

	@Override
	@Transactional
	public void reimbursementRequestApprove(Long userId, Long clubId, Long reimbursementId, String stripeRefId) {

		int updated = reimbursementRepository.reimbursementRequestApprove(clubId, userId, reimbursementId, stripeRefId);

		if (updated == 0) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Reimbursement request cannot be approved");
		}

		// Fetch rejected reimbursement info
		Map<String, Object> data = reimbursementRepository.getApprovedReimbursementInfo(reimbursementId);

		if (data == null || data.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to fetch rejected reimbursement details");
		}

		// Map values safely
		String toEmail = (String) data.get("toemail");
		String memberName = (String) data.get("membername");
		String reimbursementCategory = (String) data.get("reimbursementcategory");
		BigDecimal amount = (BigDecimal) data.get("amount");

		// Send email
		emailService.sendReimbursementApproveEmail(toEmail, memberName, reimbursementCategory, amount);
	}

	@Override
	public List<PendingActionsResponse> getPendingActions(Long clubId) {

		List<Object[]> rows = transactionRepository.getPendingActions(clubId);
		Object[] row = (rows != null && !rows.isEmpty()) ? rows.get(0) : null;

		long expenseApprovalCount = (row != null && row.length >= 2 && row[0] != null) ? ((Number) row[0]).longValue()
				: 0;

		long duesReminderCount = (row != null && row.length >= 2 && row[1] != null) ? ((Number) row[1]).longValue() : 0;

		List<PendingActionsResponse> response = new ArrayList<>(2);

		PendingActionsResponse expense = new PendingActionsResponse();
		expense.setActionType("EXPENSE_APPROVAL");
		expense.setPendingCount(expenseApprovalCount);
		response.add(expense);

		PendingActionsResponse dues = new PendingActionsResponse();
		dues.setActionType("DUES_REMINDER");
		dues.setPendingCount(duesReminderCount);
		response.add(dues);

		return response;
	}

	@Override
	public List<RecentActivityResponse> getRecentActivity(Long clubId) {

		List<Object[]> rows = transactionRepository.getRecentActivity(clubId);

		if (rows == null || rows.isEmpty()) {
			return Collections.emptyList(); // []
		}

		List<RecentActivityResponse> response = new ArrayList<>();

		for (Object[] row : rows) {
			RecentActivityResponse dto = new RecentActivityResponse();

			dto.setTransId((Long) row[0]);

			Instant instant = (Instant) row[1];
			dto.setTransDate(instant != null ? instant.atOffset(ZoneOffset.UTC) : null);

			dto.setDescription((String) row[2]);
			dto.setCategory((String) row[3]);
			dto.setType((String) row[4]);
			dto.setAmount((BigDecimal) row[5]);
			dto.setStatus((String) row[6]);

			dto.setDoneByUserId((Long) row[7]);
			dto.setDoneByUserName((String) row[8]);

			dto.setPaidToUserId((Long) row[9]); // nullable
			dto.setPaidToUserName((String) row[10]); // nullable

			response.add(dto);
		}

		return response;
	}

	@Override
	public List<CategorySpendingResponse> getSpendingByCategory(Long clubId) {
		List<Map<String, Object>> rawData = transactionRepository.getSpendingByCategoryRaw(clubId);

		String[] colorPalette = { "#6366F1", "#10B981", "#F59E0B", "#3B82F6", "#EF4444", "#8B5CF6", "#EC4899",
				"#06B6D4", "#F97316" };

		java.math.BigDecimal grandTotal = rawData.stream()
				.map(row -> row.get("value") != null ? new java.math.BigDecimal(row.get("value").toString())
						: java.math.BigDecimal.ZERO)
				.reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

		if (grandTotal.compareTo(java.math.BigDecimal.ZERO) <= 0) {
			return java.util.Collections.emptyList();
		}

		List<CategorySpendingResponse> response = new ArrayList<>();
		for (int i = 0; i < rawData.size(); i++) {
			Map<String, Object> row = rawData.get(i);
			java.math.BigDecimal categorySum = row.get("value") != null
					? new java.math.BigDecimal(row.get("value").toString())
					: java.math.BigDecimal.ZERO;

			double percentage = categorySum.divide(grandTotal, 4, java.math.RoundingMode.HALF_UP)
					.multiply(java.math.BigDecimal.valueOf(100)).doubleValue();

			String categoryName = (String) row.get("name");
			String color = colorPalette[i % colorPalette.length];

			response.add(new CategorySpendingResponse(categoryName, percentage, color));
		}
		return response;
	}

	@Override
	public List<MonthlySpendingResponse> getMonthlySpendingTrend(Long clubId) {
		List<Map<String, Object>> rawData = transactionRepository.getMonthlySpendingRaw(clubId);

		return rawData.stream()
				.map(row -> new MonthlySpendingResponse((String) row.get("month"),
						row.get("amount") != null ? new java.math.BigDecimal(row.get("amount").toString())
								: java.math.BigDecimal.ZERO))
				.toList();
	}

	@Override
	@Transactional
	public void saveMasterCategories(Long clubId, List<String> categoryNames) {
		if (categoryNames == null || categoryNames.isEmpty()) {
			return;
		}

		// 1. Clean the list: trim, remove empty, and handle case-insensitive duplicates
		// in the input
		List<String> cleanedNames = categoryNames.stream().map(String::trim).filter(name -> !name.isEmpty()).distinct()
				.toList();

		// 2. Filter out names that already exist in the database for this club
		List<BudgetCategoryMaster> toSave = cleanedNames.stream()
				.filter(name -> !masterRepository.existsByClubIdAndCategoryNameIgnoreCase(clubId, name)).map(name -> {
					BudgetCategoryMaster master = new BudgetCategoryMaster();
					master.setClubId(clubId);
					master.setCategoryName(name);
					return master;
				}).collect(Collectors.toList());

		// 3. Bulk save
		if (!toSave.isEmpty()) {
			masterRepository.saveAll(toSave);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<BudgetCategoryMaster> getBudgetCategoryMasterList(Long clubId) {
		return masterRepository.findByClubIdOrderByCategoryNameAsc(clubId);
	}

	@Override
	@Transactional
	public void saveBudget(Long clubId, BudgetSetupRequest request, Long userId) {
		int currentYear = java.time.LocalDate.now().getYear();

		boolean alreadyExists = budgetRepository.existsByClubIdAndFiscalYearAndActiveInd(clubId, currentYear, 1);
		if (alreadyExists) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"A budget for " + currentYear + " has already been established.");
		}

		// 1. Create the Budget Header
		ClubBudget budget = new ClubBudget();
		budget.setClubId(clubId);
		budget.setTotalBudget(request.getTotalBudget());
		budget.setCreatedBy(userId);
		budget.setFiscalYear(currentYear);
		budget.setActiveInd(1);

		// 2. Map the categories (with Create-on-the-fly logic)
		if (request.getCategories() != null) {
			for (BudgetSetupRequest.CategoryAllocationDTO catDto : request.getCategories()) {
				BudgetCategoryMaster master;

				if (catDto.getCategoryId() == null) {
					// BRAND NEW CATEGORY
					// Ensure the name isn't empty
					if (catDto.getCategoryName() == null || catDto.getCategoryName().trim().isEmpty()) {
						throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
								"Category name is required for new categories.");
					}

					master = new BudgetCategoryMaster();
					master.setCategoryName(catDto.getCategoryName().trim());
					master.setClubId(clubId);
					// Save it to the master table first to generate an ID
					master = masterRepository.save(master);
				} else {
					// EXISTING CATEGORY
					master = masterRepository.findById(catDto.getCategoryId())
							.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
									"Category ID " + catDto.getCategoryId() + " not found."));

					// Security check
					if (!master.getClubId().equals(clubId)) {
						throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized category access.");
					}
				}

				// 3. Create the budget allocation link
				BudgetCategory allocation = new BudgetCategory();
				allocation.setCategoryMaster(master);
				allocation.setTotalBudgeted(catDto.getTotalBudgeted());
				allocation.setTotalSpent(BigDecimal.ZERO);

				budget.addCategory(allocation);
			}
		}

		budgetRepository.save(budget);
	}

	@Override
	@Transactional
	public void patchBudget(Long clubId, BudgetPatchRequest request, Long userId) {
		int currentYear = java.time.LocalDate.now().getYear();

		// 1. Find existing budget
		ClubBudget budget = budgetRepository.findByClubIdAndFiscalYearAndActiveInd(clubId, currentYear, 1)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						"No active budget found for the current year."));

		BigDecimal limit = (request.getTotalBudget() != null) ? request.getTotalBudget() : budget.getTotalBudget();

		// 2. Perform Updates and track processed IDs for removal sync
		if (request.getCategoryUpdates() != null) {
			// We use this set to identify which allocations the user wants to KEEP
			Set<Long> processedMasterIds = new java.util.HashSet<>();

			for (BudgetPatchRequest.CategoryPatchDTO updateDto : request.getCategoryUpdates()) {
				BudgetCategoryMaster master;

				// Handle Master Category Identification (Existing or New)
				if (updateDto.getCategoryId() == null) {
					if (updateDto.getCategoryName() == null || updateDto.getCategoryName().isBlank()) {
						throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
								"Category ID or Name must be provided.");
					}

					// Prevent duplicate master entries by checking name (case-insensitive)
					master = masterRepository.findByClubIdAndCategoryNameIgnoreCase(clubId, updateDto.getCategoryName())
							.orElseGet(() -> {
								BudgetCategoryMaster newMaster = new BudgetCategoryMaster();
								newMaster.setClubId(clubId);
								newMaster.setCategoryName(updateDto.getCategoryName().trim());
								return masterRepository.save(newMaster);
							});
				} else {
					master = masterRepository.findById(updateDto.getCategoryId())
							.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
									"Master Category not found for ID: " + updateDto.getCategoryId()));
				}

				// Track this ID
				processedMasterIds.add(master.getCategoryId());

				// Update existing or add new allocation
				Optional<BudgetCategory> existingAllocation = budget.getCategories().stream()
						.filter(c -> c.getCategoryMaster().getCategoryId().equals(master.getCategoryId())).findFirst();

				if (existingAllocation.isPresent()) {
					existingAllocation.get().setTotalBudgeted(updateDto.getTotalBudgeted());
				} else {
					BudgetCategory newAllocation = new BudgetCategory();
					newAllocation.setCategoryMaster(master);
					newAllocation.setTotalBudgeted(updateDto.getTotalBudgeted());
					newAllocation.setTotalSpent(BigDecimal.ZERO);
					budget.addCategory(newAllocation);
				}
			}

			// SYNC REMOVALS: If a category is in the DB but NOT in the request, remove it
			budget.getCategories().removeIf(
					allocation -> !processedMasterIds.contains(allocation.getCategoryMaster().getCategoryId()));
		}

		// 3. VALIDATION: Check if sum of all category allocations exceeds the limit
		BigDecimal sumOfCategories = budget.getCategories().stream().map(BudgetCategory::getTotalBudgeted)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		if (sumOfCategories.compareTo(limit) > 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("Total allocation (%s) exceeds the total budget (%s)", sumOfCategories, limit));
		}

		// 4. Update Header and Save
		if (request.getTotalBudget() != null) {
			budget.setTotalBudget(request.getTotalBudget());
		}

		budgetRepository.save(budget);
	}

	@Override
	public BudgetSummaryResponse getBudgetSummary(Long clubId) {
		int currentYear = java.time.LocalDate.now().getYear();
		ClubBudget budget = budgetRepository.findByClubIdAndFiscalYearAndActiveInd(clubId, currentYear, 1)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No active budget found"));

		BudgetSummaryResponse response = new BudgetSummaryResponse();
		response.setBudgetId(budget.getBudgetId());
		response.setFiscalYear(budget.getFiscalYear());
		response.setTotalAnnualBudget(budget.getTotalBudget());

		BigDecimal totalAllocated = BigDecimal.ZERO;
		BigDecimal totalActualSpent = BigDecimal.ZERO;
		List<BudgetSummaryResponse.CategoryDetail> details = new ArrayList<>();

		for (BudgetCategory cat : budget.getCategories()) {
			BudgetSummaryResponse.CategoryDetail detail = new BudgetSummaryResponse.CategoryDetail();
			String categoryName = (cat.getCategoryMaster() != null) ? cat.getCategoryMaster().getCategoryName()
					: "Unknown";

			detail.setCategoryName(categoryName);
			detail.setBudgeted(cat.getTotalBudgeted());

			// A. Get Manual/Direct Expenses (StripeRefId IS NULL)
			BigDecimal directSpent = transactionRepository.sumDirectExpensesOnly(clubId, categoryName);
			if (directSpent == null)
				directSpent = BigDecimal.ZERO;

			// B. Get Reimbursement Payouts (Joined on StripeRefId)
			// We look at Transaction status 'Completed' to avoid the race condition issue
			BigDecimal reimbursementSpent = transactionRepository.sumValidatedReimbursementExpenses(clubId,
					categoryName);
			if (reimbursementSpent == null)
				reimbursementSpent = BigDecimal.ZERO;

			// C. Combine for accurate category total
			BigDecimal actualCategorySpent = directSpent.add(reimbursementSpent);

			detail.setSpent(actualCategorySpent);
			detail.setRemaining(cat.getTotalBudgeted().subtract(actualCategorySpent));

			// D. Percentage Math
			if (cat.getTotalBudgeted().compareTo(BigDecimal.ZERO) > 0) {
				BigDecimal percentage = actualCategorySpent.multiply(new BigDecimal("100"))
						.divide(cat.getTotalBudgeted(), 2, java.math.RoundingMode.HALF_UP);
				detail.setPercentageUsed(Math.min(percentage.doubleValue(), 100.0));
			} else {
				detail.setPercentageUsed(0.0);
			}

			details.add(detail);
			totalAllocated = totalAllocated.add(cat.getTotalBudgeted());
			totalActualSpent = totalActualSpent.add(actualCategorySpent);
		}

		response.setCategories(details);
		response.setTotalAllocated(totalAllocated);
		response.setTotalSpent(totalActualSpent);
		response.setRemainingAmount(budget.getTotalBudget().subtract(totalActualSpent));

		return response;
	}

}