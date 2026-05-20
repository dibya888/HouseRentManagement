package com.rent.util;

import java.nio.file.Path;
import java.sql.Connection;

public class DBUtil {

    private DBUtil() {
    }

    /*
     * Current logged-in user's encrypted rent database.
     * Used by backup/restore and other utilities after login.
     */
    public static Path getDatabasePath() {
        return CurrentSession.getCurrentUserDatabasePath();
    }

    public static Path getDatabaseDir() {
        return getDatabasePath().getParent();
    }

    public static String getJdbcUrl() {
        return "jdbc:sqlite:" + getDatabasePath().toAbsolutePath();
    }

    /*
     * Main business database connection.
     *
     * All existing business DAOs use DBUtil.connect().
     * After this step, those DAOs automatically point to the logged-in user's
     * encrypted rent.db.
     */
    public static Connection connect() {
        CurrentSession.requireLogin();

        return EncryptedDbConnectionFactory.open(
                CurrentSession.getCurrentUserDatabasePath(),
                CurrentSession.getDatabaseKey()
        );
    }

    /*
     * Initializes the current logged-in user's rent database schema.
     * Do not call this before login.
     */
    public static void init() {
        try (Connection conn = connect()) {
            RentDatabaseInitializer.initialize(conn);
            System.out.println("User rent database ready");
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize current user rent database.", e);
        }
    }

    /*
     * Legacy path kept only for older utilities/migration references.
     */
    public static Path getLegacyDatabasePath() {
        return AppPaths.getLegacyDatabasePath();
    }

    public static Path getLegacyDatabaseDir() {
        return AppPaths.getLegacyDatabaseDir();
    }
}