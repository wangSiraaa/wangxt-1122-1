package com.factoring.service;

import com.factoring.common.InvoiceVerifyStatusEnum;
import com.factoring.common.ReceivableStatusEnum;
import com.factoring.common.RoleEnum;
import com.factoring.entity.*;
import com.factoring.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ReceivableService {

    @Autowired
    private ReceivableRepository receivableRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private OperationLogRepository operationLogRepository;

    @Autowired
    private SysUserRepository sysUserRepository;

    private final Map<String, SysUser> loginCache = new ConcurrentHashMap<>();
    private final AtomicLong receivableNoSeq = new AtomicLong(1L);

    public SysUser login(String username, String password) {
        SysUser user = sysUserRepository.findByUsername(username).orElse(null);
        if (user == null) return null;
        if (!user.getPassword().equals(password)) return null;
        loginCache.put(username, user);
        return user;
    }

    public SysUser getLoginUser(String username) {
        return loginCache.get(username);
    }

    public void logout(String username) {
        loginCache.remove(username);
    }

    public List<SysUser> listUsers() {
        return sysUserRepository.findAll();
    }

    private void checkLogin(String username, RoleEnum... allowedRoles) {
        SysUser user = getLoginUser(username);
        if (user == null) throw new RuntimeException("用户未登录");
        if (allowedRoles != null && allowedRoles.length > 0) {
            boolean ok = false;
            for (RoleEnum r : allowedRoles) {
                if (user.getRole() == r) { ok = true; break; }
            }
            if (!ok) throw new RuntimeException("权限不足，当前角色：" + user.getRole().getDesc());
        }
    }

    private String generateReceivableNo() {
        return "YSZK" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", receivableNoSeq.getAndIncrement());
    }

    private void addLog(Long receivableId, SysUser user, String operation, String detail) {
        OperationLog opLog = new OperationLog();
        opLog.setReceivableId(receivableId);
        opLog.setOperatorUsername(user.getUsername());
        opLog.setOperatorName(user.getRealName());
        opLog.setOperatorRole(user.getRole().getDesc());
        opLog.setOperation(operation);
        opLog.setDetail(detail);
        opLog.setOperateTime(LocalDateTime.now());
        operationLogRepository.save(opLog);
    }

    // ==================== 客户经理 ====================

    @Transactional(rollbackFor = Exception.class)
    public Receivable createReceivable(String username, Receivable dto, List<Invoice> invoiceList) {
        checkLogin(username, RoleEnum.MANAGER);
        SysUser user = getLoginUser(username);

        Receivable r = new Receivable();
        r.setReceivableNo(generateReceivableNo());
        r.setContractNo(dto.getContractNo());
        r.setSellerName(dto.getSellerName());
        r.setSellerTaxNo(dto.getSellerTaxNo());
        r.setDebtorName(dto.getDebtorName());
        r.setDebtorTaxNo(dto.getDebtorTaxNo());
        r.setTotalAmount(dto.getTotalAmount());
        r.setTransferAmount(dto.getTransferAmount());
        r.setDueDate(dto.getDueDate());
        r.setRemark(dto.getRemark());
        r.setStatus(ReceivableStatusEnum.DRAFT);
        r.setManagerUsername(user.getUsername());
        r.setManagerName(user.getRealName());
        r = receivableRepository.save(r);

        BigDecimal invTotal = BigDecimal.ZERO;
        if (invoiceList != null) {
            for (Invoice inv : invoiceList) {
                inv.setReceivableId(r.getId());
                inv.setVerifyStatus(InvoiceVerifyStatusEnum.NOT_VERIFIED);
                invoiceRepository.save(inv);
                invTotal = invTotal.add(inv.getTotalAmount() != null ? inv.getTotalAmount() : BigDecimal.ZERO);
            }
        }

        addLog(r.getId(), user, "创建应收账款",
                "应收账款编号：" + r.getReceivableNo() + "，金额：" + r.getTotalAmount() + "，发票数量：" + (invoiceList == null ? 0 : invoiceList.size()));
        return r;
    }

    @Transactional(rollbackFor = Exception.class)
    public Receivable updateReceivable(String username, Long id, Receivable dto, List<Invoice> invoiceList) {
        checkLogin(username, RoleEnum.MANAGER);
        SysUser user = getLoginUser(username);

        Receivable r = receivableRepository.findById(id).orElseThrow(() -> new RuntimeException("应收账款不存在"));

        if (r.getStatus() == ReceivableStatusEnum.LOANED) {
            boolean debtorChanged = !r.getDebtorName().equals(dto.getDebtorName() == null ? r.getDebtorName() : dto.getDebtorName())
                    || (dto.getDebtorTaxNo() != null && !dto.getDebtorTaxNo().equals(r.getDebtorTaxNo()));
            if (debtorChanged) {
                throw new RuntimeException("业务规则校验失败：已放款的应收账款不能修改债务人信息");
            }
        }

        if (r.getStatus() != ReceivableStatusEnum.DRAFT && r.getStatus() != ReceivableStatusEnum.VERIFY_REJECTED
                && r.getStatus() != ReceivableStatusEnum.REJECTED) {
            throw new RuntimeException("当前状态不允许修改，状态：" + r.getStatus().getDesc());
        }

        r.setContractNo(dto.getContractNo());
        r.setSellerName(dto.getSellerName());
        r.setSellerTaxNo(dto.getSellerTaxNo());
        if (r.getStatus() != ReceivableStatusEnum.LOANED) {
            r.setDebtorName(dto.getDebtorName());
            r.setDebtorTaxNo(dto.getDebtorTaxNo());
        }
        r.setTotalAmount(dto.getTotalAmount());
        r.setTransferAmount(dto.getTransferAmount());
        r.setDueDate(dto.getDueDate());
        r.setRemark(dto.getRemark());
        r = receivableRepository.save(r);

        if (invoiceList != null) {
            invoiceRepository.deleteByReceivableId(r.getId());
            for (Invoice inv : invoiceList) {
                inv.setId(null);
                inv.setReceivableId(r.getId());
                inv.setVerifyStatus(InvoiceVerifyStatusEnum.NOT_VERIFIED);
                invoiceRepository.save(inv);
            }
        }

        addLog(r.getId(), user, "修改应收账款",
                "应收账款编号：" + r.getReceivableNo() + "，金额：" + r.getTotalAmount());
        return r;
    }

    @Transactional(rollbackFor = Exception.class)
    public Invoice verifyInvoice(String username, Long invoiceId) {
        checkLogin(username, RoleEnum.MANAGER);
        SysUser user = getLoginUser(username);

        Invoice inv = invoiceRepository.findById(invoiceId).orElseThrow(() -> new RuntimeException("发票不存在"));

        Receivable r = receivableRepository.findById(inv.getReceivableId()).orElseThrow(() -> new RuntimeException("应收账款不存在"));
        if (r.getStatus() != ReceivableStatusEnum.DRAFT && r.getStatus() != ReceivableStatusEnum.VERIFY_REJECTED) {
            throw new RuntimeException("当前状态不允许操作发票");
        }
        if (!r.getManagerUsername().equals(user.getUsername())) {
            throw new RuntimeException("只能操作自己的单据");
        }

        inv.setVerifyStatus(InvoiceVerifyStatusEnum.VERIFIED);
        inv.setVerifyTime(LocalDateTime.now());
        inv.setVerifyOperator(user.getUsername());
        inv.setVerifyRemark("模拟税务系统验真通过，发票号码：" + inv.getInvoiceNo());
        inv = invoiceRepository.save(inv);

        addLog(r.getId(), user, "发票验真",
                "发票号码：" + inv.getInvoiceNo() + "，金额：" + inv.getTotalAmount() + "，验真结果：通过");
        return inv;
    }

    @Transactional(rollbackFor = Exception.class)
    public Receivable submitToRisk(String username, Long id) {
        checkLogin(username, RoleEnum.MANAGER);
        SysUser user = getLoginUser(username);

        Receivable r = receivableRepository.findById(id).orElseThrow(() -> new RuntimeException("应收账款不存在"));

        if (!r.getManagerUsername().equals(user.getUsername())) {
            throw new RuntimeException("只能提交自己的单据");
        }
        if (r.getStatus() != ReceivableStatusEnum.DRAFT && r.getStatus() != ReceivableStatusEnum.VERIFY_REJECTED
                && r.getStatus() != ReceivableStatusEnum.REJECTED) {
            throw new RuntimeException("当前状态不允许提交，状态：" + r.getStatus().getDesc());
        }

        List<Invoice> invoices = invoiceRepository.findByReceivableIdOrderByCreateTimeDesc(r.getId());
        if (invoices == null || invoices.isEmpty()) {
            throw new RuntimeException("业务规则校验失败：请先登记发票");
        }
        for (Invoice inv : invoices) {
            if (inv.getVerifyStatus() != InvoiceVerifyStatusEnum.VERIFIED) {
                throw new RuntimeException("业务规则校验失败：发票【" + inv.getInvoiceNo() + "】未验真，不能转让");
            }
        }

        r.setStatus(ReceivableStatusEnum.PENDING_VERIFY);
        r.setSubmitTime(LocalDateTime.now());
        r = receivableRepository.save(r);

        addLog(r.getId(), user, "提交风控核验",
                "应收账款编号：" + r.getReceivableNo() + "，提交风控复核处理");
        return r;
    }

    // ==================== 风控复核 ====================

    @Transactional(rollbackFor = Exception.class)
    public Receivable verifyReceivable(String username, Long id, boolean passed, String opinion) {
        checkLogin(username, RoleEnum.RISK);
        SysUser user = getLoginUser(username);

        Receivable r = receivableRepository.findById(id).orElseThrow(() -> new RuntimeException("应收账款不存在"));
        if (r.getStatus() != ReceivableStatusEnum.PENDING_VERIFY) {
            throw new RuntimeException("当前状态不允许核验，状态：" + r.getStatus().getDesc());
        }

        r.setRiskUsername(user.getUsername());
        r.setRiskName(user.getRealName());
        r.setVerifyOpinion(opinion);
        r.setVerifyTime(LocalDateTime.now());
        if (passed) {
            r.setStatus(ReceivableStatusEnum.VERIFY_PASSED);
            addLog(r.getId(), user, "风控核验通过", "核验意见：" + opinion);
        } else {
            r.setStatus(ReceivableStatusEnum.VERIFY_REJECTED);
            addLog(r.getId(), user, "风控核验驳回", "驳回原因：" + opinion);
        }
        return receivableRepository.save(r);
    }

    @Transactional(rollbackFor = Exception.class)
    public Receivable recordBuyerReceipt(String username, Long id, boolean confirmed, String remark) {
        checkLogin(username, RoleEnum.RISK);
        SysUser user = getLoginUser(username);

        Receivable r = receivableRepository.findById(id).orElseThrow(() -> new RuntimeException("应收账款不存在"));
        if (r.getStatus() != ReceivableStatusEnum.VERIFY_PASSED && r.getStatus() != ReceivableStatusEnum.BUYER_CONFIRMED
                && r.getStatus() != ReceivableStatusEnum.PENDING_LOAN && r.getStatus() != ReceivableStatusEnum.LOANED) {
            if (r.getStatus() != ReceivableStatusEnum.VERIFY_PASSED) {
                throw new RuntimeException("当前状态不允许登记买方回执，请先完成风控核验");
            }
        }

        r.setBuyerReceipt(confirmed);
        r.setBuyerReceiptRemark(remark);
        r.setBuyerReceiptTime(LocalDateTime.now());
        r.setBuyerReceiptOperator(user.getUsername());
        if (confirmed && r.getStatus() == ReceivableStatusEnum.VERIFY_PASSED) {
            r.setStatus(ReceivableStatusEnum.BUYER_CONFIRMED);
        }
        r = receivableRepository.save(r);

        addLog(r.getId(), user, confirmed ? "登记买方确认回执" : "登记买方未回执",
                "回执内容：" + remark);
        return r;
    }

    @Transactional(rollbackFor = Exception.class)
    public Receivable transferToFund(String username, Long id) {
        checkLogin(username, RoleEnum.RISK);
        SysUser user = getLoginUser(username);

        Receivable r = receivableRepository.findById(id).orElseThrow(() -> new RuntimeException("应收账款不存在"));
        if (r.getStatus() != ReceivableStatusEnum.BUYER_CONFIRMED) {
            throw new RuntimeException("当前状态不允许流转至资金岗，请先登记买方回执，状态：" + r.getStatus().getDesc());
        }
        if (!Boolean.TRUE.equals(r.getBuyerReceipt())) {
            throw new RuntimeException("业务规则校验失败：买方未回执，不能进入放款环节");
        }

        r.setStatus(ReceivableStatusEnum.PENDING_LOAN);
        r = receivableRepository.save(r);

        addLog(r.getId(), user, "流转至资金岗",
                "应收账款编号：" + r.getReceivableNo() + "，进入放款环节");
        return r;
    }

    // ==================== 资金岗 ====================

    @Transactional(rollbackFor = Exception.class)
    public Receivable grantLoan(String username, Long id, BigDecimal loanAmount, String loanVoucherNo, String loanRemark) {
        checkLogin(username, RoleEnum.FUND);
        SysUser user = getLoginUser(username);

        Receivable r = receivableRepository.findById(id).orElseThrow(() -> new RuntimeException("应收账款不存在"));
        if (r.getStatus() != ReceivableStatusEnum.PENDING_LOAN) {
            throw new RuntimeException("当前状态不允许放款，状态：" + r.getStatus().getDesc());
        }
        if (!Boolean.TRUE.equals(r.getBuyerReceipt())) {
            throw new RuntimeException("业务规则校验失败：买方未回执，不能放款");
        }

        r.setFundUsername(user.getUsername());
        r.setFundName(user.getRealName());
        r.setLoanAmount(loanAmount);
        r.setLoanVoucherNo(loanVoucherNo);
        r.setLoanRemark(loanRemark);
        r.setLoanTime(LocalDateTime.now());
        r.setStatus(ReceivableStatusEnum.LOANED);
        r = receivableRepository.save(r);

        addLog(r.getId(), user, "发起放款",
                "放款金额：" + loanAmount + "，放款凭证号：" + loanVoucherNo + "，备注：" + loanRemark);
        return r;
    }

    // ==================== 查询 ====================

    public List<Receivable> listReceivables(String username) {
        SysUser user = getLoginUser(username);
        if (user == null) throw new RuntimeException("用户未登录");

        if (user.getRole() == RoleEnum.MANAGER) {
            return receivableRepository.findByManagerUsernameOrderByCreateTimeDesc(user.getUsername());
        }
        if (user.getRole() == RoleEnum.RISK) {
            return receivableRepository.findByStatusInOrderByCreateTimeDesc(List.of(
                    ReceivableStatusEnum.PENDING_VERIFY,
                    ReceivableStatusEnum.VERIFY_PASSED,
                    ReceivableStatusEnum.VERIFY_REJECTED,
                    ReceivableStatusEnum.BUYER_CONFIRMED,
                    ReceivableStatusEnum.PENDING_LOAN,
                    ReceivableStatusEnum.LOANED,
                    ReceivableStatusEnum.REJECTED
            ));
        }
        if (user.getRole() == RoleEnum.FUND) {
            return receivableRepository.findByStatusInOrderByCreateTimeDesc(List.of(
                    ReceivableStatusEnum.PENDING_LOAN,
                    ReceivableStatusEnum.LOANED
            ));
        }
        return receivableRepository.findAllOrderByCreateTimeDesc();
    }

    public Receivable getReceivableDetail(String username, Long id) {
        checkLogin(username);
        return receivableRepository.findById(id).orElseThrow(() -> new RuntimeException("应收账款不存在"));
    }

    public List<Invoice> listInvoices(String username, Long receivableId) {
        checkLogin(username);
        return invoiceRepository.findByReceivableIdOrderByCreateTimeDesc(receivableId);
    }

    public List<OperationLog> listLogs(String username, Long receivableId) {
        checkLogin(username);
        return operationLogRepository.findByReceivableIdOrderByOperateTimeDesc(receivableId);
    }
}
