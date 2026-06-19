package com.factoring.service;

import com.factoring.common.InvoiceVerifyStatusEnum;
import com.factoring.common.ReceivableStatusEnum;
import com.factoring.common.RoleEnum;
import com.factoring.entity.*;
import com.factoring.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DemoDataInitService implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(DemoDataInitService.class);

    @Autowired
    private SysUserRepository sysUserRepository;

    @Autowired
    private ReceivableRepository receivableRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private OperationLogRepository operationLogRepository;

    @Autowired
    private ReceivableService receivableService;

    @Override
    public void run(ApplicationArguments args) {
        initUsers();
        log.info("演示数据初始化完成，登录账号：manager/123456（客户经理）、risk/123456（风控）、fund/123456（资金岗）");
    }

    @Transactional(rollbackFor = Exception.class)
    public void initUsers() {
        if (sysUserRepository.count() > 0) return;

        SysUser manager = new SysUser();
        manager.setUsername("manager");
        manager.setPassword("123456");
        manager.setRealName("张经理");
        manager.setRole(RoleEnum.MANAGER);
        manager.setPhone("13800000001");
        sysUserRepository.save(manager);

        SysUser risk = new SysUser();
        risk.setUsername("risk");
        risk.setPassword("123456");
        risk.setRealName("李风控");
        risk.setRole(RoleEnum.RISK);
        risk.setPhone("13800000002");
        sysUserRepository.save(risk);

        SysUser fund = new SysUser();
        fund.setUsername("fund");
        fund.setPassword("123456");
        fund.setRealName("王资金");
        fund.setRole(RoleEnum.FUND);
        fund.setPhone("13800000003");
        sysUserRepository.save(fund);
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> runFullDemoFlow() {
        initUsers();
        operationLogRepository.deleteAll();
        invoiceRepository.deleteAll();
        receivableRepository.deleteAll();

        try {
            receivableService.login("manager", "123456");

            Receivable dto = new Receivable();
            dto.setContractNo("HT-2025-0088");
            dto.setSellerName("上海华贸供应链有限公司");
            dto.setSellerTaxNo("91310000MA1FL0ABCD");
            dto.setDebtorName("深圳华为技术有限公司");
            dto.setDebtorTaxNo("914403001922038619");
            dto.setTotalAmount(new BigDecimal("5000000.00"));
            dto.setTransferAmount(new BigDecimal("4000000.00"));
            dto.setDueDate(LocalDate.now().plusMonths(6));
            dto.setRemark("演示业务：上海华贸转让对华为的应收账款500万，申请融资400万");

            List<Invoice> invList = new ArrayList<>();
            Invoice inv1 = new Invoice();
            inv1.setInvoiceCode("031002100111");
            inv1.setInvoiceNo("88234501");
            inv1.setAmount(new BigDecimal("2654867.26"));
            inv1.setTaxAmount(new BigDecimal("345132.74"));
            inv1.setTotalAmount(new BigDecimal("3000000.00"));
            inv1.setInvoiceDate(LocalDate.now().minusDays(30));
            inv1.setBuyerName("深圳华为技术有限公司");
            inv1.setBuyerTaxNo("914403001922038619");
            inv1.setSellerName("上海华贸供应链有限公司");
            inv1.setSellerTaxNo("91310000MA1FL0ABCD");
            invList.add(inv1);

            Invoice inv2 = new Invoice();
            inv2.setInvoiceCode("031002100111");
            inv2.setInvoiceNo("88234502");
            inv2.setAmount(new BigDecimal("1769911.50"));
            inv2.setTaxAmount(new BigDecimal("230088.50"));
            inv2.setTotalAmount(new BigDecimal("2000000.00"));
            inv2.setInvoiceDate(LocalDate.now().minusDays(20));
            inv2.setBuyerName("深圳华为技术有限公司");
            inv2.setBuyerTaxNo("914403001922038619");
            inv2.setSellerName("上海华贸供应链有限公司");
            inv2.setSellerTaxNo("91310000MA1FL0ABCD");
            invList.add(inv2);

            Receivable r = receivableService.createReceivable("manager", dto, invList);
            Long id = r.getId();

            List<Invoice> savedInvoices = receivableService.listInvoices("manager", id);
            for (Invoice inv : savedInvoices) {
                receivableService.verifyInvoice("manager", inv.getId());
            }

            r = receivableService.submitToRisk("manager", id);
            receivableService.logout("manager");

            receivableService.login("risk", "123456");
            r = receivableService.verifyReceivable("risk", id, true,
                    "买方华为资质优良，交易真实有效，发票已验真，建议通过；转让金额400万在额度范围内。");

            r = receivableService.recordBuyerReceipt("risk", id, true,
                    "已收到深圳华为技术有限公司采购部确认函（编号HW-QR-20250618），买方确认对上海华贸的应付账款500万元真实有效，同意转让至商业保理公司。");

            r = receivableService.transferToFund("risk", id);
            receivableService.logout("risk");

            receivableService.login("fund", "123456");
            r = receivableService.grantLoan("fund", id, new BigDecimal("4000000.00"),
                    "FK-2025-0618-0001", "按转让金额80%发放，年化利率6.5%，期限6个月，放款账户：上海华贸XXXX8888");
            receivableService.logout("fund");

            return Map.of(
                    "receivable", r,
                    "invoices", savedInvoices,
                    "logs", operationLogRepository.findByReceivableIdOrderByOperateTimeDesc(id)
            );
        } catch (Exception e) {
            log.error("演示流程执行失败", e);
            throw new RuntimeException("演示流程执行失败：" + e.getMessage(), e);
        }
    }
}
