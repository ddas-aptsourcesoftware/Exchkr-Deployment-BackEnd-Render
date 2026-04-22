package com.exchkr.club.management.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "ecm_invoice_member_mapping")
public class InvoiceMemberMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoice_member_mapping_id")
    private Long invoiceMemberMappingId;

    @Column(name = "invoice_id", nullable = false)
    private Long invoiceId;

    @Column(name = "club_id", nullable = false)
    private Long clubId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "invoice_file_name")
    private String invoiceFileName;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    public InvoiceMemberMapping() {}

    public Long getInvoiceMemberMappingId() { return invoiceMemberMappingId; }
    public void setInvoiceMemberMappingId(Long invoiceMemberMappingId) { this.invoiceMemberMappingId = invoiceMemberMappingId; }
    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }
    public Long getClubId() { return clubId; }
    public void setClubId(Long clubId) { this.clubId = clubId; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public String getInvoiceFileName() { return invoiceFileName; }
    public void setInvoiceFileName(String invoiceFileName) { this.invoiceFileName = invoiceFileName; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
}