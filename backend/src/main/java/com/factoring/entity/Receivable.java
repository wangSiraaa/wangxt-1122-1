package com.factoring.entity;

import com.factoring.common.ReceivableStatusEnum;
import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "t_receivable")
public class Receivable implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String receivableNo;

    @Column(length = 128)
    private String contractNo;

    @Column(nullable = false, length = 256)
    private String sellerName;

    @Column(length = 64)
    private String sellerTaxNo;

    @Column(nullable = false, length = 256)
    private String debtorName;

    @Column(length = 64)
    private String debtorTaxNo;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Column(precision = 18, scale = 2)
    private BigDecimal transferAmount;

    @Column(precision = 18, scale = 2)
    private BigDecimal loanAmount;

    private LocalDate dueDate;

    @Column(length = 512)
    private String remark;

    @Column(nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private ReceivableStatusEnum status;

    @Column(length = 64)
    private String managerUsername;

    @Column(length = 64)
    private String managerName;

    private LocalDateTime submitTime;

    @Column(length = 64)
    private String riskUsername;

    @Column(length = 64)
    private String riskName;

    @Column(length = 1024)
    private String verifyOpinion;

    private LocalDateTime verifyTime;

    private Boolean buyerReceipt;

    @Column(length = 1024)
    private String buyerReceiptRemark;

    private LocalDateTime buyerReceiptTime;

    @Column(length = 64)
    private String buyerReceiptOperator;

    @Column(precision = 18, scale = 2)
    private BigDecimal buyerReceiptAmount;

    private Boolean tradeBackgroundVerified;

    @Column(length = 512)
    private String tradeBackgroundRemark;

    private Boolean pledged;

    @Column(length = 512)
    private String pledgedRemark;

    @Column(length = 256)
    private String repaymentAccount;

    @Column(length = 512)
    private String supplementRemark;

    private Boolean auditCorrectionFlag;

    @Column(length = 512)
    private String auditCorrectionRemark;

    @Column(length = 64)
    private String fundUsername;

    @Column(length = 64)
    private String fundName;

    private LocalDateTime loanTime;

    @Column(length = 64)
    private String loanVoucherNo;

    @Column(length = 1024)
    private String loanRemark;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @PrePersist
    public void prePersist() {
        if (status == null) status = ReceivableStatusEnum.DRAFT;
        if (buyerReceipt == null) buyerReceipt = false;
        if (tradeBackgroundVerified == null) tradeBackgroundVerified = false;
        if (pledged == null) pledged = false;
        if (auditCorrectionFlag == null) auditCorrectionFlag = false;
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updateTime = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getReceivableNo() { return receivableNo; }
    public void setReceivableNo(String receivableNo) { this.receivableNo = receivableNo; }
    public String getContractNo() { return contractNo; }
    public void setContractNo(String contractNo) { this.contractNo = contractNo; }
    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }
    public String getSellerTaxNo() { return sellerTaxNo; }
    public void setSellerTaxNo(String sellerTaxNo) { this.sellerTaxNo = sellerTaxNo; }
    public String getDebtorName() { return debtorName; }
    public void setDebtorName(String debtorName) { this.debtorName = debtorName; }
    public String getDebtorTaxNo() { return debtorTaxNo; }
    public void setDebtorTaxNo(String debtorTaxNo) { this.debtorTaxNo = debtorTaxNo; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public BigDecimal getTransferAmount() { return transferAmount; }
    public void setTransferAmount(BigDecimal transferAmount) { this.transferAmount = transferAmount; }
    public BigDecimal getLoanAmount() { return loanAmount; }
    public void setLoanAmount(BigDecimal loanAmount) { this.loanAmount = loanAmount; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public ReceivableStatusEnum getStatus() { return status; }
    public void setStatus(ReceivableStatusEnum status) { this.status = status; }
    public String getManagerUsername() { return managerUsername; }
    public void setManagerUsername(String managerUsername) { this.managerUsername = managerUsername; }
    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }
    public LocalDateTime getSubmitTime() { return submitTime; }
    public void setSubmitTime(LocalDateTime submitTime) { this.submitTime = submitTime; }
    public String getRiskUsername() { return riskUsername; }
    public void setRiskUsername(String riskUsername) { this.riskUsername = riskUsername; }
    public String getRiskName() { return riskName; }
    public void setRiskName(String riskName) { this.riskName = riskName; }
    public String getVerifyOpinion() { return verifyOpinion; }
    public void setVerifyOpinion(String verifyOpinion) { this.verifyOpinion = verifyOpinion; }
    public LocalDateTime getVerifyTime() { return verifyTime; }
    public void setVerifyTime(LocalDateTime verifyTime) { this.verifyTime = verifyTime; }
    public Boolean getBuyerReceipt() { return buyerReceipt; }
    public void setBuyerReceipt(Boolean buyerReceipt) { this.buyerReceipt = buyerReceipt; }
    public String getBuyerReceiptRemark() { return buyerReceiptRemark; }
    public void setBuyerReceiptRemark(String buyerReceiptRemark) { this.buyerReceiptRemark = buyerReceiptRemark; }
    public LocalDateTime getBuyerReceiptTime() { return buyerReceiptTime; }
    public void setBuyerReceiptTime(LocalDateTime buyerReceiptTime) { this.buyerReceiptTime = buyerReceiptTime; }
    public String getBuyerReceiptOperator() { return buyerReceiptOperator; }
    public void setBuyerReceiptOperator(String buyerReceiptOperator) { this.buyerReceiptOperator = buyerReceiptOperator; }
    public BigDecimal getBuyerReceiptAmount() { return buyerReceiptAmount; }
    public void setBuyerReceiptAmount(BigDecimal buyerReceiptAmount) { this.buyerReceiptAmount = buyerReceiptAmount; }
    public Boolean getTradeBackgroundVerified() { return tradeBackgroundVerified; }
    public void setTradeBackgroundVerified(Boolean tradeBackgroundVerified) { this.tradeBackgroundVerified = tradeBackgroundVerified; }
    public String getTradeBackgroundRemark() { return tradeBackgroundRemark; }
    public void setTradeBackgroundRemark(String tradeBackgroundRemark) { this.tradeBackgroundRemark = tradeBackgroundRemark; }
    public Boolean getPledged() { return pledged; }
    public void setPledged(Boolean pledged) { this.pledged = pledged; }
    public String getPledgedRemark() { return pledgedRemark; }
    public void setPledgedRemark(String pledgedRemark) { this.pledgedRemark = pledgedRemark; }
    public String getRepaymentAccount() { return repaymentAccount; }
    public void setRepaymentAccount(String repaymentAccount) { this.repaymentAccount = repaymentAccount; }
    public String getSupplementRemark() { return supplementRemark; }
    public void setSupplementRemark(String supplementRemark) { this.supplementRemark = supplementRemark; }
    public Boolean getAuditCorrectionFlag() { return auditCorrectionFlag; }
    public void setAuditCorrectionFlag(Boolean auditCorrectionFlag) { this.auditCorrectionFlag = auditCorrectionFlag; }
    public String getAuditCorrectionRemark() { return auditCorrectionRemark; }
    public void setAuditCorrectionRemark(String auditCorrectionRemark) { this.auditCorrectionRemark = auditCorrectionRemark; }
    public String getFundUsername() { return fundUsername; }
    public void setFundUsername(String fundUsername) { this.fundUsername = fundUsername; }
    public String getFundName() { return fundName; }
    public void setFundName(String fundName) { this.fundName = fundName; }
    public LocalDateTime getLoanTime() { return loanTime; }
    public void setLoanTime(LocalDateTime loanTime) { this.loanTime = loanTime; }
    public String getLoanVoucherNo() { return loanVoucherNo; }
    public void setLoanVoucherNo(String loanVoucherNo) { this.loanVoucherNo = loanVoucherNo; }
    public String getLoanRemark() { return loanRemark; }
    public void setLoanRemark(String loanRemark) { this.loanRemark = loanRemark; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
