package com.rent.util;

import com.rent.dao.AuditLogDAO;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class DatabaseBackupUtil {

    private static final DateTimeFormatter FILE_TIME =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private static Path getCurrentUserDbPath() {
        CurrentSession.requireLogin();
        return DBUtil.getDatabasePath();
    }

    public static void backupDatabase(Window ownerWindow) {
        try {
            CurrentSession.requireLogin();

            Path dbPath = getCurrentUserDbPath();

            if (!Files.exists(dbPath)) {
                showError("Current user's database file was not found.");
                return;
            }

            FileChooser chooser = new FileChooser();
            chooser.setTitle("Save Current User Database Backup");

            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Encrypted Rent Database Backup", "*.db")
            );

            String username = safeFileName(CurrentSession.getUsername());

            chooser.setInitialFileName(
                    "rent_backup_" +
                            username +
                            "_" +
                            LocalDateTime.now().format(FILE_TIME) +
                            ".db"
            );

            File destination = chooser.showSaveDialog(ownerWindow);

            if (destination == null) {
                return;
            }

            if (!destination.getName().toLowerCase().endsWith(".db")) {
                destination = new File(destination.getAbsolutePath() + ".db");
            }

            Files.copy(
                    dbPath,
                    destination.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );

            AuditLogDAO.log(
                    AuditActions.DATABASE_BACKUP,
                    "Current user encrypted database backup created: " + destination.getAbsolutePath()
            );

            showInfo("Backup created successfully:\n" + destination.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to backup current user's database.");
        }
    }

    public static boolean restoreDatabase(Window ownerWindow) {
        try {
            CurrentSession.requireLogin();

            Path currentDbPath = getCurrentUserDbPath();

            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Current User Database Backup");

            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Encrypted Rent Database Backup", "*.db")
            );

            File selectedBackup = chooser.showOpenDialog(ownerWindow);

            if (selectedBackup == null) {
                return false;
            }

            if (!selectedBackup.exists()) {
                showError("Selected backup file does not exist.");
                return false;
            }

            /*
             * CRITICAL:
             * Validate selected backup BEFORE replacing current DB.
             * This prevents restoring Admin backup into another user's account.
             */
            if (!canOpenBackupWithCurrentUserKey(selectedBackup.toPath())) {
                showError("""
                        This backup cannot be restored for the current user.

                        Possible reasons:
                        • The backup belongs to another user
                        • The backup was encrypted with a different database key
                        • The file is not a valid encrypted rent database

                        Restore cancelled. No data was changed.
                        """);
                return false;
            }

            Optional<ButtonType> confirm = new Alert(
                    Alert.AlertType.CONFIRMATION,
                    """
                    This will replace the current logged-in user's database.

                    Current User: %s

                    Other users will not be affected.

                    Continue?
                    """.formatted(CurrentSession.getUsername()),
                    ButtonType.YES,
                    ButtonType.NO
            ).showAndWait();

            if (confirm.isEmpty() || confirm.get() != ButtonType.YES) {
                return false;
            }

            /*
             * Safety backup of current DB before overwrite.
             */
            Path safetyBackup = currentDbPath.resolveSibling(
                    "rent_before_restore_" +
                            LocalDateTime.now().format(FILE_TIME) +
                            ".db"
            );

            if (Files.exists(currentDbPath)) {
                Files.copy(
                        currentDbPath,
                        safetyBackup,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

            Files.copy(
                    selectedBackup.toPath(),
                    currentDbPath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            /*
             * Validate restored DB once more after copy.
             */
            if (!canOpenBackupWithCurrentUserKey(currentDbPath)) {
                if (Files.exists(safetyBackup)) {
                    Files.copy(
                            safetyBackup,
                            currentDbPath,
                            StandardCopyOption.REPLACE_EXISTING
                    );
                }

                showError("""
                        Restore failed validation after copy.

                        Original database was restored from safety backup.
                        """);
                return false;
            }

            AuditLogDAO.log(
                    AuditActions.DATABASE_RESTORE,
                    "Current user encrypted database restored from: " + selectedBackup.getAbsolutePath()
            );

            showInfo("""
                    Database restored successfully.

                    Please restart the app to load restored data.
                    """);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to restore current user's database.");
            return false;
        }
    }

    private static boolean canOpenBackupWithCurrentUserKey(Path backupPath) {
        try (var ignored = EncryptedDbConnectionFactory.open(
                backupPath,
                CurrentSession.getDatabaseKey()
        )) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static String safeFileName(String value) {
        if (value == null || value.isBlank()) {
            return "user";
        }

        return value.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private static void showInfo(String message) {
        new Alert(Alert.AlertType.INFORMATION, message).showAndWait();
    }

    private static void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message).showAndWait();
    }
}