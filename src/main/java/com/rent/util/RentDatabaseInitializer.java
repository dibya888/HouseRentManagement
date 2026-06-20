package com.rent.util;

import java.sql.Connection;
import java.sql.Statement;

public final class RentDatabaseInitializer {

    private RentDatabaseInitializer() {
    }

    public static void initialize(Connection conn) {
        try (Statement stmt = conn.createStatement()) {

            stmt.execute("PRAGMA foreign_keys = ON");

            // TENANTS TABLE
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS tenants (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    phone TEXT NOT NULL,
                    email TEXT,
                    nid TEXT,
                    address TEXT,
                    flat_no TEXT,
                    rent REAL,
                    nid_path TEXT,
                    doc_path TEXT
                )
            """);

            addColumnIfMissing(stmt, "tenants", "status TEXT DEFAULT 'Active'");
            addColumnIfMissing(stmt, "tenants", "move_in_date TEXT");
            addColumnIfMissing(stmt, "tenants", "move_out_date TEXT");
            addColumnIfMissing(stmt, "tenants", "move_out_reason TEXT");
            addColumnIfMissing(stmt, "tenants", "security_deposit REAL DEFAULT 0");
            addColumnIfMissing(stmt, "tenants", "security_deposit_date TEXT");
            addColumnIfMissing(stmt, "tenants", "security_deposit_note TEXT");

            stmt.execute("""
                UPDATE tenants
                SET status = 'Active'
                WHERE status IS NULL OR status = ''
            """);

            stmt.execute("""
                UPDATE tenants
                SET security_deposit = 0
                WHERE security_deposit IS NULL
            """);

            // FLATS TABLE
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS flats (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    flat_no TEXT NOT NULL UNIQUE,
                    meter_no TEXT,
                    bedrooms INTEGER,
                    bathrooms INTEGER,
                    kitchens INTEGER,
                    balconies INTEGER,
                    dining_rooms INTEGER,
                    living_rooms INTEGER,
                    rent REAL,
                    status TEXT
                )
            """);

            addColumnIfMissing(stmt, "flats", "property_id INTEGER");

            // BILL DEFAULTS
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS bill_defaults (
                    id INTEGER PRIMARY KEY CHECK (id = 1),
                    electricity REAL DEFAULT 0,
                    gas REAL DEFAULT 0,
                    water REAL DEFAULT 0
                )
            """);

            addColumnIfMissing(stmt, "bill_defaults", "due_day INTEGER DEFAULT 5");

            stmt.execute("""
                INSERT OR IGNORE INTO bill_defaults (id, electricity, gas, water, due_day)
                VALUES (1, 0, 0, 0, 5)
            """);

            stmt.execute("""
                UPDATE bill_defaults
                SET due_day = 5
                WHERE due_day IS NULL
            """);

            // CURRENT RENT
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS rent_current (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    tenant_id INTEGER NOT NULL,
                    flat_no TEXT NOT NULL,
                    bill_month TEXT NOT NULL,
                    house_rent REAL NOT NULL DEFAULT 0,
                    electricity REAL NOT NULL DEFAULT 0,
                    water REAL NOT NULL DEFAULT 0,
                    gas REAL NOT NULL DEFAULT 0,
                    other_bills REAL NOT NULL DEFAULT 0,
                    fine REAL NOT NULL DEFAULT 0,
                    discount REAL NOT NULL DEFAULT 0,
                    total REAL NOT NULL DEFAULT 0,
                    paid_amount REAL NOT NULL DEFAULT 0,
                    payment_date TEXT,
                    due_date TEXT,
                    status TEXT NOT NULL DEFAULT 'DUE',
                    notes TEXT,
                    UNIQUE(tenant_id, bill_month),
                    FOREIGN KEY(tenant_id) REFERENCES tenants(id),
                    FOREIGN KEY(flat_no) REFERENCES flats(flat_no)
                )
            """);

            // RENT ARCHIVE
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS rent_archive (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    original_id INTEGER,
                    tenant_id INTEGER NOT NULL,
                    flat_no TEXT NOT NULL,
                    bill_month TEXT NOT NULL,
                    house_rent REAL NOT NULL DEFAULT 0,
                    electricity REAL NOT NULL DEFAULT 0,
                    water REAL NOT NULL DEFAULT 0,
                    gas REAL NOT NULL DEFAULT 0,
                    other_bills REAL NOT NULL DEFAULT 0,
                    fine REAL NOT NULL DEFAULT 0,
                    discount REAL NOT NULL DEFAULT 0,
                    total REAL NOT NULL DEFAULT 0,
                    paid_amount REAL NOT NULL DEFAULT 0,
                    payment_date TEXT,
                    due_date TEXT,
                    status TEXT NOT NULL DEFAULT 'PAID',
                    notes TEXT,
                    archived_at TEXT NOT NULL,
                    FOREIGN KEY(tenant_id) REFERENCES tenants(id),
                    FOREIGN KEY(flat_no) REFERENCES flats(flat_no)
                )
            """);

            addColumnIfMissing(stmt, "rent_archive", "receipt_no TEXT");

            // PROPERTIES
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS properties (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    address TEXT NOT NULL,
                    phone TEXT,
                    logo_path TEXT,
                    is_default INTEGER DEFAULT 0
                )
            """);

            // REPAIRS
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS repairs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    flat_no TEXT,
                    repair_date TEXT NOT NULL,
                    category TEXT NOT NULL,
                    description TEXT,
                    cost REAL NOT NULL DEFAULT 0,
                    paid_by TEXT DEFAULT 'Owner',
                    status TEXT DEFAULT 'Completed',
                    notes TEXT,
                    created_at TEXT,
                    FOREIGN KEY(flat_no) REFERENCES flats(flat_no)
                )
            """);

            addColumnIfMissing(stmt, "repairs", "vendor_name TEXT");
            addColumnIfMissing(stmt, "repairs", "vendor_phone TEXT");
            addColumnIfMissing(stmt, "repairs", "invoice_no TEXT");

            // AUDIT LOGS
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS audit_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT,
                    action TEXT NOT NULL,
                    details TEXT,
                    created_at TEXT NOT NULL
                )
            """);

            // MOVE OUT SETTLEMENTS
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS move_out_settlements (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    tenant_id INTEGER,
                    tenant_name TEXT,
                    tenant_phone TEXT,
                    flat_no TEXT,
                    move_out_date TEXT,
                    unpaid_due REAL DEFAULT 0,
                    security_deposit REAL DEFAULT 0,
                    refund_amount REAL DEFAULT 0,
                    payable_amount REAL DEFAULT 0,
                    result TEXT,
                    reason TEXT,
                    created_at TEXT
                )
            """);

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize rent database.", e);
        }
    }

    private static void addColumnIfMissing(Statement stmt, String tableName, String columnDefinition) {
        try {
            stmt.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnDefinition);
        } catch (Exception ignored) {
            // Column already exists.
        }
    }
}