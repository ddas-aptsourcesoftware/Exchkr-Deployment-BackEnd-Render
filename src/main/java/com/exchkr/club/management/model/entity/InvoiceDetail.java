package com.exchkr.club.management.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "ecm_invoice_details")
public class InvoiceDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoice_detail_id")
    private Long invoiceDetailId;

    @Column(name = "invoice_id", nullable = false)
    private Long invoiceId;

    @Column(name = "line_item_description")
    private String lineItemDescription;

    @Column(name = "line_item_amount")
    private Double lineItemAmount;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    public InvoiceDetail() {}

    public Long getInvoiceDetailId() { return invoiceDetailId; }
    public void setInvoiceDetailId(Long invoiceDetailId) { this.invoiceDetailId = invoiceDetailId; }
    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }
    public String getLineItemDescription() { return lineItemDescription; }
    public void setLineItemDescription(String lineItemDescription) { this.lineItemDescription = lineItemDescription; }
    public Double getLineItemAmount() { return lineItemAmount; }
    public void setLineItemAmount(Double lineItemAmount) { this.lineItemAmount = lineItemAmount; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
}