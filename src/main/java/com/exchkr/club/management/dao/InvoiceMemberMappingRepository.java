package com.exchkr.club.management.dao;

import com.exchkr.club.management.model.entity.InvoiceMemberMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InvoiceMemberMappingRepository extends JpaRepository<InvoiceMemberMapping, Long> {
    List<InvoiceMemberMapping> findByInvoiceId(Long invoiceId);
    List<InvoiceMemberMapping> findByMemberId(Long memberId);
}