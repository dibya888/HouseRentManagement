package com.rent.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DBUtil {

    private static final String APP_FOLDER_NAME = "HouseRentManagement";
    private static final String DB_FILE_NAME = "rent.db";

    public static Path getAppDataDir() {
        String appData = System.getenv("APPDATA");

        if (appData != null && !appData.isBlank()) {
            return Paths.get(appData, APP_FOLDER_NAME);
        }

        return Paths.get(
                System.getProperty("user.home"),
                "." + APP_FOLDER_NAME
        );
    }

    public static Path getDatabaseDir() {
        return getAppDataDir().resolve("database");
    }

    public static Path getDatabasePath() {
        return getDatabaseDir().resolve(DB_FILE_NAME);
    }

    public static String getJdbcUrl() {
        return "jdbc:sqlite:" + getDatabasePath().toAbsolutePath();
    }

    private static void ensureDatabaseDirExists() throws Exception {
        Files.createDirectories(getDatabaseDir());
    }

    public static Connection connect() {
        try {
            ensureDatabaseDirExists();

            Connection conn = DriverManager.getConnection(getJdbcUrl());

            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }

            return conn;

        } catch (Exception e) {
            System.out.println("DB Connection Failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void init() {

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            // USERS TABLE
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE,
                    password TEXT
                )
            """);

            // PASSWORD HASH COLUMNS
            try {
                stmt.execute("ALTER TABLE users ADD COLUMN password_hash TEXT");
            } catch (Exception ignored) {
                // column already exists
            }

            try {
                stmt.execute("ALTER TABLE users ADD COLUMN password_salt TEXT");
            } catch (Exception ignored) {
                // column already exists
            }

            //Add recovery PIN columns safely
            try {
                stmt.execute("ALTER TABLE users ADD COLUMN recovery_pin_hash TEXT");
            } catch (Exception ignored) {
                // column already exists
            }

            try {
                stmt.execute("ALTER TABLE users ADD COLUMN recovery_pin_salt TEXT");
            } catch (Exception ignored) {
                // column already exists
            }

            // Default admin user
            stmt.execute("""
                INSERT OR IGNORE INTO users(username, password)
                VALUES ('admin', '1234');
            """);

            // EMERGENCY RECOVERY KEYS
            String emergencyKeysSql = """
                CREATE TABLE IF NOT EXISTS emergency_keys (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    key_hash TEXT NOT NULL,
                    key_salt TEXT NOT NULL,
                    used INTEGER NOT NULL DEFAULT 0,
                    created_at TEXT,
                    used_at TEXT
                )
                """;
            stmt.execute(emergencyKeysSql);

            // TENANTS TABLE
            String tenantsSql = """
                CREATE TABLE IF NOT EXISTS tenants (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    phone TEXT NOT NULL,
                    email TEXT,
                    nid TEXT,
                    address TEXT,
                    flat_no TEXT,       -- changed from flatNo to flat_no
                    rent REAL,
                    nid_path TEXT,
                    doc_path TEXT
                )
                """;

            stmt.execute(tenantsSql);

            try {
                stmt.execute("ALTER TABLE tenants ADD COLUMN status TEXT DEFAULT 'Active'");
            } catch (Exception ignored) {
                // column already exists
            }

            try {
                stmt.execute("ALTER TABLE tenants ADD COLUMN move_in_date TEXT");
            } catch (Exception ignored) {
                // column already exists
            }

            try {
                stmt.execute("ALTER TABLE tenants ADD COLUMN move_out_date TEXT");
            } catch (Exception ignored) {
                // column already exists
            }

            try {
                stmt.execute("ALTER TABLE tenants ADD COLUMN move_out_reason TEXT");
            } catch (Exception ignored) {
                // column already exists
            }

            stmt.execute("""
                UPDATE tenants
                SET status = 'Active'
                WHERE status IS NULL OR status = ''
                """);

            try {
                stmt.execute("ALTER TABLE tenants ADD COLUMN security_deposit REAL DEFAULT 0");
            } catch (Exception ignored) {
                // column already exists
            }

            try {
                stmt.execute("ALTER TABLE tenants ADD COLUMN security_deposit_date TEXT");
            } catch (Exception ignored) {
                // column already exists
            }

            try {
                stmt.execute("ALTER TABLE tenants ADD COLUMN security_deposit_note TEXT");
            } catch (Exception ignored) {
                // column already exists
            }

            stmt.execute("""
                UPDATE tenants
                SET security_deposit = 0
                WHERE security_deposit IS NULL
                """);

            // FLATS TABLE
            String flatsSql = """
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
                """;

            stmt.execute(flatsSql);

            // BILL DEFAULTS (GLOBAL – applies to all future billing)
            String billDefaultsSql = """
                CREATE TABLE IF NOT EXISTS bill_defaults (
                    id INTEGER PRIMARY KEY CHECK (id = 1),
                    electricity REAL DEFAULT 0,
                    gas REAL DEFAULT 0,
                    water REAL DEFAULT 0
                )
                """;
            stmt.execute(billDefaultsSql);

            // Ensure single default row always exists
            stmt.execute("""
            INSERT OR IGNORE INTO bill_defaults (id, electricity, gas, water)
            VALUES (1, 0, 0, 0)
            """);
            // CURRENT DUE RENT (only DUE/PARTIAL/LATE rows live here)
            String rentCurrentSql = """
                CREATE TABLE IF NOT EXISTS rent_current (
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                
                  tenant_id INTEGER NOT NULL,
                  flat_no TEXT NOT NULL,
                  bill_month TEXT NOT NULL,          -- YYYY-MM
                
                  house_rent REAL NOT NULL DEFAULT 0,
                  electricity REAL NOT NULL DEFAULT 0,
                  water REAL NOT NULL DEFAULT 0,
                  gas REAL NOT NULL DEFAULT 0,
                  other_bills REAL NOT NULL DEFAULT 0,
                  fine REAL NOT NULL DEFAULT 0,
                  discount REAL NOT NULL DEFAULT 0,
                
                  total REAL NOT NULL DEFAULT 0,
                
                  paid_amount REAL NOT NULL DEFAULT 0,
                  payment_date TEXT,                -- YYYY-MM-DD
                  due_date TEXT,                    -- YYYY-MM-DD
                
                  status TEXT NOT NULL DEFAULT 'DUE', -- DUE/PARTIAL/LATE
                  notes TEXT,
                
                  UNIQUE(tenant_id, bill_month),
                  FOREIGN KEY(tenant_id) REFERENCES tenants(id),
                  FOREIGN KEY(flat_no) REFERENCES flats(flat_no)
                )
                """;
            stmt.execute(rentCurrentSql);

            // ARCHIVE (paid/cleared rows moved here)
            String rentArchiveSql = """
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
                
                  archived_at TEXT NOT NULL,         -- YYYY-MM-DD HH:MM:SS
                  FOREIGN KEY(tenant_id) REFERENCES tenants(id),
                  FOREIGN KEY(flat_no) REFERENCES flats(flat_no)
                )
                """;
            stmt.execute(rentArchiveSql);

            try {
                stmt.execute("ALTER TABLE rent_archive ADD COLUMN receipt_no TEXT");
            } catch (Exception ignored) {
                // column already exists
            }

            String propertiesSql = """
                CREATE TABLE IF NOT EXISTS properties (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    address TEXT NOT NULL,
                    phone TEXT,
                    logo_path TEXT,
                    is_default INTEGER DEFAULT 0
                )
                """;
            stmt.execute(propertiesSql);

            try {
                stmt.execute("ALTER TABLE flats ADD COLUMN property_id INTEGER");
            } catch (Exception ignored) {
                // column already exists → safe to ignore
            }

            // REPAIRS / MAINTENANCE TABLE
            String repairsSql = """
                CREATE TABLE IF NOT EXISTS repairs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    flat_no TEXT,
                    repair_date TEXT NOT NULL,          -- YYYY-MM-DD
                    category TEXT NOT NULL,             -- Plumbing/Electricity/Paint/Other
                    description TEXT,
                    cost REAL NOT NULL DEFAULT 0,
                    paid_by TEXT DEFAULT 'Owner',        -- Owner/Tenant
                    status TEXT DEFAULT 'Completed',     -- Pending/Completed
                    notes TEXT,
                    created_at TEXT,
                    FOREIGN KEY(flat_no) REFERENCES flats(flat_no)
                )
                """;
            stmt.execute(repairsSql);

            try {
                stmt.execute("ALTER TABLE repairs ADD COLUMN vendor_name TEXT");
            } catch (Exception ignored) {
                // column already exists
            }

            try {
                stmt.execute("ALTER TABLE repairs ADD COLUMN vendor_phone TEXT");
            } catch (Exception ignored) {
                // column already exists
            }

            try {
                stmt.execute("ALTER TABLE repairs ADD COLUMN invoice_no TEXT");
            } catch (Exception ignored) {
                // column already exists
            }

            // AUDIT LOGS TABLE
            String auditLogsSql = """
                CREATE TABLE IF NOT EXISTS audit_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT,
                    action TEXT NOT NULL,
                    details TEXT,
                    created_at TEXT NOT NULL
                )
                """;
            stmt.execute(auditLogsSql);

            // MOVE OUT SETTLEMENTS TABLE
            String moveOutSettlementsSql = """
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
                """;
            stmt.execute(moveOutSettlementsSql);

            System.out.println("Database Ready");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Optional: Test main
    public static void main(String[] args) {
        init();
    }
}