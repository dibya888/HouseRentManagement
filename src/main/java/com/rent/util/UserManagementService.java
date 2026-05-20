package com.rent.util;

import com.rent.dao.UserAccountDAO;
import com.rent.model.UserAccount;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;

public final class UserManagementService {

    private UserManagementService() {
    }

    public static boolean disableUser(String userId) {
        CurrentSession.requireAdmin();

        UserAccount user = getManagedUser(userId);

        ensureNotCurrentUser(user);
        ensureNotAdmin(user);

        return UserAccountDAO.updateStatus(user.getId(), "INACTIVE");
    }

    public static boolean enableUser(String userId) {
        CurrentSession.requireAdmin();

        UserAccount user = getManagedUser(userId);

        ensureNotCurrentUser(user);
        ensureNotAdmin(user);

        return UserAccountDAO.updateStatus(user.getId(), "ACTIVE");
    }

    public static boolean deleteUser(String userId) {
        CurrentSession.requireAdmin();

        UserAccount user = getManagedUser(userId);

        ensureNotCurrentUser(user);
        ensureNotAdmin(user);

        boolean deletedFromAuth = UserAccountDAO.deleteById(user.getId());

        if (!deletedFromAuth) {
            return false;
        }

        deleteUserFolder(user.getId());

        return true;
    }

    private static UserAccount getManagedUser(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("No user selected.");
        }

        Optional<UserAccount> optionalUser = UserAccountDAO.findById(userId);

        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("User was not found.");
        }

        return optionalUser.get();
    }

    private static void ensureNotCurrentUser(UserAccount user) {
        if (CurrentSession.getUserId().equals(user.getId())) {
            throw new IllegalArgumentException("You cannot manage your own account from here.");
        }
    }

    private static void ensureNotAdmin(UserAccount user) {
        if (user.isAdmin()) {
            throw new IllegalArgumentException("Admin account cannot be disabled or deleted.");
        }
    }

    private static void deleteUserFolder(String userId) {
        Path userDir = AppPaths.getUserDir(userId);
        Path usersRoot = AppPaths.getUsersDir();

        try {
            if (!Files.exists(userDir)) {
                return;
            }

            Path normalizedUserDir = userDir.toAbsolutePath().normalize();
            Path normalizedUsersRoot = usersRoot.toAbsolutePath().normalize();

            if (!normalizedUserDir.startsWith(normalizedUsersRoot)) {
                throw new SecurityException("Invalid user folder path.");
            }

            Files.walk(normalizedUserDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to delete: " + path, e);
                        }
                    });

        } catch (Exception e) {
            throw new RuntimeException("User account was deleted, but user database folder could not be removed.", e);
        }
    }
}