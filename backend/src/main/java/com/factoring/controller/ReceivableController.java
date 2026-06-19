package com.factoring.controller;

import com.factoring.common.Result;
import com.factoring.entity.*;
import com.factoring.service.DemoDataInitService;
import com.factoring.service.ReceivableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ReceivableController {

    @Autowired
    private ReceivableService receivableService;

    @Autowired
    private DemoDataInitService demoDataInitService;

    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("ok");
    }

    @PostMapping("/auth/login")
    public Result<SysUser> login(@RequestBody LoginReq req) {
        SysUser user = receivableService.login(req.getUsername(), req.getPassword());
        if (user == null) return Result.error("用户名或密码错误");
        return Result.success(user);
    }

    @PostMapping("/auth/logout")
    public Result<Void> logout(@RequestHeader("X-Username") String username) {
        receivableService.logout(username);
        return Result.success();
    }

    @GetMapping("/users")
    public Result<List<SysUser>> listUsers() {
        return Result.success(receivableService.listUsers());
    }

    @GetMapping("/receivables")
    public Result<List<Receivable>> list(@RequestHeader("X-Username") String username) {
        return Result.success(receivableService.listReceivables(username));
    }

    @GetMapping("/receivables/{id}")
    public Result<Receivable> detail(@RequestHeader("X-Username") String username, @PathVariable Long id) {
        return Result.success(receivableService.getReceivableDetail(username, id));
    }

    @GetMapping("/receivables/{id}/invoices")
    public Result<List<Invoice>> invoices(@RequestHeader("X-Username") String username, @PathVariable Long id) {
        return Result.success(receivableService.listInvoices(username, id));
    }

    @GetMapping("/receivables/{id}/logs")
    public Result<List<OperationLog>> logs(@RequestHeader("X-Username") String username, @PathVariable Long id) {
        return Result.success(receivableService.listLogs(username, id));
    }

    @PostMapping("/receivables")
    public Result<Receivable> create(@RequestHeader("X-Username") String username, @RequestBody CreateReq req) {
        return Result.success(receivableService.createReceivable(username, req.getReceivable(), req.getInvoices()));
    }

    @PutMapping("/receivables/{id}")
    public Result<Receivable> update(@RequestHeader("X-Username") String username, @PathVariable Long id, @RequestBody CreateReq req) {
        return Result.success(receivableService.updateReceivable(username, id, req.getReceivable(), req.getInvoices()));
    }

    @PostMapping("/receivables/{id}/invoice-verify")
    public Result<Invoice> verifyInvoice(@RequestHeader("X-Username") String username, @PathVariable Long id, @RequestBody VerifyInvoiceReq req) {
        return Result.success(receivableService.verifyInvoice(username, req.getInvoiceId()));
    }

    @PostMapping("/receivables/{id}/submit")
    public Result<Receivable> submit(@RequestHeader("X-Username") String username, @PathVariable Long id) {
        return Result.success(receivableService.submitToRisk(username, id));
    }

    @PostMapping("/receivables/{id}/verify")
    public Result<Receivable> verify(@RequestHeader("X-Username") String username, @PathVariable Long id, @RequestBody VerifyReq req) {
        return Result.success(receivableService.verifyReceivable(username, id, req.isPassed(), req.getOpinion()));
    }

    @PostMapping("/receivables/{id}/buyer-receipt")
    public Result<Receivable> buyerReceipt(@RequestHeader("X-Username") String username, @PathVariable Long id, @RequestBody BuyerReceiptReq req) {
        return Result.success(receivableService.recordBuyerReceipt(username, id, req.isConfirmed(), req.getRemark()));
    }

    @PostMapping("/receivables/{id}/transfer-fund")
    public Result<Receivable> transferFund(@RequestHeader("X-Username") String username, @PathVariable Long id) {
        return Result.success(receivableService.transferToFund(username, id));
    }

    @PostMapping("/receivables/{id}/loan")
    public Result<Receivable> loan(@RequestHeader("X-Username") String username, @PathVariable Long id, @RequestBody LoanReq req) {
        return Result.success(receivableService.grantLoan(username, id, req.getLoanAmount(), req.getLoanVoucherNo(), req.getLoanRemark()));
    }

    @PostMapping("/demo/run")
    public Result<Map<String, Object>> runDemo() {
        return Result.success(demoDataInitService.runFullDemoFlow());
    }

    public static class LoginReq {
        private String username;
        private String password;
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class CreateReq {
        private Receivable receivable;
        private List<Invoice> invoices;
        public Receivable getReceivable() { return receivable; }
        public void setReceivable(Receivable receivable) { this.receivable = receivable; }
        public List<Invoice> getInvoices() { return invoices; }
        public void setInvoices(List<Invoice> invoices) { this.invoices = invoices; }
    }

    public static class VerifyInvoiceReq {
        private Long invoiceId;
        public Long getInvoiceId() { return invoiceId; }
        public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }
    }

    public static class VerifyReq {
        private boolean passed;
        private String opinion;
        public boolean isPassed() { return passed; }
        public void setPassed(boolean passed) { this.passed = passed; }
        public String getOpinion() { return opinion; }
        public void setOpinion(String opinion) { this.opinion = opinion; }
    }

    public static class BuyerReceiptReq {
        private boolean confirmed;
        private String remark;
        public boolean isConfirmed() { return confirmed; }
        public void setConfirmed(boolean confirmed) { this.confirmed = confirmed; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
    }

    public static class LoanReq {
        private BigDecimal loanAmount;
        private String loanVoucherNo;
        private String loanRemark;
        public BigDecimal getLoanAmount() { return loanAmount; }
        public void setLoanAmount(BigDecimal loanAmount) { this.loanAmount = loanAmount; }
        public String getLoanVoucherNo() { return loanVoucherNo; }
        public void setLoanVoucherNo(String loanVoucherNo) { this.loanVoucherNo = loanVoucherNo; }
        public String getLoanRemark() { return loanRemark; }
        public void setLoanRemark(String loanRemark) { this.loanRemark = loanRemark; }
    }
}
