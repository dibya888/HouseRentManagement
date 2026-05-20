package com.rent.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Window;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;

public class DatabaseResetUtil {

    private static final String RESET_CONFIRM_TEXT = "RESET";

    private DatabaseResetUtil() {
    }

    public static boolean confirmFactoryReset(Window ownerWindow) {
        Alert warning = new Alert(
                Alert.AlertType.WARNING,
                """
                Factory Reset will permanently delete:

                • All users
                • All encrypted user databases
                • Auth database
                • Recovery keys
                • Old legacy database, if present

                This cannot be undone.

                You should create backups before continuing.
                """,
                ButtonType.OK,
                ButtonType.CANCEL
        );

        warning.setTitle("Factory Reset");
        warning.setHeaderText("Permanent Reset Warning");

        Optional<ButtonType> warningResult = warning.showAndWait();

        if (warningResult.isEmpty() || warningResult.get() != ButtonType.OK) {
            return false;
        }

        TextInputDialog confirmDialog = new TextInputDialog();
        confirmDialog.setTitle("Confirm Factory Reset");
        confirmDialog.setHeaderText("Type RESET to confirm.");
        confirmDialog.setContentText("Confirmation:");

        Optional<String> typed = confirmDialog.showAndWait();

        return typed.isPresent()
                && RESET_CONFIRM_TEXT.equals(typed.get().trim());
    }

    public static boolean factoryReset() {
        try {
            CurrentSession.clear();

            deleteIfExists(AppPaths.getAuthDir());
            deleteIfExists(AppPaths.getUsersDir());
            deleteIfExists(AppPaths.getLegacyDatabaseDir());

            /*
             * Recreate fresh secure system.
             */
            AuthDBUtil.init();
            AuthBootstrapService.ensureDefaultAdminExists();
            UserRentDatabaseService.ensureDefaultAdminRentDatabaseExists();

            return true;

        } catch (Exception e) {
            e.printStackTrace();

            new Alert(
                    Alert.AlertType.ERROR,
                    "Factory reset failed. Please restart the app and try again."
            ).showAndWait();

            return false;
        }
    }

    private static void deleteIfExists(Path path) throws Exception {
        if (path == null || !Files.exists(path)) {
            return;
        }

        Path normalized = path.toAbsolutePath().normalize();
        Path appRoot = AppPaths.getAppDataDir().toAbsolutePath().normalize();

        /*
         * Safety guard:
         * never delete outside our app data folder.
         */
        if (!normalized.startsWith(appRoot)) {
            throw new SecurityException("Refusing to delete path outside app data folder: " + normalized);
        }

        Files.walk(normalized)
                .sorted(Comparator.reverseOrder())
                .forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to delete: " + p, e);
                    }
                });
    }
}