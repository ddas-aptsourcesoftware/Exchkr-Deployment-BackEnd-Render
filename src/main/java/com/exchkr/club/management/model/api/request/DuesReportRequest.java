package com.exchkr.club.management.model.api.request;


public record DuesReportRequest(
	    String status // e.g., "Paid", "Unpaid", "Partial", or "All"
	) {}