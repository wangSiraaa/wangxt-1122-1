package com.factoring.common;

public enum InvoiceVerifyStatusEnum {
    NOT_VERIFIED("NOT_VERIFIED", "未验真"),
    VERIFYING("VERIFYING", "验真中"),
    VERIFIED("VERIFIED", "已验真"),
    VERIFY_FAILED("VERIFY_FAILED", "验真失败");

    private final String code;
    private final String desc;

    InvoiceVerifyStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() { return code; }
    public String getDesc() { return desc; }

    public static InvoiceVerifyStatusEnum of(String code) {
        for (InvoiceVerifyStatusEnum e : values()) {
            if (e.code.equalsIgnoreCase(code)) return e;
        }
        return null;
    }
}
