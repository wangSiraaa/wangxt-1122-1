package com.factoring.common;

public enum RoleEnum {
    MANAGER("MANAGER", "客户经理"),
    RISK("RISK", "风控复核"),
    FUND("FUND", "资金岗");

    private final String code;
    private final String desc;

    RoleEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() { return code; }
    public String getDesc() { return desc; }

    public static RoleEnum of(String code) {
        for (RoleEnum e : values()) {
            if (e.code.equalsIgnoreCase(code)) return e;
        }
        return null;
    }
}
