package com.rent.util;

import com.rent.dao.UserAccountDAO;
import com.rent.model.UserAccount;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class LegacyAdminMigrationService {

    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "1234";

    /*
     * Business-data tables only.
     * Do NOT migrate old users or old emergency_keys from legacy rent.db.
     */
    private static final String[] BUSINESS_TABLES = {
            "properties",
            "flats",
            "tenants",
            "bill_defaults",
            "rent_current",
            "rent_archive",
            "repairs",
            "audit_logs",
            "move_out_settlements"
    };

    private LegacyAdminMigrationService() {
    }

    public static MigrationResult migrateLegacyDataToAdminIfNeeded() {
        Path legacyDbPath = AppPaths.getLegacyDatabasePath();

        if (!Files.exists(legacyDbPath)) {
            return MigrationResult.NO_LEGACY_DB;
        }


        Optional<UserAccount> optionalAdmin =
                UserAccountDAO.findByUsername(DEFAULT_ADMIN_USERNAME);

        if (optionalAdmin.isEmpty()) {
            throw new RuntimeException("Cannot migrate legacy data: admin account not found in auth.db.");
        }

        UserAccount admin = optionalAdmin.get();

        if (!admin.isActive() || !admin.isAdmin()) {
            throw new RuntimeException("Cannot migrate legacy data: admin account is invalid.");
        }

        String dbKey = DbKeyCryptoUtil.decryptDatabaseKey(
                admin.getEncryptedDbKey(),
                DEFAULT_ADMIN_PASSWORD,
                admin.getDbKeySalt()
        );

        Path adminDbPath = AppPaths.getUserRentDbPath(admin.getId());

        try (Connection legacyConn = DriverManager.getConnection(
                "jdbc:sqlite:" + legacyDbPath.toAbsolutePath()
        );
             Connection adminConn = EncryptedDbConnectionFactory.open(adminDbPath, dbKey)) {

            RentDatabaseInitializer.initialize(adminConn);

            if (!legacyLooksLikeBusinessDb(legacyConn)) {
                return MigrationResult.NO_BUSINESS_DATA;
            }

            if (!adminBusinessDbLooksEmpty(adminConn)) {
                return MigrationResult.ALREADY_MIGRATED_OR_ADMIN_DB_NOT_EMPTY;
            }

            copyBusinessTables(legacyConn, adminConn);
            return MigrationResult.MIGRATED;

        } catch (Exception e) {
            throw new RuntimeException("Failed to migrate legacy database data to Admin encrypted database.", e);
        }
    }

    private static boolean legacyLooksLikeBusinessDb(Connection legacyConn) {
        return tableExists(legacyConn, "flats")
                || tableExists(legacyConn, "tenants")
                || tableExists(legacyConn, "rent_current")
                || tableExists(legacyConn, "rent_archive")
                || tableExists(legacyConn, "repairs")
                || tableExists(legacyConn, "properties");
    }

    private static boolean adminBusinessDbLooksEmpty(Connection adminConn) {
        String[] checkTables = {
                "properties",
                "flats",
                "tenants",
                "rent_current",
                "rent_archive",
                "repairs",
                "move_out_settlements"
        };

        for (String table : checkTables) {
            if (tableExists(adminConn, table) && countRows(adminConn, table) > 0) {
                return false;
            }
        }

        return true;
    }

    private static void copyBusinessTables(Connection legacyConn, Connection adminConn) throws Exception {
        adminConn.setAutoCommit(false);

        try (Statement stmt = adminConn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = OFF");
        }

        try {
            for (String table : BUSINESS_TABLES) {
                copyTableIfExists(legacyConn, adminConn, table);
            }

            try (Statement stmt = adminConn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }

            adminConn.commit();

        } catch (Exception e) {
            adminConn.rollback();
            throw e;
        } finally {
            adminConn.setAutoCommit(true);
        }
    }

    private static void copyTableIfExists(Connection sourceConn,
                                          Connection targetConn,
                                          String tableName) throws Exception {

        if (!tableExists(sourceConn, tableName) || !tableExists(targetConn, tableName)) {
            return;
        }

        List<String> sourceColumns = getColumns(sourceConn, tableName);
        List<String> targetColumns = getColumns(targetConn, tableName);

        List<String> commonColumns = new ArrayList<>();

        for (String sourceColumn : sourceColumns) {
            if (targetColumns.contains(sourceColumn)) {
                commonColumns.add(sourceColumn);
            }
        }

        if (commonColumns.isEmpty()) {
            return;
        }

        String selectSql = "SELECT " + joinColumns(commonColumns) + " FROM " + tableName;

        String insertSql =
                "INSERT OR IGNORE INTO " + tableName +
                        " (" + joinColumns(commonColumns) + ") VALUES (" +
                        placeholders(commonColumns.size()) +
                        ")";

        try (PreparedStatement selectPs = sourceConn.prepareStatement(selectSql);
             ResultSet rs = selectPs.executeQuery();
             PreparedStatement insertPs = targetConn.prepareStatement(insertSql)) {

            int columnCount = commonColumns.size();

            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    insertPs.setObject(i, rs.getObject(i));
                }
                insertPs.addBatch();
            }

            insertPs.executeBatch();
        }
    }

    private static boolean tableExists(Connection conn, String tableName) {
        String sql = """
            SELECT name
            FROM sqlite_master
            WHERE type = 'table'
              AND name = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableName);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (Exception e) {
            return false;
        }
    }

    private static int countRows(Connection conn, String tableName) {
        String sql = "SELECT COUNT(*) FROM " + tableName;

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;

        } catch (Exception e) {
            return 0;
        }
    }

    private static List<String> getColumns(Connection conn, String tableName) throws Exception {
        List<String> columns = new ArrayList<>();

        String sql = "PRAGMA table_info(" + tableName + ")";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                columns.add(rs.getString("name"));
            }
        }

        return columns;
    }

    private static String joinColumns(List<String> columns) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(columns.get(i));
        }

        return sb.toString();
    }

    private static String placeholders(int count) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < count; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("?");
        }

        return sb.toString();
    }
}