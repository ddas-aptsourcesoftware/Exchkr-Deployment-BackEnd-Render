package com.exchkr.club.management.dao;

import com.exchkr.club.management.model.entity.InvoiceHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InvoiceHeaderRepository extends JpaRepository<InvoiceHeader, Long> {
    List<InvoiceHeader> findByClubId(Long clubId);
}