package com.rent.util;

public class AuditActions {

    public static final String LOGIN_SUCCESS = "LOGIN_SUCCESS";
    public static final String LOGIN_FAILED = "LOGIN_FAILED";
    public static final String LOGOUT = "LOGOUT";

    public static final String DATABASE_BACKUP = "DATABASE_BACKUP";
    public static final String DATABASE_RESTORE = "DATABASE_RESTORE";
    public static final String FACTORY_RESET = "FACTORY_RESET";

    public static final String PASSWORD_CHANGED = "PASSWORD_CHANGED";
    public static final String RECOVERY_PIN_SET = "RECOVERY_PIN_SET";
    public static final String EMERGENCY_KEYS_GENERATED = "EMERGENCY_KEYS_GENERATED";
    public static final String EMERGENCY_KEY_USED = "EMERGENCY_KEY_USED";

    public static final String RENT_PAYMENT = "RENT_PAYMENT";
    public static final String ARCHIVE_RESTORE = "ARCHIVE_RESTORE";
    public static final String ARCHIVE_PAYMENT_DELETED = "ARCHIVE_PAYMENT_DELETED";
    public static final String RECEIPT_PRINTED = "RECEIPT_PRINTED";

    public static final String REPAIR_ADDED = "REPAIR_ADDED";
    public static final String REPAIR_UPDATED = "REPAIR_UPDATED";
    public static final String REPAIR_DELETED = "REPAIR_DELETED";

    public static final String TENANT_ADDED = "TENANT_ADDED";
    public static final String TENANT_UPDATED = "TENANT_UPDATED";
    public static final String TENANT_MOVED_OUT = "TENANT_MOVED_OUT";
    public static final String TENANT_DELETED = "TENANT_DELETED";

    public static final String FLAT_ADDED = "FLAT_ADDED";
    public static final String FLAT_UPDATED = "FLAT_UPDATED";
    public static final String FLAT_DELETED = "FLAT_DELETED";

    public static final String PROPERTY_ADDED = "PROPERTY_ADDED";
    public static final String PROPERTY_UPDATED = "PROPERTY_UPDATED";
    public static final String PROPERTY_DELETED = "PROPERTY_DELETED";

    public static final String REPORT_PDF_EXPORTED = "REPORT_PDF_EXPORTED";
    public static final String REPORT_EXCEL_EXPORTED = "REPORT_EXCEL_EXPORTED";
    public static final String REPORT_PRINTED = "REPORT_PRINTED";

    public static final String MONTH_GENERATED = "MONTH_GENERATED";

    public static final String SETTLEMENT_CREATED = "SETTLEMENT_CREATED";

    public static final String SETTLEMENT_PDF_EXPORTED = "SETTLEMENT_PDF_EXPORTED";

    public static final String SETTLEMENT_SETTLED = "SETTLEMENT_SETTLED";

    private AuditActions() {
    }
}