package com.rent.util;

import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Window;
import com.rent.dao.AuditLogDAO;
import com.rent.util.AuditActions;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class DatabaseResetUtil {

    private static final Path DB_PATH =
            Path.of("src/main/resources/database/rent.db");

    public static boolean confirmFactoryReset(Window ownerWindow) {

        Alert warning = new Alert(Alert.AlertType.WARNING);
        warning.initOwner(ownerWindow);
        warning.setTitle("Factory Reset");
        warning.setHeaderText("This will permanently delete all data.");
        warning.setContentText("""
                Factory Reset will delete:
                
                - Users
                - Properties
                - Flats
                - Tenants
                - Rent records
                - Rent archive
                - Repairs
                - Reports data
                - Settings
                
                This action cannot be undone.
                """);

        warning.showAndWait();

        TextInputDialog dialog = new TextInputDialog();
        dialog.initOwner(ownerWindow);
        dialog.setTitle("Confirm Factory Reset");
        dialog.setHeaderText("Type DELETE ALL DATA to continue");
        dialog.setContentText("Confirmation:");

        Optional<String> result = dialog.showAndWait();

        return result.isPresent()
                && "DELETE ALL DATA".equals(result.get().trim());
    }

    public static boolean factoryReset() {
        try {
            Files.deleteIfExists(DB_PATH);

            DBUtil.init();
            com.rent.dao.UserSecurityDAO.migratePlainPasswordsIfNeeded();
            AuditLogDAO.log(
                    "SYSTEM",
                    AuditActions.FACTORY_RESET,
                    "Factory reset performed. Database deleted and recreated."
            );

            return true;

        } catch (Exception e) {
            e.printStackTrace();

            new Alert(Alert.AlertType.ERROR,
                    "Factory reset failed. Please close the app and try again.").showAndWait();

            return false;
        }
    }
}