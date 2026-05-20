package com.rent.util;

import com.rent.dao.UserAccountDAO;
import com.rent.model.UserAccount;

import java.util.Optional;

public final class AuthPasswordResetService {

    private AuthPasswordResetService() {
    }

    public static boolean resetPasswordByUsername(String username, String newPassword) {
        if (username == null || username.isBlank() || newPassword == null || newPassword.isBlank()) {
            return false;
        }

        Optional<UserAccount> opt = UserAccountDAO.findByUsername(username.trim());
        if (opt.isEmpty()) {
            return false;
        }

        UserAccount user = opt.get();

        if (!user.isActive()) {
            return false;
        }

        // Generate new password hash/salt
        String newSalt = SecurityUtil.generateSalt();
        String newHash = SecurityUtil.hashSecret(newPassword, newSalt);

        // Keep existing DB key, but re-wrap it using the new password
        String existingDbKey = DbKeyCryptoUtil.decryptDatabaseKey(
                user.getEncryptedDbKey(),
                /* old password is unknown here -> cannot decrypt */
                /* So: we must not change DB key unless we can recover it */
                "",
                user.getDbKeySalt()
        );

        // IMPORTANT:
        // We cannot decrypt the user's DB key without the old password.
        // Therefore we will NOT rotate password here.
        // (This is why Recovery PIN and Emergency Key reset must happen while we can derive DB key.)
        return false;
    }
}