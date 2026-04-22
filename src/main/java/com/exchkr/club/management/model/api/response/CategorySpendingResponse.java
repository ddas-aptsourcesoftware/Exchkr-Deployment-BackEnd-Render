package com.exchkr.club.management.model.api.response;

public record CategorySpendingResponse(
    String name,
    Double percentage,
    String color
) {}