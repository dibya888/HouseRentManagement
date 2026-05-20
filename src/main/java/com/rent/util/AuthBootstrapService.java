package com.rent.util;

import com.rent.dao.UserAccountDAO;
import com.rent.model.UserAccount;

import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public final class AuthBootstrapService {

    private static final DateTimeFormatter TS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "1234";

    private AuthBootstrapService() {
    }

    public static void ensureDefaultAdminExists() {
        /*
         * Only create default admin if auth.db has no users.
         * This prevents duplicate admin creation.
         */
        if (UserAccountDAO.hasAnyUser()) {
            return;
        }

        try {
            String userId = UUID.randomUUID().toString();
            String now = LocalDateTime.now().format(TS);

            String passwordSalt = SecurityUtil.generateSalt();
            String passwordHash = SecurityUtil.hashSecret(
                    DEFAULT_ADMIN_PASSWORD,
                    passwordSalt
            );

            String dbKey = DbKeyCryptoUtil.generateDatabaseKey();
            String dbKeySalt = DbKeyCryptoUtil.generateSalt();
            String encryptedDbKey = DbKeyCryptoUtil.encryptDatabaseKey(
                    dbKey,
                    DEFAULT_ADMIN_PASSWORD,
                    dbKeySalt
            );

            Files.createDirectories(AppPaths.getUserDir(userId));

            UserAccount admin = new UserAccount();
            admin.setId(userId);
            admin.setUsername(DEFAULT_ADMIN_USERNAME);
            admin.setDisplayName("Admin");
            admin.setPasswordHash(passwordHash);
            admin.setPasswordSalt(passwordSalt);
            admin.setDbKeySalt(dbKeySalt);
            admin.setEncryptedDbKey(encryptedDbKey);
            admin.setRole("ADMIN");
            admin.setStatus("ACTIVE");
            admin.setDbFolder(userId);
            admin.setCreatedAt(now);
            admin.setUpdatedAt(null);
            admin.setLastLoginAt(null);

            boolean created = UserAccountDAO.insert(admin);

            if (!created) {
                throw new RuntimeException("Default admin could not be created in auth.db.");
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to bootstrap default admin.", e);
        }
    }
}