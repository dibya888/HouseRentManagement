package com.rent.util;

import com.rent.dao.UserAccountDAO;
import com.rent.model.UserAccount;

import java.util.Optional;

public final class AuthService {

    private AuthService() {
    }

    public static boolean login(String username, String password) {
        if (username == null || username.isBlank()
                || password == null || password.isBlank()) {
            return false;
        }

        Optional<UserAccount> optionalUser =
                UserAccountDAO.findByUsername(username.trim());

        if (optionalUser.isEmpty()) {
            return false;
        }

        UserAccount user = optionalUser.get();

        if (!user.isActive()) {
            return false;
        }

        boolean passwordValid = SecurityUtil.verifySecret(
                password,
                user.getPasswordHash(),
                user.getPasswordSalt()
        );

        if (!passwordValid) {
            return false;
        }

        String databaseKey = DbKeyCryptoUtil.decryptDatabaseKey(
                user.getEncryptedDbKey(),
                password,
                user.getDbKeySalt()
        );

        /*
         * Open once immediately to verify this user's encrypted rent database.
         * If wrong/corrupt, login fails before dashboard opens.
         */
        try (var ignored = EncryptedDbConnectionFactory.open(
                AppPaths.getUserRentDbPath(user.getId()),
                databaseKey
        )) {
            // verified
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        CurrentSession.start(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getRole(),
                databaseKey
        );

        UserAccountDAO.updateLastLogin(user.getId());

        return true;
    }
}