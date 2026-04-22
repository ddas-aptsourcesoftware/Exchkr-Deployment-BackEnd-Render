package com.exchkr.club.management.model.api.response;

import java.math.BigDecimal;

public record MonthlySpendingResponse(
    String month,
    BigDecimal amount
) {}