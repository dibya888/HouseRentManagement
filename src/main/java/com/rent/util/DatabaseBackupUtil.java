package com.rent.util;

import com.rent.dao.AuditLogDAO;
import com.rent.dao.UserAccountDAO;
import com.rent.model.UserAccount;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class DatabaseBackupUtil {

    private static final DateTimeFormatter FILE_TIME =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private static final String FORMAT_VERSION = "2";
    private static final String MANIFEST_ENTRY = "manifest.properties";
    private static final String DATABASE_ENTRY = "rent.db";

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

            Optional<String> backupPasswordOptional =
                    PortableBackupPasswordDialog.askBackupPasswordForCreate();

            if (backupPasswordOptional.isEmpty()) {
                return;
            }

            String backupPassword = backupPasswordOptional.get();

            String backupKeySalt = DbKeyCryptoUtil.generateSalt();
            String encryptedDbKeyForBackup = DbKeyCryptoUtil.encryptDatabaseKey(
                    CurrentSession.getDatabaseKey(),
                    backupPassword,
                    backupKeySalt
            );

            FileChooser chooser = new FileChooser();
            chooser.setTitle("Save Portable Backup");

            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("House Rent Backup", "*.hrmbak")
            );

            String username = safeFileName(CurrentSession.getUsername());

            chooser.setInitialFileName(
                    "rent_backup_" +
                            username +
                            "_" +
                            LocalDateTime.now().format(FILE_TIME) +
                            ".hrmbak"
            );

            File destination = chooser.showSaveDialog(ownerWindow);

            if (destination == null) {
                return;
            }

            if (!destination.getName().toLowerCase().endsWith(".hrmbak")) {
                destination = new File(destination.getAbsolutePath() + ".hrmbak");
            }

            Properties manifest = new Properties();
            manifest.setProperty("formatVersion", FORMAT_VERSION);
            manifest.setProperty("backupId", UUID.randomUUID().toString());
            manifest.setProperty("createdAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            manifest.setProperty("username", CurrentSession.getUsername());
            manifest.setProperty("displayName", CurrentSession.getDisplayName() == null ? "" : CurrentSession.getDisplayName());
            manifest.setProperty("dbKeySalt", backupKeySalt);
            manifest.setProperty("encryptedDbKey", encryptedDbKeyForBackup);

            createBackupPackage(dbPath, destination.toPath(), manifest);

            AuditLogDAO.log(
                    AuditActions.DATABASE_BACKUP,
                    "Portable encrypted database backup created: " + destination.getAbsolutePath()
            );

            showInfo("""
                    Portable backup created successfully.

                    This .hrmbak backup can be restored even after Factory Reset,
                    if you remember the backup password.
                    """);

        } catch (Exception e) {
            e.printStackTrace();
            showError(rootMessage(e, "Failed to create portable backup."));
        }
    }

    public static boolean restoreDatabase(Window ownerWindow) {
        Path tempDir = null;

        try {
            CurrentSession.requireLogin();

            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Portable Backup");

            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("House Rent Backup", "*.hrmbak")
            );

            File selectedBackup = chooser.showOpenDialog(ownerWindow);

            if (selectedBackup == null) {
                return false;
            }

            if (!selectedBackup.exists()) {
                showError("Selected backup file does not exist.");
                return false;
            }

            tempDir = Files.createTempDirectory("hrm_restore_");

            ExtractedBackup extracted = extractBackupPackage(
                    selectedBackup.toPath(),
                    tempDir
            );

            if (!isBackupForCurrentUser(extracted)) {
                showError("""
            This backup belongs to another user.

            Backup User: %s
            Current User: %s

            Restore cancelled. No data was changed.
            """.formatted(extracted.username, CurrentSession.getUsername()));

                return false;
            }


            Optional<String> backupPasswordOptional =
                    PortableBackupPasswordDialog.askBackupPasswordForRestore();

            if (backupPasswordOptional.isEmpty()) {
                return false;
            }

            String backupPassword = backupPasswordOptional.get();

            String restoredDbKey;

            try {
                restoredDbKey = DbKeyCryptoUtil.decryptDatabaseKey(
                        extracted.encryptedDbKey,
                        backupPassword,
                        extracted.dbKeySalt
                );
            } catch (Exception e) {
                showError("Incorrect backup password or damaged backup metadata.");
                return false;
            }

            if (!canOpenWithKey(extracted.databasePath, restoredDbKey)) {
                showError("""
                        Backup database could not be opened.

                        Possible reasons:
                        • Wrong backup password
                        • Damaged backup file
                        • Invalid backup format
                        """);
                return false;
            }

            Optional<String> currentPasswordOptional =
                    PortableBackupPasswordDialog.askCurrentAccountPassword();

            if (currentPasswordOptional.isEmpty()) {
                return false;
            }

            String currentPassword = currentPasswordOptional.get();

            Optional<UserAccount> optionalCurrentUser =
                    UserAccountDAO.findById(CurrentSession.getUserId());

            if (optionalCurrentUser.isEmpty()) {
                showError("Current user account was not found.");
                return false;
            }

            UserAccount currentUser = optionalCurrentUser.get();

            boolean currentPasswordValid = SecurityUtil.verifySecret(
                    currentPassword,
                    currentUser.getPasswordHash(),
                    currentUser.getPasswordSalt()
            );

            if (!currentPasswordValid) {
                showError("Current account password is incorrect.");
                return false;
            }

            Optional<ButtonType> confirm = new Alert(
                    Alert.AlertType.CONFIRMATION,
                    """
                    This will replace the current logged-in user's database.

                    Current User: %s
                    Backup User: %s

                    Continue?
                    """.formatted(CurrentSession.getUsername(), extracted.username),
                    ButtonType.YES,
                    ButtonType.NO
            ).showAndWait();

            if (confirm.isEmpty() || confirm.get() != ButtonType.YES) {
                return false;
            }

            Path currentDbPath = getCurrentUserDbPath();

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
                    extracted.databasePath,
                    currentDbPath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            if (!canOpenWithKey(currentDbPath, restoredDbKey)) {
                if (Files.exists(safetyBackup)) {
                    Files.copy(
                            safetyBackup,
                            currentDbPath,
                            StandardCopyOption.REPLACE_EXISTING
                    );
                }

                showError("Restore failed validation. Original database was restored.");
                return false;
            }

            String newDbKeySalt = DbKeyCryptoUtil.generateSalt();
            String newEncryptedDbKey = DbKeyCryptoUtil.encryptDatabaseKey(
                    restoredDbKey,
                    currentPassword,
                    newDbKeySalt
            );

            boolean updated = UserAccountDAO.updateDatabaseKeyWrapper(
                    CurrentSession.getUserId(),
                    newDbKeySalt,
                    newEncryptedDbKey
            );

            if (!updated) {
                if (Files.exists(safetyBackup)) {
                    Files.copy(
                            safetyBackup,
                            currentDbPath,
                            StandardCopyOption.REPLACE_EXISTING
                    );
                }

                showError("Restore failed while updating user security metadata. Original database was restored.");
                return false;
            }

            CurrentSession.replaceDatabaseKey(restoredDbKey);

            AuditLogDAO.log(
                    AuditActions.DATABASE_RESTORE,
                    "Portable encrypted database restored from: " + selectedBackup.getAbsolutePath()
            );

            showInfo("""
                    Database restored successfully.

                    Please restart the app to reload all data safely.
                    """);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            showError(rootMessage(e, "Failed to restore portable backup."));
            return false;

        } finally {
            if (tempDir != null) {
                try {
                    deleteDirectory(tempDir);
                } catch (Exception ignored) {
                }
            }
        }
    }

    private static void createBackupPackage(Path dbPath,
                                            Path backupPath,
                                            Properties manifest) throws Exception {

        try (OutputStream out = Files.newOutputStream(backupPath);
             ZipOutputStream zip = new ZipOutputStream(out)) {

            ZipEntry manifestEntry = new ZipEntry(MANIFEST_ENTRY);
            zip.putNextEntry(manifestEntry);
            manifest.store(zip, "House Rent Management Portable Backup");
            zip.closeEntry();

            ZipEntry dbEntry = new ZipEntry(DATABASE_ENTRY);
            zip.putNextEntry(dbEntry);
            Files.copy(dbPath, zip);
            zip.closeEntry();
        }
    }

    private static ExtractedBackup extractBackupPackage(Path backupPath,
                                                        Path tempDir) throws Exception {

        Properties manifest = new Properties();
        Path extractedDb = tempDir.resolve(DATABASE_ENTRY);

        boolean manifestFound = false;
        boolean dbFound = false;

        try (InputStream in = Files.newInputStream(backupPath);
             ZipInputStream zip = new ZipInputStream(in)) {

            ZipEntry entry;

            while ((entry = zip.getNextEntry()) != null) {
                String name = entry.getName();

                if (MANIFEST_ENTRY.equals(name)) {
                    manifest.load(zip);
                    manifestFound = true;
                } else if (DATABASE_ENTRY.equals(name)) {
                    Files.copy(
                            zip,
                            extractedDb,
                            StandardCopyOption.REPLACE_EXISTING
                    );
                    dbFound = true;
                }

                zip.closeEntry();
            }
        }

        if (!manifestFound || !dbFound) {
            throw new IllegalArgumentException("Invalid backup package.");
        }

        String formatVersion = manifest.getProperty("formatVersion", "");

        if (!FORMAT_VERSION.equals(formatVersion)) {
            throw new IllegalArgumentException("Unsupported backup format version.");
        }

        String dbKeySalt = manifest.getProperty("dbKeySalt", "");
        String encryptedDbKey = manifest.getProperty("encryptedDbKey", "");

        if (dbKeySalt.isBlank() || encryptedDbKey.isBlank()) {
            throw new IllegalArgumentException("Backup metadata is incomplete.");
        }

        ExtractedBackup backup = new ExtractedBackup();
        backup.databasePath = extractedDb;
        backup.username = manifest.getProperty("username", "unknown");
        backup.dbKeySalt = dbKeySalt;
        backup.encryptedDbKey = encryptedDbKey;

        return backup;
    }

    private static boolean canOpenWithKey(Path dbPath, String dbKey) {
        try (var ignored = EncryptedDbConnectionFactory.open(dbPath, dbKey)) {
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

    private static void deleteDirectory(Path dir) throws Exception {
        if (!Files.exists(dir)) {
            return;
        }

        Files.walk(dir)
                .sorted(java.util.Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (Exception ignored) {
                    }
                });
    }

    private static String rootMessage(Exception e, String fallback) {
        Throwable root = e;

        while (root.getCause() != null) {
            root = root.getCause();
        }

        return root.getMessage() == null || root.getMessage().isBlank()
                ? fallback
                : root.getMessage();
    }

    private static void showInfo(String message) {
        new Alert(Alert.AlertType.INFORMATION, message).showAndWait();
    }

    private static void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message).showAndWait();
    }

    private static class ExtractedBackup {
        Path databasePath;
        String username;
        String dbKeySalt;
        String encryptedDbKey;
    }

    private static boolean isBackupForCurrentUser(ExtractedBackup backup) {
        if (backup == null || backup.username == null) {
            return false;
        }

        String backupUsername = backup.username.trim();
        String currentUsername = CurrentSession.getUsername().trim();

        return backupUsername.equalsIgnoreCase(currentUsername);
    }
}