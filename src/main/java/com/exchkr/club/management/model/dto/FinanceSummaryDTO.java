package com.exchkr.club.management.model.dto;

import java.math.BigDecimal;

public record FinanceSummaryDTO(
    BigDecimal totalIncome,
    BigDecimal totalExpenses,
    long pendingReimbursements
) {}