package com.rent.util;

import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DatabaseBackupUtil {

    private static final Path DB_PATH =
            Path.of("src/main/resources/database/rent.db");

    private static final DateTimeFormatter FILE_TIME =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public static void backupDatabase(Window ownerWindow) {
        try {
            if (!Files.exists(DB_PATH)) {
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
                    DB_PATH,
                    destination.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );

            showInfo("Backup created successfully:\n" + destination.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to backup database.");
        }
    }

    public static void restoreDatabase(Window ownerWindow) {
        try {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Database Backup");

            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("SQLite Database Backup", "*.db")
            );

            File selectedBackup = chooser.showOpenDialog(ownerWindow);

            if (selectedBackup == null) {
                return;
            }

            if (!selectedBackup.exists()) {
                showError("Selected backup file does not exist.");
                return;
            }

            Files.copy(
                    selectedBackup.toPath(),
                    DB_PATH,
                    StandardCopyOption.REPLACE_EXISTING
            );

            showInfo("""
                    Database restored successfully.

                    Please restart the app to load restored data.
                    """);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to restore database.");
        }
    }

    private static void showInfo(String message) {
        new Alert(Alert.AlertType.INFORMATION, message).showAndWait();
    }

    private static void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message).showAndWait();
    }
}