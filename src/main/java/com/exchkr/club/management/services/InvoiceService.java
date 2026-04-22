package com.exchkr.club.management.services;

import com.exchkr.club.management.model.api.request.CreateInvoiceRequest;

public interface InvoiceService {
    void createInvoice(CreateInvoiceRequest request, Long officerId, Long clubId);
}