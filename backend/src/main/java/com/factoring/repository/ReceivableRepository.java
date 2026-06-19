package com.factoring.repository;

import com.factoring.common.ReceivableStatusEnum;
import com.factoring.entity.Receivable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReceivableRepository extends JpaRepository<Receivable, Long> {
    @Query("SELECT r FROM Receivable r ORDER BY r.createTime DESC")
    List<Receivable> findAllOrderByCreateTimeDesc();

    List<Receivable> findByManagerUsernameOrderByCreateTimeDesc(String managerUsername);

    List<Receivable> findByStatusOrderByCreateTimeDesc(ReceivableStatusEnum status);

    List<Receivable> findByStatusInOrderByCreateTimeDesc(List<ReceivableStatusEnum> statusList);
}
