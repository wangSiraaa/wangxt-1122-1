package com.factoring.repository;

import com.factoring.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByReceivableIdOrderByCreateTimeDesc(Long receivableId);
    void deleteByReceivableId(Long receivableId);

    @Query("SELECT i FROM Invoice i WHERE i.invoiceCode = :invoiceCode AND i.invoiceNo = :invoiceNo " +
           "AND i.receivableId != :excludeReceivableId " +
           "AND i.receivableId IN (SELECT r.id FROM Receivable r WHERE r.status IN ('BUYER_CONFIRMED', 'PENDING_LOAN', 'LOANED', 'PENDING_SUPPLEMENT'))")
    List<Invoice> findTransferredInvoicesByCodeAndNo(@Param("invoiceCode") String invoiceCode,
                                                    @Param("invoiceNo") String invoiceNo,
                                                    @Param("excludeReceivableId") Long excludeReceivableId);
}
