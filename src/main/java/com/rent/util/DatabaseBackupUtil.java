package com.rent.util;

import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import com.rent.dao.AuditLogDAO;
import com.rent.util.AuditActions;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DatabaseBackupUtil {

    private static final DateTimeFormatter FILE_TIME =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private static Path getDbPath() {
        return DBUtil.getDatabasePath();
    }

    public static void backupDatabase(Window ownerWindow) {
        try {
            Path dbPath = getDbPath();

            if (!Files.exists(dbPath)) {
                showError("Database file not found.");
                return;
            }

            FileChooser chooser = new FileChooser();
            chooser.setTitle("Save Database Backup");

            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("SQLite Database Backup", "*.db")
            );

            chooser.setInitialFileName(
                    "rent_backup_" + LocalDateTime.now().format(FILE_TIME) + ".db"
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
                    "Database backup created: " + destination.getAbsolutePath()
            );

            showInfo("Backup created successfully:\n" + destination.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to backup database.");
        }
    }

    public static boolean restoreDatabase(Window ownerWindow) {
        try {
            Path dbPath = getDbPath();

            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Database Backup");

            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("SQLite Database Backup", "*.db")
            );

            File selectedBackup = chooser.showOpenDialog(ownerWindow);

            if (selectedBackup == null) {
                return false;
            }

            if (!selectedBackup.exists()) {
                showError("Selected backup file does not exist.");
                return false;
            }

            Files.copy(
                    selectedBackup.toPath(),
                    dbPath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            AuditLogDAO.log(
                    AuditActions.DATABASE_RESTORE,
                    "Database restored from: " + selectedBackup.getAbsolutePath()
            );

            showInfo("""
                    Database restored successfully.
                    Please restart the app to load restored data.
                    """);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to restore database.");
            return false;
        }
    }

    private static void showInfo(String message) {
        new Alert(Alert.AlertType.INFORMATION, message).showAndWait();
    }

    private static void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message).showAndWait();
    }
}