package com.factoring.entity;

import com.factoring.common.InvoiceVerifyStatusEnum;
import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "t_invoice")
public class Invoice implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long receivableId;

    @Column(nullable = false, length = 64)
    private String invoiceNo;

    @Column(nullable = false, length = 64)
    private String invoiceCode;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal taxAmount;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;

    private LocalDate invoiceDate;

    @Column(length = 256)
    private String buyerName;

    @Column(length = 64)
    private String buyerTaxNo;

    @Column(length = 256)
    private String sellerName;

    @Column(length = 64)
    private String sellerTaxNo;

    @Column(nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private InvoiceVerifyStatusEnum verifyStatus;

    @Column(length = 512)
    private String verifyRemark;

    private LocalDateTime verifyTime;

    @Column(length = 64)
    private String verifyOperator;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @PrePersist
    public void prePersist() {
        if (verifyStatus == null) verifyStatus = InvoiceVerifyStatusEnum.NOT_VERIFIED;
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updateTime = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getReceivableId() { return receivableId; }
    public void setReceivableId(Long receivableId) { this.receivableId = receivableId; }
    public String getInvoiceNo() { return invoiceNo; }
    public void setInvoiceNo(String invoiceNo) { this.invoiceNo = invoiceNo; }
    public String getInvoiceCode() { return invoiceCode; }
    public void setInvoiceCode(String invoiceCode) { this.invoiceCode = invoiceCode; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public LocalDate getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(LocalDate invoiceDate) { this.invoiceDate = invoiceDate; }
    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }
    public String getBuyerTaxNo() { return buyerTaxNo; }
    public void setBuyerTaxNo(String buyerTaxNo) { this.buyerTaxNo = buyerTaxNo; }
    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }
    public String getSellerTaxNo() { return sellerTaxNo; }
    public void setSellerTaxNo(String sellerTaxNo) { this.sellerTaxNo = sellerTaxNo; }
    public InvoiceVerifyStatusEnum getVerifyStatus() { return verifyStatus; }
    public void setVerifyStatus(InvoiceVerifyStatusEnum verifyStatus) { this.verifyStatus = verifyStatus; }
    public String getVerifyRemark() { return verifyRemark; }
    public void setVerifyRemark(String verifyRemark) { this.verifyRemark = verifyRemark; }
    public LocalDateTime getVerifyTime() { return verifyTime; }
    public void setVerifyTime(LocalDateTime verifyTime) { this.verifyTime = verifyTime; }
    public String getVerifyOperator() { return verifyOperator; }
    public void setVerifyOperator(String verifyOperator) { this.verifyOperator = verifyOperator; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
