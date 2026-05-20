package com.rent.util;

import com.rent.dao.UserAccountDAO;
import com.rent.model.UserAccount;

import java.nio.file.Files;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public final class UserCreationService {

    private static final DateTimeFormatter TS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private UserCreationService() {
    }

    public static boolean createUser(String username,
                                     String displayName,
                                     String temporaryPassword) {

        CurrentSession.requireAdmin();

        String cleanUsername = username == null ? "" : username.trim();
        String cleanDisplayName = displayName == null ? "" : displayName.trim();
        String cleanPassword = temporaryPassword == null ? "" : temporaryPassword;

        if (cleanUsername.isBlank()
                || cleanPassword.isBlank()) {
            throw new IllegalArgumentException("Username and password are required.");
        }

        if (cleanUsername.length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters.");
        }

        if (cleanPassword.length() < 4) {
            throw new IllegalArgumentException("Password must be at least 4 characters.");
        }

        if (UserAccountDAO.usernameExists(cleanUsername)) {
            throw new IllegalArgumentException("Username already exists.");
        }

        String userId = UUID.randomUUID().toString();
        String now = LocalDateTime.now().format(TS);

        String passwordSalt = SecurityUtil.generateSalt();
        String passwordHash = SecurityUtil.hashSecret(cleanPassword, passwordSalt);

        String dbKey = DbKeyCryptoUtil.generateDatabaseKey();
        String dbKeySalt = DbKeyCryptoUtil.generateSalt();
        String encryptedDbKey = DbKeyCryptoUtil.encryptDatabaseKey(
                dbKey,
                cleanPassword,
                dbKeySalt
        );

        try {
            Files.createDirectories(AppPaths.getUserDir(userId));

            try (Connection conn = EncryptedDbConnectionFactory.open(
                    AppPaths.getUserRentDbPath(userId),
                    dbKey
            )) {
                RentDatabaseInitializer.initialize(conn);
            }

            UserAccount user = new UserAccount();
            user.setId(userId);
            user.setUsername(cleanUsername);
            user.setDisplayName(cleanDisplayName.isBlank() ? cleanUsername : cleanDisplayName);
            user.setPasswordHash(passwordHash);
            user.setPasswordSalt(passwordSalt);
            user.setDbKeySalt(dbKeySalt);
            user.setEncryptedDbKey(encryptedDbKey);
            user.setRole("USER");
            user.setStatus("ACTIVE");
            user.setDbFolder(userId);
            user.setCreatedAt(now);
            user.setUpdatedAt(null);
            user.setLastLoginAt(null);

            boolean inserted = UserAccountDAO.insert(user);

            if (!inserted) {
                throw new RuntimeException("User account could not be saved.");
            }

            return true;

        } catch (Exception e) {
            throw new RuntimeException("Failed to create user.", e);
        }
    }
}
