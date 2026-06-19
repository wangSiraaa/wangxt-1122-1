package com.factoring.common;

public enum ReceivableStatusEnum {
    DRAFT("DRAFT", "草稿"),
    PENDING_VERIFY("PENDING_VERIFY", "待风控核验"),
    VERIFY_PASSED("VERIFY_PASSED", "核验通过"),
    VERIFY_REJECTED("VERIFY_REJECTED", "核验驳回"),
    BUYER_CONFIRMED("BUYER_CONFIRMED", "买方已回执"),
    PENDING_SUPPLEMENT("PENDING_SUPPLEMENT", "待补充材料"),
    PENDING_LOAN("PENDING_LOAN", "待放款"),
    LOANED("LOANED", "已放款"),
    REJECTED("REJECTED", "已驳回");

    private final String code;
    private final String desc;

    ReceivableStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() { return code; }
    public String getDesc() { return desc; }

    public static ReceivableStatusEnum of(String code) {
        for (ReceivableStatusEnum e : values()) {
            if (e.code.equalsIgnoreCase(code)) return e;
        }
        return null;
    }
}
