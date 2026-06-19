package com.factoring.repository;

import com.factoring.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByReceivableIdOrderByCreateTimeDesc(Long receivableId);
    void deleteByReceivableId(Long receivableId);
}
