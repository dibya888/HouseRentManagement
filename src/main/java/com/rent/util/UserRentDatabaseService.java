package com.rent.util;

import com.rent.dao.UserAccountDAO;
import com.rent.model.UserAccount;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.Optional;

public final class UserRentDatabaseService {

    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "1234";

    private UserRentDatabaseService() {
    }

    public static void ensureDefaultAdminRentDatabaseExists() {
        Optional<UserAccount> optionalAdmin =
                UserAccountDAO.findByUsername(DEFAULT_ADMIN_USERNAME);

        if (optionalAdmin.isEmpty()) {
            throw new RuntimeException("Default admin account was not found in auth.db.");
        }

        UserAccount admin = optionalAdmin.get();

        if (!admin.isActive()) {
            throw new RuntimeException("Default admin account is not active.");
        }

        if (!admin.isAdmin()) {
            throw new RuntimeException("Default admin account does not have ADMIN role.");
        }

        Path adminDbPath = AppPaths.getUserRentDbPath(admin.getId());

        if (Files.exists(adminDbPath)) {
            /*
             * DB already exists.
             * Open once to verify key/encryption and ensure latest schema.
             */
            String dbKey = DbKeyCryptoUtil.decryptDatabaseKey(
                    admin.getEncryptedDbKey(),
                    DEFAULT_ADMIN_PASSWORD,
                    admin.getDbKeySalt()
            );

            try (Connection conn = EncryptedDbConnectionFactory.open(adminDbPath, dbKey)) {
                RentDatabaseInitializer.initialize(conn);
            } catch (Exception e) {
                throw new RuntimeException("Admin encrypted rent database exists but could not be opened.", e);
            }

            return;
        }

        try {
            Files.createDirectories(AppPaths.getUserDir(admin.getId()));

            String dbKey = DbKeyCryptoUtil.decryptDatabaseKey(
                    admin.getEncryptedDbKey(),
                    DEFAULT_ADMIN_PASSWORD,
                    admin.getDbKeySalt()
            );

            try (Connection conn = EncryptedDbConnectionFactory.open(adminDbPath, dbKey)) {
                RentDatabaseInitializer.initialize(conn);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to create Admin encrypted rent database.", e);
        }
    }
}