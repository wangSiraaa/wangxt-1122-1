package com.factoring.repository;

import com.factoring.entity.OperationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {
    List<OperationLog> findByReceivableIdOrderByOperateTimeDesc(Long receivableId);
}
