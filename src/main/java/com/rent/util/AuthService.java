package com.rent.util;

import com.rent.dao.UserAccountDAO;
import com.rent.model.UserAccount;

import java.sql.Connection;
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

        String databaseKey;

        try {
            databaseKey = DbKeyCryptoUtil.decryptDatabaseKey(
                    user.getEncryptedDbKey(),
                    password,
                    user.getDbKeySalt()
            );
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        /*
         * Start session before schema init so DBUtil and DAOs can use it.
         */
        CurrentSession.start(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getRole(),
                databaseKey
        );

        /*
         * Open + initialize the user's encrypted rent database.
         * If DB file does not exist, it is created here.
         */
        try (Connection conn = EncryptedDbConnectionFactory.open(
                AppPaths.getUserRentDbPath(user.getId()),
                databaseKey
        )) {
            RentDatabaseInitializer.initialize(conn);
        } catch (Exception e) {
            e.printStackTrace();
            CurrentSession.clear();
            return false;
        }

        UserAccountDAO.updateLastLogin(user.getId());

        return true;
    }
}