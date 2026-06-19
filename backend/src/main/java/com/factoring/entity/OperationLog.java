package com.factoring.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "t_operation_log")
public class OperationLog implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long receivableId;

    @Column(length = 64)
    private String operatorUsername;

    @Column(length = 64)
    private String operatorName;

    @Column(length = 64)
    private String operatorRole;

    @Column(length = 128)
    private String operation;

    @Column(length = 512)
    private String detail;

    private LocalDateTime operateTime;

    @PrePersist
    public void prePersist() {
        if (operateTime == null) operateTime = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getReceivableId() { return receivableId; }
    public void setReceivableId(Long receivableId) { this.receivableId = receivableId; }
    public String getOperatorUsername() { return operatorUsername; }
    public void setOperatorUsername(String operatorUsername) { this.operatorUsername = operatorUsername; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    public String getOperatorRole() { return operatorRole; }
    public void setOperatorRole(String operatorRole) { this.operatorRole = operatorRole; }
    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public LocalDateTime getOperateTime() { return operateTime; }
    public void setOperateTime(LocalDateTime operateTime) { this.operateTime = operateTime; }
}
